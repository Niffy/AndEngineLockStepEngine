package com.niffy.AndEngineLockStepEngine.threads;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.os.Looper;
import android.os.Message;

import com.niffy.AndEngineLockStepEngine.flags.ITCFlags;
import com.niffy.AndEngineLockStepEngine.flags.IntendedFlag;
import com.niffy.AndEngineLockStepEngine.flags.MessageFlag;
import com.niffy.AndEngineLockStepEngine.messages.IMessage;
import com.niffy.AndEngineLockStepEngine.messages.MessageAck;
import com.niffy.AndEngineLockStepEngine.messages.MessageAckMulti;
import com.niffy.AndEngineLockStepEngine.messages.MessageClientDisconnect;
import com.niffy.AndEngineLockStepEngine.messages.MessageClientJoin;
import com.niffy.AndEngineLockStepEngine.messages.MessageEncapsulated;
import com.niffy.AndEngineLockStepEngine.messages.MessageError;
import com.niffy.AndEngineLockStepEngine.messages.MessageMigrate;
import com.niffy.AndEngineLockStepEngine.messages.MessagePing;
import com.niffy.AndEngineLockStepEngine.messages.MessagePingAck;
import com.niffy.AndEngineLockStepEngine.messages.MessagePingHighest;
import com.niffy.AndEngineLockStepEngine.messages.pool.MessagePool;
import com.niffy.AndEngineLockStepEngine.messages.pool.MessagePoolTags;
import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.packet.IPacketHandler;
import com.niffy.AndEngineLockStepEngine.packet.PacketHandler;

public abstract class CommunicationThread extends Thread implements ICommunicationThread {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(CommunicationThread.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected WeakThreadHandler<IHandlerMessage> mCallerThreadHandler;
	protected WeakThreadHandler<IHandlerMessage> mHandler;
	protected Looper mLooper;
	protected IBaseOptions mBaseOptions;
	protected final AtomicBoolean mRunning = new AtomicBoolean(false);
	protected final AtomicBoolean mTerminated = new AtomicBoolean(false);
	protected final AtomicBoolean mIgnoreIncoming = new AtomicBoolean(true);
	protected IPacketHandler mPacketHandler;
	protected ArrayList<InetAddress> mClients;
	protected MessagePool<IMessage> mMessagePool;

	// ===========================================================
	// Constructors
	// ===========================================================

	public CommunicationThread(WeakThreadHandler<IHandlerMessage> pCaller, final IBaseOptions pOptions) {
		this.mCallerThreadHandler = pCaller;
		this.mBaseOptions = pOptions;
		this.mClients = new ArrayList<InetAddress>();
		this.mLooper = Looper.myLooper();
		this.mHandler = new WeakThreadHandler<IHandlerMessage>(this);
		this.mPacketHandler = new PacketHandler(this, this.mBaseOptions);
		this.mMessagePool = new MessagePool<IMessage>();
		this.producePoolItems();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void handlePassedMessage(Message pMessage) {
		Bundle bundle;
		switch (pMessage.what) {
		case ITCFlags.SEND_MESSAGE:
			bundle = pMessage.getData();
			final String ip = bundle.getString("ip", null);
			final int intended = bundle.getInt("intended", -1);
			final byte[] data = bundle.getByteArray("data");
			this.sendMessageWithPacketHandler(intended, ip, data);
			break;
		case ITCFlags.LOCKSTEP_INCREMENT:
			bundle = pMessage.getData();
			final int pStep = bundle.getInt("step");
			this.mPacketHandler.lockstepIncrement(pStep);
		}
	}

	@Override
	public WeakThreadHandler<IHandlerMessage> getParentHandler() {
		return this.mCallerThreadHandler;
	}

	@Override
	public WeakThreadHandler<IHandlerMessage> getHandler() {
		return this.mHandler;
	}

	@Override
	public void windowNotEmpty(InetAddress pAddress) {
		if (this.mCallerThreadHandler != null) {
			Message pMessage = this.mCallerThreadHandler.obtainMessage();
			/* TODO produce error message to send to all */
			this.mCallerThreadHandler.sendMessage(pMessage);
		}
	}

	@Override
	public boolean isRunning() {
		return this.mRunning.get();
	}

	@Override
	public boolean isTerminated() {
		return this.mTerminated.get();
	}

	@Override
	public void terminate() {
		log.info("Terminating the thread");
		if (!this.mTerminated.getAndSet(true)) {
			this.mRunning.getAndSet(false);
			this.interrupt();
		}
	}

	@Override
	public boolean isIgnoring() {
		return this.mIgnoreIncoming.get();
	}

	@Override
	public void allowProcessing() {
		this.mIgnoreIncoming.getAndSet(false);
	}

	@Override
	public void addClient(InetAddress pAddress) {
		this.mClients.add(pAddress);
		this.mPacketHandler.addClient(pAddress);
	}

	@Override
	public ArrayList<InetAddress> getClients() {
		return this.mClients;
	}

	@Override
	public void removeClient(InetAddress pAddress) {
		this.mClients.remove(pAddress);
		this.mPacketHandler.removeClient(pAddress);
	}

	/**
	 * Leave implementation to the subclasses
	 * 
	 * @return <code>0</code> as the packet handler should have generated this
	 * @see com.niffy.AndEngineLockStepEngine.packet.ISendMessage#sendMessage(java.net.InetAddress,
	 *      com.niffy.AndEngineLockStepEngine.messages.IMessage)
	 */
	@Override
	public <T extends IMessage> int sendMessage(InetAddress pAddress, T pMessage) {
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

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	/**
	 * Send message to a client.
	 * 
	 * @param pIntended
	 *            {@link Integer} Who the message is intended for.
	 * @param pAddress
	 *            {@link InetAddress} as {@link String} or <code>null</code> if
	 *            to everyone
	 * @param pData
	 *            {@link Byte} array of the message to be encapsulated.
	 */
	protected void sendMessageWithPacketHandler(final int pIntended, final String pAddress, final byte[] pData) {
		if (pAddress == null) {
			this.sendMessageWithPacketHandler(pIntended, pData);
		} else {
			try {
				InetAddress pAddressCast = InetAddress.getByName(pAddress);
				this.sendMessageWithPacketHandler(pIntended, pAddressCast, pData);
			} catch (UnknownHostException e) {
				log.error("Could not cast: {} to an InetAddress", pAddress, e);
				/*
				 * TODO inform of error
				 */
			}
		}
	}

	/**
	 * Send to someone specific.
	 * 
	 * @param pIntended
	 *            {@link Integer} Who the message is intended for. If
	 *            <code>-1</code> then {@link MessageFlag#LOCKSTEP_CLIENT} is
	 *            used
	 * @param pAddress
	 *            {@link InetAddress} as {@link String} cannot be
	 *            <code>null</code>
	 * @param pData
	 *            {@link Byte} array of the message to be encapsulated.
	 */
	protected void sendMessageWithPacketHandler(final int pIntended, final InetAddress pAddress, final byte[] pData) {
		MessageEncapsulated pMessage = (MessageEncapsulated) this.obtainMessage(MessageFlag.ENCAPSULATED);
		pMessage.setData(pData);
		pMessage.setRequireAck(true);
		if (pIntended != -1) {
			pMessage.setIntended(pIntended);
		} else {
			pMessage.setIntended(IntendedFlag.LOCKSTEP_CLIENT);
		}
		this.mPacketHandler.sendMessage(pAddress, pMessage);
	}

	/**
	 * Send to all
	 * 
	 * @param pIntended
	 *            {@link Integer} Who the message is intended for. If
	 * @param pData
	 *            {@link Byte} array of the message to be encapsulated.
	 */
	protected void sendMessageWithPacketHandler(final int pIntended, final byte[] pData) {
		final int pClientCount = this.mClients.size();
		for (int i = 0; i < pClientCount; i++) {
			this.sendMessageWithPacketHandler(pIntended, this.mClients.get(i), pData);
		}
	}

	/**
	 * Produce the pool items required
	 */
	protected void producePoolItems() {
		Integer pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.ACK_INITIAL_STRING);
		Integer pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.ACK_GROWTH_STRING);
		int pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.ACK_INITIAL_INT;
		int pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.ACK_INITIAL_INT;
		int pFlag = MessageFlag.ACK;
		Class<? extends IMessage> pMessageClass = MessageAck.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.ACK_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.ACK_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.ACK_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.ACK_INITIAL_INT;
		pFlag = MessageFlag.ACK_MULTI;
		pMessageClass = MessageAckMulti.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.ERROR_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.ERROR_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.ERROR_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.ERROR_INITIAL_INT;
		pFlag = MessageFlag.ERROR;
		pMessageClass = MessageError.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.PING_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.PING_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.PING_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.PING_INITIAL_INT;
		pFlag = MessageFlag.PING;
		pMessageClass = MessagePing.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.PING_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.PING_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.PING_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.PING_INITIAL_INT;
		pFlag = MessageFlag.PING_ACK;
		pMessageClass = MessagePingAck.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.PING_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.PING_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.PING_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.PING_INITIAL_INT;
		pFlag = MessageFlag.PING_HIGHEST;
		pMessageClass = MessagePingHighest.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.MIGRATE_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.MIGRATE_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.MIGRATE_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.MIGRATE_INITIAL_INT;
		pFlag = MessageFlag.MIGRATE;
		pMessageClass = MessageMigrate.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.CLIENT_JOIN_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.CLIENT_JOIN_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.CLIENT_JOIN_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.CLIENT_JOIN_INITIAL_INT;
		pFlag = MessageFlag.CLIENT_JOIN;
		pMessageClass = MessageClientJoin.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.CLIENT_DISCONNECTED_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.CLIENT_DISCONNECTED_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.CLIENT_DISCONNECTED_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.CLIENT_DISCONNECTED_INITIAL_INT;
		pFlag = MessageFlag.CLIENT_DISCONNECTED;
		pMessageClass = MessageClientDisconnect.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);

		pGetIntialSize = this.mBaseOptions.getPoolProperties(MessagePoolTags.ENCAPSULATED_INITIAL_STRING);
		pGetGrowth = this.mBaseOptions.getPoolProperties(MessagePoolTags.ENCAPSULATED_GROWTH_STRING);
		pInitialSize = (pGetIntialSize != null) ? pGetIntialSize : MessagePoolTags.ENCAPSULATED_INITIAL_INT;
		pGrowth = (pGetGrowth != null) ? pGetGrowth : MessagePoolTags.ENCAPSULATED_INITIAL_INT;
		pFlag = MessageFlag.ENCAPSULATED;
		pMessageClass = MessageEncapsulated.class;
		this.mMessagePool.registerMessage(pFlag, pMessageClass, pInitialSize, pGrowth);
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
