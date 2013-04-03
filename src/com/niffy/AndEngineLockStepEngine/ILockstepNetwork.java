package com.niffy.AndEngineLockStepEngine;

import java.net.InetAddress;
import java.util.ArrayList;

import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.tcp.TCPCommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.udp.UDPCommunicationThread;

public interface ILockstepNetwork extends IHandlerMessage {
	/**
	 * Attach {@link TCPCommunicationThread} thread. Must be started!
	 * 
	 * @param pThread
	 */
	public void attachTCPThread(final ICommunicationThread pThread);

	/**
	 * Attach {@link UDPCommunicationThread} thread. Must be started!
	 * 
	 * @param pThread
	 */
	public void attachUDPThread(final ICommunicationThread pThread);

	/**
	 * Set to ignore or allow TCP communications
	 * 
	 * @param pIgnore
	 * @see ICommunicationThread#setIgnoreIncoming(boolean)
	 */
	public void ignoreTCPCommunication(boolean pIgnore);

	/**
	 * Set to ignore or allow UDP communications
	 * 
	 * @param pIgnore
	 * @see ICommunicationThread#setIgnoreIncoming(boolean)
	 */
	public void ignoreUDPCommunication(boolean pIgnore);

	/**
	 * Terminate the TCP thread
	 */
	public void terminateTCPThread();

	/**
	 * Terminate the UDP thread
	 */
	public void terminateUDPThread();

	/**
	 * Indicate to clients we are migrating.
	 */
	public void migrate();

	public void addClient(final InetAddress pAddress);

	public ArrayList<InetAddress> getClients();

	public void removeClient(final InetAddress pAddress);

}
