package com.niffy.AndEngineLockStepEngine.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Parcel;
import android.os.Parcelable;

public class MessagePing extends Message {
	// ===========================================================
	// Constants
	// ===========================================================
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(MessagePing.class);

	// ===========================================================
	// Fields
	// ===========================================================
	// ===========================================================
	// Constructors
	// ===========================================================

	public MessagePing() {
		super();
	}

	public MessagePing(final int pIntended) {
		super(pIntended);
	}

	public MessagePing(final int pIntended, final int pFlag, final int pSequenceNumber) {
		super(pIntended, pFlag, pSequenceNumber);
	}

	public MessagePing(final int pFlag, final int pSequenceNumber) {
		super(pFlag, pSequenceNumber);
	}

	public MessagePing(Parcel in) {
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
	public static final Parcelable.Creator<MessagePing> CREATOR = new Parcelable.Creator<MessagePing>() {

		@Override
		public MessagePing createFromParcel(Parcel source) {
			return new MessagePing(source);
		}

		@Override
		public MessagePing[] newArray(int size) {
			return new MessagePing[size];
		}
	};
}
