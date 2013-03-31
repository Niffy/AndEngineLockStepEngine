package com.niffy.AndEngineLockStepEngine.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Parcel;
import android.os.Parcelable;

public class MessagePingAck extends Message {
	// ===========================================================
	// Constants
	// ===========================================================
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(MessagePingAck.class);

	// ===========================================================
	// Fields
	// ===========================================================
	// ===========================================================
	// Constructors
	// ===========================================================

	public MessagePingAck() {
		super();
	}

	public MessagePingAck(final int pIntended) {
		super(pIntended);
	}

	public MessagePingAck(final int pIntended, final int pFlag, final int pSequenceNumber) {
		super(pIntended, pFlag, pSequenceNumber);
	}

	public MessagePingAck(final int pFlag, final int pSequenceNumber) {
		super(pFlag, pSequenceNumber);
	}

	public MessagePingAck(Parcel in) {
		super(in);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream) throws IOException {
	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream) throws IOException {
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
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
	public static final Parcelable.Creator<MessagePingAck> CREATOR = new Parcelable.Creator<MessagePingAck>() {

		@Override
		public MessagePingAck createFromParcel(Parcel source) {
			return new MessagePingAck(source);
		}

		@Override
		public MessagePingAck[] newArray(int size) {
			return new MessagePingAck[size];
		}
	};
}
