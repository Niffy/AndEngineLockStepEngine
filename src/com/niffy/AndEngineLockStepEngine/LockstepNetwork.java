package com.niffy.AndEngineLockStepEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.os.Message;

import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;

public class LockstepNetwork implements ILockstepNetwork{
	// ===========================================================
	// Constants
	// ===========================================================
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(LockstepNetwork.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected final ILockstepEngine mLockstepEngine;
	protected ICommunicationThread mCommunicationThread;
	protected ICommunicationThread mUDP;
	protected ICommunicationThread mTCP;
	// ===========================================================
	// Constructors
	// ===========================================================

	public LockstepNetwork(ILockstepEngine pLockstepEngine) {
		this.mLockstepEngine = pLockstepEngine;
	}
	
	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void attachTCPThread(ICommunicationThread pThread) {
		this.mTCP = pThread;
	}
	
	@Override
	public void attachUDPThread(ICommunicationThread pThread) {
		this.mUDP = pThread;
		this.mLockstepEngine.start();
	}
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	@Override
	public void handlePassedMessage(Message pMessage) {
		
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
