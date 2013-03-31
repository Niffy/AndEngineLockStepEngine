package com.niffy.AndEngineLockStepEngine.threads.tcp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

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

	public TCPCommunicationThread(WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions) {
		super(pCaller, pOptions);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void run() {
		this.mRunning.set(true);
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
		switch (pMessage.what) {
		case ITCFlags.TCP_CLIENT_INCOMMING:
			bundle = pMessage.getData();
			final String ip = bundle.getString("ip");
			final byte[] data = bundle.getByteArray("data");
			this.mPacketHandler.reconstructData(ip, data);
			break;
		}
	}

	@Override
	public <T extends IMessage> int sendMessage(InetAddress pAddress, T pMessage) {
		while (!Thread.interrupted() && this.mRunning.get() && !this.mTerminated.get() && !this.mIgnoreIncoming.get()) {
			IBaseSocketThread clientThread = this.mSockets.get(pAddress);
			if (clientThread != null) {
				try {
					final ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
					final DataOutputStream dOutput = new DataOutputStream(bOutput);
					pMessage.write(dOutput);
					dOutput.flush();
					bOutput.flush();
					Message msg = clientThread.getHandler().obtainMessage();
					msg.what = ITCFlags.TCP_CLIENT_OUTGOING;
					Bundle bundle = new Bundle();
					bundle.putByteArray("data", bOutput.toByteArray());
					msg.setData(bundle);
					clientThread.getHandler().sendMessage(msg);
				} catch (IOException e) {
					log.error("Error sending TCP message to client thread: {}", pAddress, e);
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

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public void clientJoin(final InetAddress pAddress) {
		this.mPacketHandler.addClient(pAddress);
		Message msg = this.mCallerThreadHandler.obtainMessage();
		msg.what = ITCFlags.CLIENT_CONNECTED;
		msg.obj = pAddress;
		this.mCallerThreadHandler.sendMessage(msg);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
