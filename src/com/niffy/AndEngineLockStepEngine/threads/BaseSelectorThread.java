package com.niffy.AndEngineLockStepEngine.threads;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.threads.nio.ChangeRequest;
import com.niffy.AndEngineLockStepEngine.threads.nio.Connection;

/**
 * Heavily based on James Greefield <a
 * href=""http://rox-xmlrpc.sourceforge.net/niotut/> ROX Java NIO Tutorial</a>
 * 
 * @author Paul Robinson
 * @author <a href="mailto:nio@flat502.com">James Greenfield</a>
 * @since 11 May 2013 15:09:48
 * 
 * @see<a href=""http://rox-xmlrpc.sourceforge.net/niotut/> ROX Java NIO
 *        Tutorial</a>
 */
public abstract class BaseSelectorThread extends BaseCommunicationThread {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(BaseSelectorThread.class);
	protected static final int DefaultBufferCapacity = 8192;
	// ===========================================================
	// Fields
	// ===========================================================
	protected Selector mSelector;
	protected int mBufferCapacity = 8192;
	protected ByteBuffer readBuffer = ByteBuffer.allocate(8192);
	protected List<ChangeRequest> mPendingChanges = new LinkedList<ChangeRequest>();
	protected Map<String, ArrayList<ByteBuffer>> mPendingData = new HashMap<String, ArrayList<ByteBuffer>>();
	protected HashMap<InetSocketAddress, Connection> mChannelMap = new HashMap<InetSocketAddress, Connection>();

	// ===========================================================
	// Constructors
	// ===========================================================
	/**
	 * Uses the default buffer capacity of {@link #DefaultBufferCapacity}
	 * 
	 * @param pName
	 *            name of thread
	 * @param pAddress
	 *            {@link InetSocketAddress} of client.
	 * @param pCaller
	 *            {@link WeakThreadHandler} to pass messages to.
	 * @param pOptions
	 *            {@link IBaseOptions} to use
	 * @throws IOException
	 *             when calling {@link #initSelector()}
	 */
	public BaseSelectorThread(final String pName, final InetSocketAddress pAddress,
			WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions) throws IOException {
		this(pName, pAddress, pCaller, pOptions, DefaultBufferCapacity);
	}

	/**
	 * 
	 * @param pName
	 *            name of thread
	 * @param pAddress
	 *            {@link InetSocketAddress} of client.
	 * @param pCaller
	 *            {@link WeakThreadHandler} to pass messages to.
	 * @param pOptions
	 *            {@link IBaseOptions} to use
	 * @param pBufferCapacity
	 *            What size should the buffer capacity to read and write.
	 * @throws IOException
	 *             when calling {@link #initSelector()}
	 */
	public BaseSelectorThread(final String pName, final InetSocketAddress pAddress,
			WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions, final int pBufferCapacity)
			throws IOException {
		super(pName, pAddress, pCaller, pOptions);
		this.mAddress = pAddress;
		this.mBufferCapacity = pBufferCapacity;
		ByteBuffer.allocate(this.mBufferCapacity);
		this.mSelector = this.initSelector();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	/**
	 * This creates a new {@link Selector} to use. All concrete classes should
	 * call <code>super.initSelector()</code>
	 * 
	 * @return {@link Selector} to be used
	 * @throws IOException
	 *             when calling {@link SelectorProvider#openSelector()}
	 */
	protected Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();
		return socketSelector;
	}

	protected void finishConnection(SelectionKey pKey) throws IOException {

	}

	protected void accept(SelectionKey pKey) throws IOException {

	}

	protected void read(SelectionKey pKey) throws IOException {

	}

	/**
	 * Write to a channel
	 * 
	 * @param pKey
	 *            {@link SelectionKey} which needs an operation performing on
	 * @throws IOException
	 * @throws CancelledKeyException
	 *             when a call to {@link SelectionKey} which has been cancelled.
	 */
	protected void write(SelectionKey pKey) throws IOException, CancelledKeyException {

	}

	/**
	 * 
	 * @param pKey
	 *            {@link SelectionKey} to cancel
	 * @param pChannel
	 *            {@link AbstractSelectableChannel} to close
	 * @throws IOException
	 *             when {@link AbstractSelectableChannel#close()} is called
	 */
	protected void handleConnectionFailure(SelectionKey pKey, AbstractSelectableChannel pChannel) throws IOException {
		log.warn("A connection failure has occured.");
		log.warn("Cancel key: {}", pKey.toString());
		pKey.cancel();
		log.warn("Closing channel: {}", pChannel.toString());
		pChannel.close();
	}

	protected void handleConnectionShutdown(SelectionKey pKey, AbstractSelectableChannel pChannel) throws IOException {
		log.warn("Shuting down connection cleanly");
		pChannel.close();
		pKey.cancel();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
