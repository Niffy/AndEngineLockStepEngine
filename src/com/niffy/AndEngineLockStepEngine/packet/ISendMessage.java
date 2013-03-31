package com.niffy.AndEngineLockStepEngine.packet;

import java.net.InetAddress;

import com.niffy.AndEngineLockStepEngine.flags.MessageFlag;
import com.niffy.AndEngineLockStepEngine.messages.IMessage;

public interface ISendMessage {
	/**
	 * Send a message to a client
	 * 
	 * @param pAddress
	 *            {@link InetAddress} of client to contact.
	 * @param pMessage
	 *            Message to send, <b>MUST</b> extend {@link IMessage}
	 * @return {@link Integer} of sequence number used when sending the packet.
	 */
	public <T extends IMessage> int sendMessage(final InetAddress pAddress, final T pMessage);

	/**
	 * Obtain a message.
	 * 
	 * @param pFlag
	 *            {@link Integer} of flag from {@link MessageFlag}
	 * @return message which extends {@link IMessage}, to use
	 * @throws MessagePoolException
	 */
	public IMessage obtainMessage(final int pFlag);

	/**
	 * Recycle a message
	 * 
	 * @param pMessage
	 *            {@link IMessage} to put back into the pool
	 */
	public <T extends IMessage> void recycleMessage(final T pMessage);
}
