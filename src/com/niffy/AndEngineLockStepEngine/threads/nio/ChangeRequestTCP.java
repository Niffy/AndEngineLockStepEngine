package com.niffy.AndEngineLockStepEngine.threads.nio;

import java.nio.channels.SocketChannel;

public class ChangeRequestTCP {
	// ===========================================================
	// Constants
	// ===========================================================
	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;
	// ===========================================================
	// Fields
	// ===========================================================
	public SocketChannel mSocketChannel;
	public int mType;
	public int mOps;
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public ChangeRequestTCP(SocketChannel pSocketChannel, int pType, int pOps) {
		this.mSocketChannel = pSocketChannel;
		this.mType = pType;
		this.mOps = pOps;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
