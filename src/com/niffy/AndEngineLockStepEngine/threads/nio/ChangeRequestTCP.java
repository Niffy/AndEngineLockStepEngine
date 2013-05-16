package com.niffy.AndEngineLockStepEngine.threads.nio;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class ChangeRequestTCP extends ChangeRequest {
	// ===========================================================
	// Constants
	// ===========================================================
	// ===========================================================
	// Fields
	// ===========================================================
	// ===========================================================
	// Constructors
	// ===========================================================

	public ChangeRequestTCP(SocketChannel pSocketChannel, int pType, int pOps, InetSocketAddress pAddress) {
		super(pSocketChannel, pType, pOps, pAddress);
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
