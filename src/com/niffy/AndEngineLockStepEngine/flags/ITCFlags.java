package com.niffy.AndEngineLockStepEngine.flags;

import android.os.Bundle;
import android.os.Message;

import com.niffy.AndEngineLockStepEngine.LockstepNetwork;
import com.niffy.AndEngineLockStepEngine.threads.CommunicationHandler;
import com.niffy.AndEngineLockStepEngine.threads.CommunicationThread;
import com.niffy.AndEngineLockStepEngine.threads.nio.ClientSelector;
import com.niffy.AndEngineLockStepEngine.threads.nio.SelectorFlag;
import com.niffy.AndEngineLockStepEngine.threads.nio.ServerSelector;

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
	/* TODO clean up java doc as some flag instructions are outdated */
	/**
	 * This will be sent from main activity to the {@link LockstepNetwork} to
	 * connect to a host. Bundle will have a string key <code>ip</code> with the
	 * ip to connect to.
	 * 
	 */
	public final static int CONNECT_TO = -10;
	/**
	 * This will be send from {@link TCPCommunicationThread} to
	 * {@link LockstepNetwork} to indicate that a connection has been made. <br>
	 * <b>The main activity should not act upon this flag, other then pass it on
	 * to {@link LockstepNetwork}</b>
	 */
	public final static int CONNECTED_TO_HOST = -11;
	/**
	 * This will be send from {@link TCPCommunicationThread} to
	 * {@link LockstepNetwork} to indicate that a connection has not been made. <br>
	 * <b>The main activity should not act upon this flag, other then pass it on
	 * to {@link LockstepNetwork}</b>
	 */
	public final static int CONNECT_TO_ERROR = -12;
	/**
	 * This will send from {@link CommunicationThread} to the main activity.
	 * This will need to be passed onto {@link LockstepNetwork}<br>
	 * {@link Message#getData()} {@link Bundle} will contain a {@link String}
	 * with the key <code>ip</code> This will contain the IP address of the
	 * client. <br>
	 * <b>The main activity should not act upon this flag, other then pass it on
	 * to {@link LockstepNetwork}</b>
	 */
	public final static int CLIENT_CONNECTED = 0;
	/**
	 * This will send from {@link CommunicationThread} to the main activity.
	 * This will need to be passed onto {@link LockstepNetwork}<br>
	 * {@link Message#getData()} {@link Bundle} will contain a {@link String}
	 * with the key <code>ip</code> This will contain the IP address of the
	 * client. <br>
	 * <b>The main activity should not act upon this flag, other then pass it on
	 * to {@link LockstepNetwork}</b>
	 */
	public final static int CLIENT_DISCONNECTED = 1;
	/**
	 * This will send from {@link CommunicationThread} to the main activity.
	 * This will need to be passed onto {@link LockstepNetwork} <br>
	 * <b>The main activity should not act upon this flag, other then pass it on
	 * to {@link LockstepNetwork}</b>
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
	 * This will be passed from {@link TCPCommunicationThread} to
	 * {@link BaseSocketThread} to shut down the socket
	 */
	public final static int NETWORK_TCP_SHUTDOWN_SOCKET = 12;
	public final static int UDP_INCOMMING = 13;
	/**
	 * This will be passed from the main activity, either for itself or on
	 * behalf of {@link LockstepNetwork}, Subclasses of
	 * {@link CommunicationThread} will have to handle sending the message in
	 * their own way. <br>
	 * {@link Message#getData()} {@link Bundle} will contain a {@link String}
	 * with the key <code>ip</code> This will contain the IP address to who to
	 * send the message to. <br>
	 * An {@link Integer} with the key <code>intended</code> from
	 * {@link IntendedFlag}, for who it is intended for. <br>
	 * {@link Byte} Array with the key <code>data</code>, the encapsulated
	 * message. <br>
	 * {@link Boolean} with the key <code>method</code> <code>true</code> means
	 * TCP <code>false</code> means UDP
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
	public final static int RECIEVE_MESSAGE_LOCKSTEP = 22;
	/**
	 * This will be passed from the main activity to inform of a lockstep
	 * increment.
	 */
	public final static int LOCKSTEP_INCREMENT = 900;
	/**
	 * This will passed from subclass of {@link CommunicationThread} to main
	 * activity to pass onto {@link LockstepNetwork}. This will only happen if
	 * the network gets {@link ErrorCodes#CLIENT_WINDOW_NOT_EMPTY} <br>
	 * {@link Message#getData()} {@link Bundle} will contain a {@link String}
	 * with the key <code>ip</code> This will contain the IP address of the
	 * client. <br>
	 * <b>The main activity should not act upon this flag, other then pass it on
	 * to {@link LockstepNetwork}</b>
	 */
	public final static int CLIENT_WINDOW_NOT_EMPTY = 901;
	/**
	 * This will be passed from the subclasses of {@link CommunicationThread} to
	 * the main activity for it self and the {@link LockstepNetwork}. The main
	 * activity should process this flag after it has handed it to
	 * {@link LockstepNetwork}
	 */
	public final static int NETWORK_ERROR = 1000000;
	/**
	 * This will be passed from {@link BaseSocketThread} to
	 * {@link TCPCommunicationThread} when the connection has closed or an
	 * error. Most likely client disconnect. <br>
	 * {@link Message#getData()} {@link Bundle} will contain a {@link String}
	 * with the key <code>ip</code> This will contain the IP address of the
	 * client. <br>
	 * This should trigger {@link TCPCommunicationThread} to send
	 * {@link #CLIENT_DISCONNECTED} to the main activity
	 */
	public final static int NETWORK_TCP_EXCEPTION = 1000001;
	/**
	 * This will be passed from {@link CommunicationThread} to main activity if
	 * it could not send a message to a client. {@link Message#getData()}
	 * {@link Bundle} will contain a {@link String} with the key <code>ip</code>
	 * This will contain the IP address of the client. <br>
	 * It will also contain a byte array of key <code>data</code> of the
	 * message, this data may be incomplete! <br>
	 * An {@link Integer} with the key <code>error</code> will contain the error
	 * code.
	 */
	public final static int NETWORK_SEND_MESSAGE_FAILURE = 1000002;
	/**
	 * This will be passed from {@link CommunicationThread} to main activity if
	 * it could not receive a message from a client. {@link Message#getData()}
	 * {@link Bundle} will contain a {@link String} with the key <code>ip</code>
	 * This will contain the IP address of the client. <br>
	 * It will also contain a byte array of key <code>data</code> of the
	 * message, this data may be incomplete! <br>
	 * An {@link Integer} with the key <code>error</code> will contain the error
	 * code.
	 */
	public final static int NETWORK_RECIEVE_FAILURE = 1000003;

	/**
	 * int key <code>selector</code> of vale from {@link SelectorFlag}
	 */
	public final static int NETWORK_SELECTER_DEFAULT = 800;
	/**
	 * To be passed from {@link ServerSelector} to {@link CommunicationHandler}.
	 * Should inform {@link ClientSelector} of this to create connection back.
	 * Inform UDP selector to allow and accept connections from this IP.
	 */
	public final static int NEW_CLIENT_CONNECTED = 801;


	/**
	 * All flags that need to be passed to the lockstep engine
	 */
	public final static int[] LOCKSTEP_FLAGS = { CONNECTED_TO_HOST, CONNECT_TO_ERROR, CONNECT_TO_ERROR,
			CLIENT_CONNECTED, CLIENT_DISCONNECTED, CLIENT_ERROR, RECIEVE_MESSAGE_LOCKSTEP, LOCKSTEP_INCREMENT,
			CLIENT_WINDOW_NOT_EMPTY, NETWORK_ERROR, NETWORK_SEND_MESSAGE_FAILURE, NETWORK_RECIEVE_FAILURE };
}
