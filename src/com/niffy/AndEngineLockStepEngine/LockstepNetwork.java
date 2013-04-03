package com.niffy.AndEngineLockStepEngine;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.os.Message;

import com.niffy.AndEngineLockStepEngine.flags.ITCFlags;
import com.niffy.AndEngineLockStepEngine.flags.IntendedFlag;
import com.niffy.AndEngineLockStepEngine.flags.MessageFlag;
import com.niffy.AndEngineLockStepEngine.messages.IMessage;
import com.niffy.AndEngineLockStepEngine.messages.MessageMigrate;
import com.niffy.AndEngineLockStepEngine.messages.pool.MessagePool;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;

public class LockstepNetwork implements ILockstepNetwork {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(LockstepNetwork.class);
	// ===========================================================
	// Fields
	// ===========================================================
	protected final ILockstepEngine mLockstepEngine;
	protected ICommunicationThread mCommunicationThread;
	protected ICommunicationThread mUDP;
	protected ICommunicationThread mTCP;
	protected ArrayList<InetAddress> mClients;
	protected MessagePool<IMessage> mMessagePool;
	// ===========================================================
	// Constructors
	// ===========================================================

	public LockstepNetwork(ILockstepEngine pLockstepEngine, IBaseOptions pBaseOptions) {
		this.mLockstepEngine = pLockstepEngine;
		this.mClients = new ArrayList<InetAddress>();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@SuppressWarnings("unused")
	@Override
	public void handlePassedMessage(Message pMessage) {
		Bundle bundle;
		String ip;
		byte[] data;
		switch (pMessage.what) {
		case ITCFlags.CLIENT_CONNECTED:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			this.clientJoin(ip);
			break;
		case ITCFlags.CLIENT_DISCONNECTED:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			this.clientDisconect(ip);
			break;
		case ITCFlags.CLIENT_ERROR:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			final String pMsg = "Error.";
			this.mLockstepEngine.getLockstepClientListener().clientError(this.castAddress(ip), pMsg);
			break;
		case ITCFlags.CLIENT_WINDOW_NOT_EMPTY:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			this.mLockstepEngine.getLockstepClientListener().clientOutOfSync(this.castAddress(ip));
			break;
		case ITCFlags.RECIEVE_MESSAGE_LOCKSTEP:
			bundle = pMessage.getData();
			this.handleIncomePacket(bundle);
			break;
		case ITCFlags.NETWORK_ERROR:
			/* TODO handle this*/
			break;
		}
	}

	@Override
	public void ignoreTCPCommunication(boolean pIgnore) {
		this.mTCP.setIgnoreIncoming(pIgnore);
	}

	@Override
	public void ignoreUDPCommunication(boolean pIgnore) {
		this.mUDP.setIgnoreIncoming(pIgnore);
	}

	@Override
	public void terminateTCPThread() {
		this.mTCP.terminate();
	}

	@Override
	public void terminateUDPThread() {
		this.mUDP.terminate();
	}

	@Override
	public void attachTCPThread(ICommunicationThread pThread) {
		this.mTCP = pThread;
		if (!pThread.isIgnoring()) {
			log.warn("TCP Thread is not ignoring incoming data.");
		}
	}

	@Override
	public void attachUDPThread(ICommunicationThread pThread) {
		this.mUDP = pThread;
	}

	@Override
	public void migrate() {
		this.ignoreTCPCommunication(true);
		this.ignoreUDPCommunication(false);
		this.sendMigrateMessage();
	}

	@Override
	public void addClient(InetAddress pAddress) {
		if (!this.mClients.contains(pAddress)) {
			this.mClients.add(pAddress);
		} else {
			log.warn("Client is already added: {}", pAddress);
		}
	}

	@Override
	public ArrayList<InetAddress> getClients() {
		return this.mClients;
	}

	@Override
	public void removeClient(InetAddress pAddress) {
		if (this.mClients.contains(pAddress)) {
			this.mClients.remove(pAddress);
		} else {
			log.warn("Client is not in the list to remove: {}", pAddress);
		}
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	protected InetAddress castAddress(final String pAddress) {
		try {
			InetAddress addressCast = InetAddress.getByName(pAddress);
			return addressCast;
		} catch (UnknownHostException e) {
			log.error("Could notnot cast address: {}", pAddress, e);
		}
		return null;
	}

	protected void clientJoin(final String pAddress) {
		InetAddress cast = this.castAddress(pAddress);
		if (cast != null) {
			this.addClient(cast);
		} else {
			log.error("Could not add client as could not cast address: {}", pAddress);
		}
	}

	protected void clientDisconect(final String pAddress) {
		InetAddress cast = this.castAddress(pAddress);
		if (cast != null) {
			this.removeClient(cast);
		} else {
			log.error("Could not remove client as could not cast address: {}", pAddress);
		}
	}

	protected void handleIncomePacket(final Bundle pBundle) {
		final String pIp = pBundle.getString("ip");
		final int pFlag = pBundle.getInt("flag");
		final byte[] pData = pBundle.getByteArray("data");
		if (pFlag == MessageFlag.MIGRATE) {
			this.migrate();
		}
	}

	protected void sendMigrateMessage(){
		MessageMigrate pMessage = (MessageMigrate) this.obtainMessage(MessageFlag.MIGRATE);
		pMessage.setRequireAck(true); //Although it doesn't really matter
		pMessage.setIntended(IntendedFlag.LOCKSTEP);
		final int pClientCount = this.mClients.size();
		for (int i = 0; i < pClientCount; i++) {
			this.mPacketHandler.sendMessage(this.mClients.get(i), pMessage);
		}
		this.recycleMessage(pMessage);
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
