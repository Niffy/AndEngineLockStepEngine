package com.niffy.AndEngineLockStepEngine.flags;

import com.niffy.AndEngineLockStepEngine.LockstepNetwork;
import com.niffy.AndEngineLockStepEngine.threads.CommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.tcp.BaseSocketThread;
import com.niffy.AndEngineLockStepEngine.threads.tcp.TCPCommunicationThread;

/**
 * Flags to indicate packet type
 * 
 * @author Paul Robinson
 * @since 28 Mar 2013 21:05:51
 */
public final class ITCFlags {
	// ===========================================================
	// Constants
	// ===========================================================
	/**
	 * This will send from {@link CommunicationThread} to the main activity.
	 * This will need to be passed onto {@link LockstepNetwork}
	 */
	public final static int CLIENT_CONNECTED = 0;
	/**
	 * This will send from {@link CommunicationThread} to the main activity.
	 * This will need to be passed onto {@link LockstepNetwork}
	 */
	public final static int CLIENT_DISCONNECTED = 1;
	/**
	 * This will send from {@link CommunicationThread} to the main activity.
	 * This will need to be passed onto {@link LockstepNetwork}
	 */
	public final static int CLIENT_ERROR = 2;
	/**
	 * This will be passed from {@link BaseSocketThread} to
	 * {@link TCPCommunicationThread} thread
	 */
	public final static int TCP_CLIENT_INCOMMING = 10;
	/**
	 * This will be passed from {@link TCPCommunicationThread} to
	 * {@link BaseSocketThread}
	 */
	public final static int TCP_CLIENT_OUTGOING = 11;
	/**
	 * This will be passed from the main activity, either for itself or on
	 * behalf of {@link LockstepNetwork}, Subclasses of
	 * {@link CommunicationThread} will have to handle sending the message in
	 * their own way.
	 */
	public final static int SEND_MESSAGE = 20;
	/**
	 * This will be passed from subclass of {@link CommunicationThread} to the
	 * main activity for it to process itself.
	 */
	public final static int RECIEVE_MESSAGE_CLIENT = 21;
	/**
	 * This will be passed from subclass of {@link CommunicationThread} to the
	 * main activity for it to pass on to {@link LockstepNetwork}
	 */
	public final static int RECIEVE_MESSAGE_LOCKSTEP = 21;
	/**
	 * This will be passed from the main activity to inform of a lockstep
	 * increment.
	 */
	public final static int LOCKSTEP_INCREMENT = 900;

}
