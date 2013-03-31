package com.niffy.AndEngineLockStepEngine.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageAck extends Message {
	// ===========================================================
	// Constants
	// ===========================================================
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(MessageAck.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected int mAckFor = 0;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MessageAck() {
		super();
	}

	public MessageAck(final int pIntended) {
		super(pIntended);
	}

	public MessageAck(final int pIntended, final int pFlag, final int pSequenceNumber) {
		super(pIntended, pFlag, pSequenceNumber);
	}

	public MessageAck(final int pFlag, final int pSequenceNumber) {
		super(pFlag, pSequenceNumber);
	}

	public MessageAck(Parcel in) {
		super(in);
		this.mAckFor = in.readInt();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream) throws IOException {
		this.mAckFor = pDataInputStream.readInt();
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mAckFor);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(this.mAckFor);

	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public void setAckForSequnce(int pAckSequence) {
		this.mAckFor = pAckSequence;
	}

	public int getAckForSequence() {
		return this.mAckFor;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	public static final Parcelable.Creator<MessageAck> CREATOR = new Parcelable.Creator<MessageAck>() {

		@Override
		public MessageAck createFromParcel(Parcel source) {
			return new MessageAck(source);
		}

		@Override
		public MessageAck[] newArray(int size) {
			return new MessageAck[size];
		}
	};
}
