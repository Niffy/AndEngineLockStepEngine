package com.niffy.AndEngineLockStepEngine.threads;

import java.net.InetAddress;
import java.util.ArrayList;

import com.niffy.AndEngineLockStepEngine.messages.MessageError;
import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.misc.WeakThreadHandler;
import com.niffy.AndEngineLockStepEngine.packet.ISendMessage;
import com.niffy.AndEngineLockStepEngine.window.IGenericWindowCrisisListener;

public interface ICommunicationThread extends IHandlerMessage, IGenericWindowCrisisListener, ISendMessage {
	public WeakThreadHandler<IHandlerMessage> getParentHandler();

	public WeakThreadHandler<IHandlerMessage> getHandler();
	
	public boolean isRunning();

	public boolean isTerminated();

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

	public void terminate();

	public void addClient(final InetAddress pAddress);

	public ArrayList<InetAddress> getClients();

	public void removeClient(final InetAddress pAddress);

	public void handleErrorMessage(final InetAddress pAddress, final MessageError pMessage);
}
