package com.niffy.AndEngineLockStepEngine.threads;

import java.net.InetAddress;

import com.niffy.AndEngineLockStepEngine.messages.MessageError;
import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;

public interface IBaseCommunicationThread extends IHandlerMessage {
	public WeakThreadHandler<IHandlerMessage> getParentHandler();

	public WeakThreadHandler<IHandlerMessage> getHandler();

	public boolean isRunning();

	public boolean isTerminated();

	public void terminate();

	/**
	 * Is the thread ignoring all incoming data?
	 */
	public boolean isIgnoring();

	/**
	 * Allow processing of incoming data. Set to <code>false</code> to allow
	 * processing, <code>true</code> to ignore incoming data.
	 * 
	 * @param pAllow
	 *            {@link Boolean} Allow processing if coming data.
	 */
	public void setIgnoreIncoming(final boolean pAllow);

	public void handleErrorMessage(final InetAddress pAddress, final MessageError pMessage);
}
