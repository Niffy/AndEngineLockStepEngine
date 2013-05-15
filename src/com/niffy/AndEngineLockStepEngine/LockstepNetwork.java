package com.niffy.AndEngineLockStepEngine;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import com.niffy.AndEngineLockStepEngine.messages.pool.MessagePoolTags;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.nio.ICommunicationHandler;

public class LockstepNetwork implements ILockstepNetwork {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(LockstepNetwork.class);
	// ===========================================================
	// Fields
	// ===========================================================
	protected final ILockstepEngine mLockstepEngine;
	protected ICommunicationHandler mCommunicationHandler;
	protected ArrayList<InetAddress> mClients;
	protected IBaseOptions mBaseOptions;
	protected MessagePool<IMessage> mMessagePool;

	// ===========================================================
	// Constructors
	// ===========================================================

	public LockstepNetwork(ILockstepEngine pLockstepEngine, IBaseOptions pBaseOptions) {
		this.mLockstepEngine = pLockstepEngine;
		this.mBaseOptions = pBaseOptions;
		this.mClients = new ArrayList<InetAddress>();
		this.mMessagePool = new MessagePool<IMessage>();
		this.producePoolItems();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@SuppressWarnings("unused")
	@Override
	public void handlePassedMessage(Message pMessage) {
		log.debug("Handling message: {}", pMessage.what);
		Bundle bundle;
		String ip;
		byte[] data;
		switch (pMessage.what) {
		/* TODO check all are within of lockstep handling */
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
			this.clientError(ip, pMsg);
			break;
		case ITCFlags.CLIENT_WINDOW_NOT_EMPTY:
			bundle = pMessage.getData();
			ip = bundle.getString("ip");
			this.clientOutOfSync(ip);
			break;
		case ITCFlags.RECIEVE_MESSAGE_LOCKSTEP:
			bundle = pMessage.getData();
			this.handleIncomePacket(bundle);
			break;
		case ITCFlags.NETWORK_ERROR:
			this.networkError("Unknown network error");
			break;
		case ITCFlags.CONNECT_TO:
			//this.mLockstepEngine.getLockstepClientListener().connected();
			break;
		case ITCFlags.CONNECT_TO_ERROR:
			this.mLockstepEngine.getLockstepClientListener().connectError();
			break;
		case ITCFlags.CONNECTED_TO_HOST:
			this.mLockstepEngine.getLockstepClientListener().connected();
			break;
		case ITCFlags.NETWORK_SEND_MESSAGE_FAILURE:
			bundle = pMessage.getData();
			this.mLockstepEngine.getLockstepClientListener().networkError("Could not send message");
			break;
		}
	}

	@Override
	public ICommunicationThread getTCPThread() {
		return this.mTCP;
	}

	@Override
	public ICommunicationThread getUDPThread() {
		return this.mUDP;
	}

	@Override
	public void ignoreTCPCommunication(boolean pIgnore) {
		log.debug("Ignore TCP coms: {}", pIgnore);
		this.mTCP.setIgnoreIncoming(pIgnore);
		Message msg = this.mTCP.getHandler().obtainMessage();
		msg.what = ITCFlags.IGNORE;
		Bundle pData = new Bundle();
		pData.putBoolean("boolean", pIgnore);
		msg.setData(pData);
		this.mTCP.getParentHandler().sendMessage(msg);
	}

	@Override
	public void ignoreUDPCommunication(boolean pIgnore) {
		log.debug("Ignore UDP coms: {}", pIgnore);
		this.mUDP.setIgnoreIncoming(pIgnore); 
	}

	@Override
	public void setMainCommunicationThread(ICommunicationThread pThread) {
		this.mCommunicationThread = pThread;
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
		this.sendMigrateMessage();
		this.triggerMigrate();
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

	@Override
	public <T extends IMessage> int sendMessage(InetAddress pAddress, T pMessage) {
		byte[] buf = null;
		final ByteArrayOutputStream bOutput = new ByteArrayOutputStream();
		final DataOutputStream dOutput = new DataOutputStream(bOutput);
		try {
			pMessage.write(dOutput);
			dOutput.flush();
			buf = bOutput.toByteArray();
			Bundle pData = new Bundle();
			pData.putString("ip", pAddress.getHostAddress());
			pData.putInt("intended", IntendedFlag.LOCKSTEP);
			pData.putByteArray("data", buf);
			Message msg = this.mCommunicationThread.getHandler().obtainMessage();
			msg.what = ITCFlags.SEND_MESSAGE;
			msg.setData(pData);
			this.mCommunicationThread.getHandler().sendMessage(msg);
		} catch (IOException e) {
			log.error("Could not pass packet to communication thread", e);
			this.networkError("Could not pass packet to communication thread, error: " + e.toString());
			return -1;
		}
	
		return 0;
	}

	@Override
	public IMessage obtainMessage(int pFlag) {
		return this.mMessagePool.obtainMessage(pFlag);
	}

	@Override
	public <T extends IMessage> void recycleMessage(T pMessage) {
		this.mMessagePool.recycleMessage(pMessage);
	}

	@Override
	public void connectTo(String pAddress) {
		//HandlerThread tcpHandlerThread = (HandlerThread) this.mTCP;
		//Handler handler = new Handler(tcpHandlerThread.getLooper()); 
		Message msg = this.mTCP.getHandler().obtainMessage();
		//Message msg = handler.obtainMessage();
		msg.what = ITCFlags.CONNECT_TO;
		Bundle pData = new Bundle();
		pData.putString("ip", pAddress);
		msg.setData(pData);
		this.mTCP.getHandler().sendMessage(msg);
		//handler.sendMessage(msg);
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
		log.debug("Client joined: {}", pAddress);
		InetAddress cast = this.castAddress(pAddress);
		if (cast != null) {
			this.addClient(cast);
			this.mLockstepEngine.getLockstepClientListener().clientConnected(cast);
		} else {
			log.error("Could not add client as could not cast address: {}", pAddress);
		}
	}

	protected void clientDisconect(final String pAddress) {
		InetAddress cast = this.castAddress(pAddress);
		if (cast != null) {
			this.removeClient(cast);
			this.mLockstepEngine.getLockstepClientListener().clientDisconnected(cast);
		} else {
			log.error("Could not remove client as could not cast address: {}", pAddress);
		}
	}

	public void clientError(final String pAddress, final String pError) {
		InetAddress cast = this.castAddress(pAddress);
		if (cast != null) {
			this.mLockstepEngine.getLockstepClientListener().clientError(cast, pError);
		} else {
			log.error("Could not inform of client error as could not cast address: {}", pAddress);
		}
	}

	public void clientOutOfSync(final String pAddress) {
		InetAddress cast = this.castAddress(pAddress);
		if (cast != null) {
			this.mLockstepEngine.getLockstepClientListener().clientOutOfSync(cast);
		} else {
			log.error("Could not inform of client out of sync as could not cast address: {}", pAddress);
		}
	}

	public void networkError(final String pMessage) {
		this.mLockstepEngine.getLockstepClientListener().networkError(pMessage);
	}

	@SuppressWarnings("unused")
	protected void handleIncomePacket(final Bundle pBundle) {
		final String pIp = pBundle.getString("ip");
		final int pFlag = pBundle.getInt("flag");
		final byte[] pData = pBundle.getByteArray("data");
		if (pFlag == MessageFlag.MIGRATE) {
			this.triggerMigrate();
		}
	}

	protected void triggerMigrate() {
		/*
		 * TODO Perhaps ignore coms isn't so good?
		 */
		//this.ignoreTCPCommunication(false);
		//this.ignoreUDPCommunication(false);
		this.mCommunicationThread = this.mUDP;
		this.mLockstepEngine.getLockstepClientListener().migrate();
	}

	protected void sendMigrateMessage() {
		MessageMigrate pMessage = (MessageMigrate) this.obtainMessage(MessageFlag.MIGRATE);
		pMessage.setRequireAck(true); // Although it doesn't really matter
		pMessage.setIntended(IntendedFlag.LOCKSTEP);
		final int pClientCount = this.mClients.size();
		for (int i = 0; i < pClientCount; i++) {
			this.sendMessage(this.mClients.get(i), pMessage);
		}
		this.recycleMessage(pMessage);
	}

	protected void producePoolItems() {
		Integer pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.MIGRATE_INITIAL_STRING);
		Integer pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.MIGRATE_GROWTH_STRING);
		int pInitialSize = (pGetIntialSize != -1) ? pGetIntialSize : MessagePoolTags.MIGRATE_INITIAL_INT;
		int pGrowth = (pGetGrowth != -1) ? pGetGrowth : MessagePoolTags.MIGRATE_GROWTH_INT;
		int pFlag = MessageFlag.MIGRATE;
		Class<? extends IMessage> pMessageClass = MessageMigrate.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	@Override
	public boolean allRunning() {
		if (this.mTCP != null && this.mUDP != null) {
			if (this.mTCP.isRunning() && this.mUDP.isRunning()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
