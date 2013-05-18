package com.niffy.AndEngineLockStepEngine.threads.nio;

import java.net.InetSocketAddress;

import com.niffy.AndEngineLockStepEngine.exceptions.ClientDoesNotExist;
import com.niffy.AndEngineLockStepEngine.exceptions.NotConnectedToClient;
import com.niffy.AndEngineLockStepEngine.threads.IBaseThread;

/**
 * 
 * <b>Extends: </b> {@link IBaseThread}
 * 
 * @author Paul Robinson
 * @since 16 May 2013 15:35:36
 */
public interface ISelectorThread extends IBaseThread {
	/**
	 * 
	 * @param pAddress
	 * @param pData
	 * @throws NotConnectedToClient If client is added but channel is not connected
	 * @throws ClientDoesNotExist If client has never been added to the thread.
	 */
	public void send(final InetSocketAddress pAddress, final byte[] pData) throws NotConnectedToClient,
			ClientDoesNotExist;
}
