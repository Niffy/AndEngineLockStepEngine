package com.niffy.AndEngineLockStepEngine.packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Bundle;
import android.os.Message;

import com.niffy.AndEngineLockStepEngine.flags.ITCFlags;
import com.niffy.AndEngineLockStepEngine.flags.IntendedFlag;
import com.niffy.AndEngineLockStepEngine.flags.MessageFlag;
import com.niffy.AndEngineLockStepEngine.messages.IMessage;
import com.niffy.AndEngineLockStepEngine.messages.MessageAck;
import com.niffy.AndEngineLockStepEngine.messages.MessageAckMulti;
import com.niffy.AndEngineLockStepEngine.options.IBaseOptions;
import com.niffy.AndEngineLockStepEngine.packet.ack.AckWindowManager;
import com.niffy.AndEngineLockStepEngine.packet.ack.IAckWindowManager;
import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;

public class PacketHandler implements IPacketHandler {
	// ===========================================================
	// Constants
	// ===========================================================
	private final Logger log = LoggerFactory.getLogger(PacketHandler.class);

	// ===========================================================
	// Fields
	// ===========================================================
	final protected ICommunicationThread mThread;
	final protected IBaseOptions mBaseOptions;
	final protected IAckWindowManager mAckManager;
	final protected ArrayList<InetAddress> mClients;
	/**
	 * Sequence number for packets
	 */
	protected AtomicInteger mSequence = new AtomicInteger(0);

	// ===========================================================
	// Constructors
	// ===========================================================

	public PacketHandler(final ICommunicationThread pParent, final IBaseOptions pBaseOptions) {
		this.mThread = pParent;
		this.mBaseOptions = pBaseOptions;
		this.mAckManager = new AckWindowManager(this, this.mBaseOptions.getStepsBeforeCrisis(),
				this.mBaseOptions.getAckWindowSize(), this);
		this.mClients = new ArrayList<InetAddress>();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void reconstructData(String pAddress, byte[] pData) {
		try {
			InetAddress addressCast = InetAddress.getByName(pAddress);
			final ByteArrayInputStream bInput = new ByteArrayInputStream(pData);
			DataInputStream dis = new DataInputStream(bInput);
			final int sequence = dis.readInt();
			final boolean requireAck = dis.readBoolean();
			final int intended = dis.readInt();
			final int flag = dis.readInt();
			this.handleIncomingPacket(addressCast, sequence, requireAck, intended, flag, dis, pData);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void windowNotEmpty(InetAddress pAddress) {
		this.mThread.windowNotEmpty(pAddress);
	}

	@Override
	public <T extends IMessage> int sendMessage(InetAddress pAddress, T pMessage) {
		final int pSequence = this.mSequence.getAndIncrement();
		pMessage.setSequence(pSequence);
		if (pMessage.getRequireAck()) {
			this.mAckManager.addAwaitingAck(pAddress, pSequence);
		}
		this.mThread.sendMessage(pAddress, pMessage);
		return pSequence;
	}

	@Override
	public IMessage obtainMessage(int pFlag) {
		return this.mThread.obtainMessage(pFlag);
	}

	@Override
	public <T extends IMessage> void recycleMessage(T pMessage) {
		this.mThread.recycleMessage(pMessage);
	}

	@Override
	public void addClient(InetAddress pAddress) {
		this.mClients.add(pAddress);
		this.mAckManager.addClient(pAddress);
	}

	@Override
	public ArrayList<InetAddress> getClients() {
		return this.mClients;
	}

	@Override
	public void removeClient(InetAddress pAddress) {
		this.mClients.remove(pAddress);
		this.mAckManager.removeClient(pAddress);
	}

	@Override
	public void lockstepIncrement(int pIncrement) {
		this.mAckManager.slide(pIncrement);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	protected void handleIncomingPacket(final InetAddress pFrom, final int pSequence, final boolean pRequireAck,
			final int pIntended, final int pFlag, final DataInputStream pDataInput, final byte[] pData)
			throws IOException {
		if (!pRequireAck) {
			/* An ack is not required for this message so carry on processing */
			if (pFlag == MessageFlag.ACK) {
				final int pAckFor = this.extractAckForSequence(pFrom, pSequence, pRequireAck, pIntended, pFlag,
						pDataInput, pData);
				if (pAckFor != -1) {
					this.mAckManager.processReceivedAck(pFrom, pAckFor);
				} else {
					log.error("Could not get ack for from sequence: {} from: {}", pSequence, pFrom);
				}
			} else if (pFlag == MessageFlag.ACK_MULTI) {
				final int[] pAckFor = this.extractAcksForSequence(pFrom, pSequence, pRequireAck, pIntended, pFlag,
						pDataInput, pData);
				if (pAckFor != null) {
					this.mAckManager.processReceivedAck(pFrom, pAckFor);
				} else {
					log.error("Could not get acks for from sequence: {} from: {}", pSequence, pFrom);
				}
			}
		} else {
			/* An ack is required so send one! */
			MessageAck ack = (MessageAck) this.obtainMessage(MessageFlag.ACK);
			ack.setAckForSequnce(pSequence);
			ack.setIntended(IntendedFlag.NETWORK);
			ack.setRequireAck(false);
			this.mAckManager.addSentAck(pFrom, pSequence);
			this.sendMessage(pFrom, ack);
			this.recycleMessage(ack);
		}
		if (pIntended == IntendedFlag.CLIENT) {
			/* Packet is intended for client, pass back to thread to pass on */
			this.passToClient(pFrom, pSequence, pRequireAck, pIntended, pFlag, pDataInput, pData);
		} else if (pIntended == IntendedFlag.LOCKSTEP) {
			/* Packet is intended for lockstep, pass back to thread to pass on */
			this.passToLockstep(pFrom, pSequence, pRequireAck, pIntended, pFlag, pDataInput, pData);
		} else if (pIntended == IntendedFlag.LOCKSTEP_CLIENT) {
			/* Packet is for lockstep and client! */
			this.passToLockstep(pFrom, pSequence, pRequireAck, pIntended, pFlag, pDataInput, pData);
			this.passToClient(pFrom, pSequence, pRequireAck, pIntended, pFlag, pDataInput, pData);
		} else if (pIntended == IntendedFlag.NETWORK) {
			/* Packet is intended for network, but we only deal with low level stuff! */
			if (pFlag != MessageFlag.ACK || pFlag != MessageFlag.ACK_MULTI) {
				log.warn("Unknown message intended for network to handle");
			}
		} else {
			Object[] pError = { pIntended, pFlag, pFrom };
			log.warn("Unknown intended recipient: {0} Message Flag: {1} From: {2}", pError);
		}
	}

	protected void passToClient(final InetAddress pFrom, final int pSequence, final boolean pRequireAck,
			final int pIntended, final int pFlag, final DataInputStream pDataInput, final byte[] pData) {
		Message msg = this.mThread.getParentHandler().obtainMessage();
		msg.what = ITCFlags.RECIEVE_MESSAGE_CLIENT;
		Bundle bundle = new Bundle();
		bundle.putString("ip", pFrom.toString());
		bundle.putByteArray("data", pData);
		msg.setData(bundle);
		this.mThread.getParentHandler().sendMessage(msg);
	}

	protected void passToLockstep(final InetAddress pFrom, final int pSequence, final boolean pRequireAck,
			final int pIntended, final int pFlag, final DataInputStream pDataInput, final byte[] pData) {
		Message msg = this.mThread.getParentHandler().obtainMessage();
		msg.what = ITCFlags.RECIEVE_MESSAGE_LOCKSTEP;
		Bundle bundle = new Bundle();
		bundle.putString("ip", pFrom.toString());
		bundle.putByteArray("data", pData);
		msg.setData(bundle);
		this.mThread.getParentHandler().sendMessage(msg);
	}

	protected int extractAckForSequence(final InetAddress pFrom, final int pSequence, final boolean pRequireAck,
			final int pIntended, final int pFlag, final DataInputStream pDataInput, final byte[] pData) {
		int found = -1;
		MessageAck ack = (MessageAck) this.obtainMessage(pFlag);
		try {
			ack.read(pDataInput);
			found = ack.getAckForSequence();
			this.recycleMessage(ack);
		} catch (IOException e) {
			log.error("Could not read in ack for: {} from: {}", pSequence, pFrom);
			log.error("Error: ", e);
		}
		return found;
	}

	protected int[] extractAcksForSequence(final InetAddress pFrom, final int pSequence, final boolean pRequireAck,
			final int pIntended, final int pFlag, final DataInputStream pDataInput, final byte[] pData) {
		int[] found = null;
		MessageAckMulti ack = (MessageAckMulti) this.obtainMessage(pFlag);
		try {
			ack.read(pDataInput);
			found = ack.getSequences();
			this.recycleMessage(ack);
		} catch (IOException e) {
			log.error("Could not read in ack for: {} from: {}", pSequence, pFrom);
			log.error("Error: ", e);
		}
		return found;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
