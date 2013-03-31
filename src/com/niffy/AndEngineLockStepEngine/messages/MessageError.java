package com.niffy.AndEngineLockStepEngine.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Parcel;
import android.os.Parcelable;

public class MessageError extends Message {
	// ===========================================================
	// Constants
	// ===========================================================
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(MessageError.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected String mString = "Error:";
	protected int mStringSize;
	protected byte[] mStringData;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MessageError() {
		super();
	}

	public MessageError(final int pIntended) {
		super(pIntended);
	}

	public MessageError(final int pIntended, final int pFlag, final int pSequenceNumber) {
		super(pIntended, pFlag, pSequenceNumber);
	}

	public MessageError(final int pFlag, final int pSequenceNumber) {
		super(pFlag, pSequenceNumber);
	}

	public MessageError(Parcel in) {
		super(in);
		this.mString = in.readString();
		this.mStringSize = in.readInt();
		in.readByteArray(this.mStringData);
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void onReadTransmissionData(DataInputStream pDataInputStream) throws IOException {
		this.mStringSize = pDataInputStream.readInt();
		this.mStringData = new byte[this.mStringSize];
		pDataInputStream.read(this.mStringData, 0, this.mStringSize);
		this.mString = new String(this.mStringData, "utf-8");

	}

	@Override
	protected void onWriteTransmissionData(DataOutputStream pDataOutputStream) throws IOException {
		pDataOutputStream.writeInt(this.mStringSize);
		pDataOutputStream.write(this.mStringData);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(this.mString);
		dest.writeInt(this.mStringSize);
		dest.writeByteArray(this.mStringData);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================
	public void setString(String pString) {
		this.mString = pString;
		try {
			this.mStringData = this.mString.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.mStringSize = this.mStringData.length;
	}

	public String getString() {
		return this.mString;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	public static final Parcelable.Creator<MessageError> CREATOR = new Parcelable.Creator<MessageError>() {

		@Override
		public MessageError createFromParcel(Parcel source) {
			return new MessageError(source);
		}

		@Override
		public MessageError[] newArray(int size) {
			return new MessageError[size];
		}
	};
}
