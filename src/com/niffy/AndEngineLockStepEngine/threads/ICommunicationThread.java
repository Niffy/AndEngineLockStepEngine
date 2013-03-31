package com.niffy.AndEngineLockStepEngine.threads;

import java.net.InetAddress;
import java.util.ArrayList;

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
	 * Allow processing of incoming data. if {@link #isIgnoring()} is
	 * <code>true</code> then it will be set to <code>false</code> to accept
	 * incoming data
	 */
	public void allowProcessing();

	public void terminate();

	public void addClient(final InetAddress pAddress);

	public ArrayList<InetAddress> getClients();

	public void removeClient(final InetAddress pAddress);
}
