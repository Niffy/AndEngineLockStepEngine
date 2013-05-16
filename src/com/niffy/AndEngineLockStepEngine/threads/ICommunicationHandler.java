package com.niffy.AndEngineLockStepEngine.threads;

import com.niffy.AndEngineLockStepEngine.threads.nio.ISelectorThread;

public interface ICommunicationHandler extends ICommunicationThread{

	public void setUDPSelectorThread(final ISelectorThread pSelectorThread);
	public void setTCPClientSelectorThread(final ISelectorThread pSelectorThread);
	public void setTCPServerSelectorThread(final ISelectorThread pSelectorThread);
}
