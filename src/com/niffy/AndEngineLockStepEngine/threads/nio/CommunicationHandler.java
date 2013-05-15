package com.niffy.AndEngineLockStepEngine.threads.nio;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

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

public class CommunicationHandler extends CommunicationThread implements ICommunicationHandler {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(CommunicationHandler.class);
	/*
	 * TODO Set main selector to use.
	 * TODO handle sending via TCP or UDP.
	 */
	// ===========================================================
	// Fields
	// ===========================================================
	protected ISelectorThread mUDP;
	protected ISelectorThread mTCPServer;
	protected ISelectorThread mTCPClient;
	protected ISelectorThread mMainSelector;
	protected SelectorType mCurrentSelectorInUse;

	// ===========================================================
	// Constructors
	// ===========================================================

	public CommunicationHandler(final String pName, final InetSocketAddress pAddress,
			WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions) {
		super(pName, pAddress, pCaller, pOptions);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void setUDPSelectorThread(ISelectorThread pSelectorThread) {
		this.mUDP = pSelectorThread;
	}

	@Override
	public void setTCPClientSelectorThread(ISelectorThread pSelectorThread) {
		this.mTCPServer = pSelectorThread;
	}

	@Override
	public void setTCPServerSelectorThread(ISelectorThread pSelectorThread) {
		this.mTCPClient = pSelectorThread;
	}

	@Override
	public void run() {
		Looper.prepare();
		this.mRunning.set(true);
		this.mHandler = new WeakThreadHandler<IHandlerMessage>(this, Looper.myLooper());
		Thread udp = (Thread) this.mUDP;
		udp.start();
		Thread tcpClient = (Thread) this.mTCPClient;
		tcpClient.start();
		Thread tcpServer = (Thread) this.mTCPServer;
		tcpServer.start();
		Looper.loop();
	}

	@Override
	public void handlePassedMessage(Message pMessage) {
		/*
		 * TODO message to select either UDP or TCPClient to send messages
		 */
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
			break;
		}
	}

	@Override
	protected void connect(final String pAddress) {
		/*
		 * TODO send to TCPClient to create connection.
		 * TODO implement a circle. When we connect as client we expect a connection back in the server region
		 */
	}

	@Override
	public <T extends IMessage> int sendMessage(InetAddress pAddress, T pMessage) {
		log.debug("Send Message to: {}", pAddress.getHostAddress());
		byte[] pData = null;
		while (!Thread.interrupted() && this.mRunning.get() && !this.mTerminated.get()) {
			try {
				String addr = pAddress.getHostAddress();
				final ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
				final DataOutputStream dOutput = new DataOutputStream(bOutput);
				pMessage.write(dOutput);
				dOutput.flush();
				bOutput.flush();
				pData = bOutput.toByteArray();
				// TODO send via mMainSelector
			} catch (IOException e) {
				log.error("Error sending message to client: {}", pAddress, e);
				this.networkMessageFailure(pAddress.toString(), pData, ITCFlags.NETWORK_SEND_MESSAGE_FAILURE,
						ErrorCodes.COULD_NOT_SEND);
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
		if (!this.mTerminated.getAndSet(true)) {
			this.mRunning.getAndSet(false);
			this.interrupt();
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	protected void clientJoin(final InetAddress pAddress) {
		/*
		 * TODO check if TCPClient has made a connection to the caller.
		 * If not initiate so, wait for TCPClient to inform of connect 
		 * success then call this method again
		 */
		/*
		this.mPacketHandler.addClient(pAddress);
		Message msg = this.mCallerThreadHandler.obtainMessage();
		msg.what = ITCFlags.CLIENT_CONNECTED;
		final Bundle pBundle = new Bundle();
		pBundle.putString("ip", pAddress.getHostAddress());
		msg.setData(pBundle);
		this.mCallerThreadHandler.sendMessage(msg);
		 */
	}

	protected void clientDisconect(final String pAddress) {
		/*
		 * TODO remove in TCPClient and TCPServer
		 */
		/*
		try {
			InetAddress addressCast = InetAddress.getByName(pAddress);
			this.removeClient(addressCast);
		} catch (UnknownHostException e) {
			log.error("Could not disconnect client as could not cast address: {}", pAddress, e);
		}
		 */
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
