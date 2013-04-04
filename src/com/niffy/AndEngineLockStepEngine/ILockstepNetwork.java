package com.niffy.AndEngineLockStepEngine;

import java.net.InetAddress;
import java.util.ArrayList;

import com.niffy.AndEngineLockStepEngine.misc.IHandlerMessage;
import com.niffy.AndEngineLockStepEngine.packet.ISendMessage;
import com.niffy.AndEngineLockStepEngine.threads.ICommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.tcp.TCPCommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.udp.UDPCommunicationThread;

public interface ILockstepNetwork extends IHandlerMessage, ISendMessage {
	public ICommunicationThread getTCPThread();

	public ICommunicationThread getUDPThread();

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
	 * Set the current main communication thread to use. <br>
	 * After attaching both UDP and TCP threads, set this to the TCP thread.
	 * Calling {@link #migrate()} will then set it to
	 * {@link UDPCommunicationThread}
	 * 
	 * @param pThread
	 */
	public void setMainCommunicationThread(final ICommunicationThread pThread);

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
	
	public boolean allRunning();
	
	public void connectTo(final InetAddress pAddress);

}
