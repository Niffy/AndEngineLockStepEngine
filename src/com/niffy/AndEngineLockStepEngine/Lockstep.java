package com.niffy.AndEngineLockStepEngine;


import java.util.ArrayList;

public class Lockstep implements ILockstepEngine{
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	public final long NANOSECOND = 1000000;
	/**
	 * Accumulator to count ticks.
	 */
	protected long mSecondsElapsedAccumulator = 0;
	/**
	 * Listeners to update when the game step changes.
	 */
	protected ArrayList<ILockstepListener> mListeners;
	/**
	 * Current game step
	 */
	protected int mCurrentGameStep = 0;
	/**
	 * Standard TickLength in milliseconds
	 */
	protected long mStandardTickLength = 50;
	/**
	 * Current tick length. {@link #mStandardTickLength} +
	 * {@link #mServerTickLength}
	 */
	protected long mCurrentTickLength = 0;
	/**
	 * {@link #mCurrentTickLength} but in nanoseconds
	 */
	protected long mCurrentTickLengthNanoSeconds = 0;
	/**
	 * At what step the tick length will change.
	 */
	protected int mGameStepChangeOver = 0;
	/**
	 * New tick length to use.
	 */
	protected long mNewTickLength = 0;
	/**
	 * Countdown to start the game.
	 */
	protected long mCountdownTime = 0;
	// ===========================================================
	// Constructors
	// ===========================================================

	public Lockstep() {
		this.mListeners = new ArrayList<ILockstepListener>();
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	@Override
	public void onUpdate(long pNanosecondsElapsed){
		this.mSecondsElapsedAccumulator += pNanosecondsElapsed;
		while(this.mSecondsElapsedAccumulator >= this.mCurrentTickLengthNanoSeconds){
			this.incrementGameStep();
			if(this.mGameStepChangeOver == this.mCurrentGameStep){
				this.tickChangeOver();
			}
			this.mSecondsElapsedAccumulator -= this.mCurrentTickLengthNanoSeconds;
		}
	}
	
	@Override
	public long getCurrentTickLength() {
		return this.mCurrentTickLength;
	}

	@Override
	public long getStandardTicklength() {
		return this.mStandardTickLength;
	}

	@Override
	public int getCurrentStep() {
		return this.mCurrentGameStep;
	}

	@Override
	public void setStandardGameLength(long pStandardGameStepTime) {
		this.mStandardTickLength = pStandardGameStepTime;
	}

	@Override
	public void setChangeOver(int pGameStep, long pStepLength) {
		this.mGameStepChangeOver = pGameStep;
		this.mNewTickLength = pStepLength;
	}

	@Override
	public void setCountDownToStart(long pCountdown) {
		this.mCountdownTime = pCountdown;
	}
	
	@Override
	public void subscribeStepChangeListener(ILockstepListener pLockstepListener) {
		if(!this.mListeners.contains(pLockstepListener)){
			this.mListeners.add(pLockstepListener);
		}
	}

	@Override
	public void unsubscribeStepChangeListener(ILockstepListener pLockstepListener) {
		if(this.mListeners.contains(pLockstepListener)){
			this.mListeners.remove(pLockstepListener);
		}
	}
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================
	private void incrementGameStep(){
		this.mCurrentGameStep++;
		this.informGameStepChange();
	}
	
	private void informGameStepChange(){
		final int count = this.mListeners.size();
		for (int i = 0; i < count; i++) {
			this.mListeners.get(i).lockstepStepChange(this.mCurrentGameStep);
		}
	}
	
	private void tickChangeOver(){
		this.mCurrentTickLength = this.mNewTickLength;
		this.mCurrentTickLengthNanoSeconds = this.mCurrentTickLength * this.NANOSECOND;
	}
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}
