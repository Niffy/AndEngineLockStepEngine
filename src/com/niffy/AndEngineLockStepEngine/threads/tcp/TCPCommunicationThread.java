package com.niffy.AndEngineLockStepEngine.threads.tcp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import com.niffy.AndEngineLockStepEngine.flags.ErrorCodes;
import com.niffy.AndEngineLockStepEngine.flags.ITCFlags;
import com.niffy.AndEngineLockStepEngine.messages.IMessage;
import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.threads.CommunicationThread;

public class TCPCommunicationThread extends CommunicationThread {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(TCPCommunicationThread.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected HashMap<InetAddress, IBaseSocketThread> mSockets;
	protected ServerSocket mServerTCPSocket;

	// ===========================================================
	// Constructors
	// ===========================================================

	public TCPCommunicationThread(final InetAddress pAddress, WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions) {
		super(pAddress, pCaller, pOptions);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void run() {
		this.mRunning.set(true);
		if(!this.mSentRunningMessage){
			Message msg = this.mCallerThreadHandler.obtainMessage();
			msg.what = ITCFlags.TCP_THREAD_START;
			this.mCallerThreadHandler.sendMessage(msg);
		}
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
		while (!Thread.interrupted() && this.mRunning.get() && !this.mTerminated.get() && !this.mIgnoreIncoming.get()) {
			try {
				log.info("Accepting socket..");
				final Socket client = this.mServerTCPSocket.accept();
				InetAddress pClientAddress = client.getInetAddress();
				BaseSocketThread clientThread = new BaseSocketThread(this.getHandler(), client,
						pClientAddress.toString(), Looper.getMainLooper());
				clientThread.start();
				this.mSockets.put(client.getInetAddress(), clientThread);
				this.mClients.add(pClientAddress);
				log.info("Accepted: {}", pClientAddress.toString());
				this.clientJoin(pClientAddress);
			} catch (IOException e) {
				log.error("Error with accepting on TCP socket", e);
			}
		}
	}

	@Override
	public void handlePassedMessage(Message pMessage) {
		log.debug("Handling message: {}", pMessage.what);
		super.handlePassedMessage(pMessage);
		Bundle bundle;
		String ip;
		byte[] data;
		switch (pMessage.what) {
		case ITCFlags.TCP_CLIENT_INCOMMING:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			data = bundle.getByteArray("data");
			this.mPacketHandler.reconstructData(ip, data);
			break;
		case ITCFlags.NETWORK_TCP_EXCEPTION:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			this.clientDisconect(ip);
			Message msg = this.mCallerThreadHandler.obtainMessage();
			msg.what = ITCFlags.CLIENT_DISCONNECTED;
			msg.setData(bundle);
			this.mCallerThreadHandler.sendMessage(msg);
		}
	}

	@Override
	public <T extends IMessage> int sendMessage(InetAddress pAddress, T pMessage) {
		byte[] pData = null;
		while (!Thread.interrupted() && this.mRunning.get() && !this.mTerminated.get() && !this.mIgnoreIncoming.get()) {
			IBaseSocketThread clientThread = this.mSockets.get(pAddress);
			if (clientThread != null) {
				try {
					final ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
					final DataOutputStream dOutput = new DataOutputStream(bOutput);
					pMessage.write(dOutput);
					dOutput.flush();
					bOutput.flush();
					pData = bOutput.toByteArray();
					Message msg = clientThread.getHandler().obtainMessage();
					msg.what = ITCFlags.TCP_CLIENT_OUTGOING;
					Bundle bundle = new Bundle();
					bundle.putByteArray("data", bOutput.toByteArray());
					msg.setData(bundle);
					clientThread.getHandler().sendMessage(msg);
				} catch (IOException e) {
					log.error("Error sending TCP message to client thread: {}", pAddress, e);
					this.networkMessageFailure(pAddress.toString(), pData, ITCFlags.NETWORK_SEND_MESSAGE_FAILURE,
							ErrorCodes.COULD_NOT_SEND);
				}
			}
		}
		return 0;
	}

	@Override
	protected void producePoolItems() {
		super.producePoolItems();
	}

	@Override
	protected void sendMessageWithPacketHandler(final int pIntended, InetAddress pAddress, byte[] pData) {
		super.sendMessageWithPacketHandler(pIntended, pAddress, pData);
	}

	@Override
	protected void sendMessageWithPacketHandler(final int pIntended, byte[] pData) {
		super.sendMessageWithPacketHandler(pIntended, pData);
	}

	@Override
	public void terminate() {
		log.warn("Terminating the thread");
		try {
			if (!this.mTerminated.getAndSet(true)) {
				this.mRunning.getAndSet(false);
				this.mServerTCPSocket.close();
				Iterator<Entry<InetAddress, IBaseSocketThread>> entries = this.mSockets.entrySet().iterator();
				while (entries.hasNext()) {
					Entry<InetAddress, IBaseSocketThread> entry = entries.next();
					Message msg = entry.getValue().getHandler().obtainMessage();
					msg.what = ITCFlags.NETWORK_TCP_SHUTDOWN_SOCKET;
					entry.getValue().getHandler().sendMessage(msg);
				}
				this.interrupt();
			}
		} catch (IOException e) {
			log.error("Exception when shuting down the TCP thread", e);
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	protected void clientJoin(final InetAddress pAddress) {
		this.mPacketHandler.addClient(pAddress);
		Message msg = this.mCallerThreadHandler.obtainMessage();
		msg.what = ITCFlags.CLIENT_CONNECTED;
		final Bundle pBundle = new Bundle();
		pBundle.putString("ip", pAddress.toString());
		this.mCallerThreadHandler.sendMessage(msg);
	}

	protected void clientDisconect(final String pAddress) {
		try {
			InetAddress addressCast = InetAddress.getByName(pAddress);
			this.removeClient(addressCast);
		} catch (UnknownHostException e) {
			log.error("Could not disconnect client as could not cast address: {}", pAddress, e);
		}
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
