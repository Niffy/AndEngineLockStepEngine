package com.niffy.AndEngineLockStepEngine.flags;

/**
 * Flags to indicate who the message is for
 * 
 * @author Paul Robinson
 * @since 28 Mar 2013 21:05:51
 */
public final class IntendedFlag {
	// ===========================================================
	// Constants
	// ===========================================================
	public final static int LOCKSTEP = 0;
	public final static int CLIENT = 1;
	/**
	 * THIS IS FOR BOTH {@link #LOCKSTEP} and {@link #CLIENT} <br>
	 */
	public final static int LOCKSTEP_CLIENT = 2;
	public final static int NETWORK = 3;
}
