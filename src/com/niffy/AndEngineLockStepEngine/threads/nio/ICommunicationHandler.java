package com.niffy.AndEngineLockStepEngine.threads.nio;

import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;

public interface ICommunicationHandler extends ICommunicationThread{

	public void setUDPSelectorThread(final ISelectorThread pSelectorThread);
	public void setTCPClientSelectorThread(final ISelectorThread pSelectorThread);
	public void setTCPServerSelectorThread(final ISelectorThread pSelectorThread);
}
