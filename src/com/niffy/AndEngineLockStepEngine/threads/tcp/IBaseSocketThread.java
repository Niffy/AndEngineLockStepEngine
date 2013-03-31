package com.niffy.AndEngineLockStepEngine.threads.tcp;

import java.io.IOException;

import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;

public interface IBaseSocketThread{
	public WeakThreadHandler<IHandlerMessage> getHandler();
	public void terminate() throws IOException;
}
