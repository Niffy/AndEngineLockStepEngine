package com.niffy.AndEngineLockStepEngine.threads.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.threads.BaseSelectorThread;

public class ServerSelector extends BaseSelectorThread {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(ServerSelector.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected ServerSocketChannel mTCPChannel;

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * @see {@link BaseSelectorThread#BaseSelectorThread(String, InetSocketAddress, WeakThreadHandler, IBaseOptions, int)}
	 */
	public ServerSelector(final String pName, final InetSocketAddress pAddress,
			WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions) throws IOException {
		super(pName, pAddress, pCaller, pOptions);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void run() {
		log.debug("Running TCP Selector Thread");
		while (true) {
			try {
				// Process any pending changes
				synchronized (this.mPendingChanges) {
					Iterator<ChangeRequest> changes = this.mPendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.mType) {
						case ChangeRequestTCP.CHANGEOPS:
							SelectionKey key = change.mChannel.keyFor(this.mSelector);
							if (key == null) {
								log.error("Could not change channel operations for. Null key {} ",
										change.mChannel.toString());
							} else {
								try {
									key.interestOps(change.mOps);
								} catch (IllegalArgumentException e) {
									log.error("IllegalArgumentException", e);
									/* TODO handle this, clean up pending data and pending changes?
									 * And remove from any collections
									 */
								} catch (CancelledKeyException e) {
									log.error("CancelledKeyException", e);
									/* TODO handle this, clean up pending data and pending changes?
									 * And remove from any collections
									 */
								}
							}
						}
					}
					this.mPendingChanges.clear();
				}

				// Wait for an event one of the registered channels
				this.mSelector.select();

				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = this.mSelector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					try {
						if (key.isAcceptable()) {
							this.accept(key);
						} else if (key.isReadable()) {
							this.read(key);
						} else if (key.isWritable()) {
							this.write(key);
						}
					} catch (IOException e) {
						log.error("IOException on key operation", e);
					}
				}
			} catch (Exception e) {
				log.error("Exception in main loop", e);
			}
		}
	}

	@Override
	protected Selector initSelector() throws IOException {
		Selector found = super.initSelector();

		// Create a new non-blocking server socket channel
		this.mTCPChannel = ServerSocketChannel.open();
		this.mTCPChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		this.mTCPChannel.socket().bind(this.mAddress);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		this.mTCPChannel.register(found, SelectionKey.OP_ACCEPT);

		return found;
	}

	@Override
	protected void accept(SelectionKey pKey) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) pKey.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		Socket socket = socketChannel.socket();
		socketChannel.configureBlocking(false);
		socketChannel.register(this.mSelector, SelectionKey.OP_READ);
		Connection con = new Connection((InetSocketAddress) socket.getRemoteSocketAddress(), socketChannel);
		this.mChannelMap.put(con.getAddress(), con);
		pKey.attach(con);
		/* 
		 * TODO inform of new connection has been made
		 */
	}

	@Override
	protected void read(SelectionKey pKey) throws IOException {
		SocketChannel socketChannel;
		String connectionIP;
		Connection con = (Connection) pKey.attachment();
		if (con != null) {
			socketChannel = con.getSocketChannel();
			connectionIP = con.getAddress().getAddress().getHostAddress();
		} else {
			socketChannel = (SocketChannel) pKey.channel();
			InetSocketAddress address = (InetSocketAddress) socketChannel.socket().getRemoteSocketAddress();
			connectionIP = address.getAddress().getHostAddress();
			log.warn("Could not get Connection attachment for IP: {}", connectionIP);
		}
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead = -1;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (AsynchronousCloseException e) {
			log.error("For IP: {}", connectionIP);
			log.error("AsynchronousCloseException", e);
			/* TODO Handle this */
			this.handleConnectionFailure(pKey, socketChannel);
		} catch (NotYetConnectedException e) {
			log.error("For IP: {}", connectionIP);
			log.error("NotYetConnectedException", e);
			this.handleConnectionFailure(pKey, socketChannel);
			/* TODO Handle this */
		} catch (ClosedChannelException e) {
			log.error("For IP: {}", connectionIP);
			log.error("ClosedChannelException", e);
			this.handleConnectionFailure(pKey, socketChannel);
			/* TODO Handle this */
		} catch (IOException e) {
			log.error("For IP: {}", connectionIP);
			log.error("IOException", e);
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			this.handleConnectionFailure(pKey, socketChannel);
			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			/* TODO check that closing socketChannel is ok, 
			 * we were doing pKey.channel().close()
			 */
			this.handleConnectionShutdown(pKey, socketChannel);
			return;
		}
		/* TODO pass message out of thread */
	}

	/**
	 * @throws IOException
	 *             due to {@link SocketChannel#write(ByteBuffer)} call
	 * @throws CancelledKeyException
	 * @see com.niffy.AndEngineLockStepEngine.threads.BaseSelectorThread#write(java.nio.channels.SelectionKey)
	 */
	@Override
	protected void write(SelectionKey pKey) throws IOException, CancelledKeyException {
		SocketChannel socketChannel;
		String connectionIP;
		Connection con = (Connection) pKey.attachment();
		if (con != null) {
			socketChannel = con.getSocketChannel();
			connectionIP = con.getAddress().getAddress().getHostAddress();
		} else {
			socketChannel = (SocketChannel) pKey.channel();
			InetSocketAddress address = (InetSocketAddress) socketChannel.socket().getRemoteSocketAddress();
			connectionIP = address.getAddress().getHostAddress();
			log.warn("Could not get Connection attachment for IP: {}", connectionIP);
		}

		synchronized (this.mPendingData) {
			ArrayList<ByteBuffer> queue = this.mPendingData.get(connectionIP);

			// Write until there's not more data ...
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					// ... or the socket's buffer fills up
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for
				// data.
				pKey.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
