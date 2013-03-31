package com.niffy.AndEngineLockStepEngine;

import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;

public interface ILockstepNetwork extends IHandlerMessage{
	public void attachTCPThread(final ICommunicationThread pThread);

	public void attachUDPThread(final ICommunicationThread pThread);

}
