package com.niffy.AndEngineLockStepEngine;

import java.net.InetAddress;

public interface ILockstepClientListener {
	/*
	 * TODO instead of passing inetaddress, pass as string
	 */
	public void clientConnected(final InetAddress pClient);

	public void clientDisconnected(final InetAddress pClient);

	public void clientError(final InetAddress pClient, final String pMsg);

	public void clientOutOfSync(final InetAddress pClient);
	
	public void migrate();
	
	public void connected();
	
	public void connectError();
	
	public void networkError(final String pError);
}
