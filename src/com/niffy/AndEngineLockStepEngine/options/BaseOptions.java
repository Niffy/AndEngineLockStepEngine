package com.niffy.AndEngineLockStepEngine.options;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseOptions implements IBaseOptions {
	// ===========================================================
	// Constants
	// ===========================================================
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(BaseOptions.class);

	// ===========================================================
	// Fields
	// ===========================================================
	protected InetAddress mHostIP;
	protected int mBufferSize = 512;
	protected int mTCPPort = 9999;
	protected int mUDPPort = 9998;
	protected int mStepsBeforeCrisis = 0;
	protected long mStandardTickLength = 0;
	protected int mAckWindowSize = 0;
	protected HashMap<String, Integer> mMessagePoolOptions;
	protected int mVersion = -1;
	protected long mPingRTT = 0;
	protected String mClientName = "default";

	// ===========================================================
	// Constructors
	// ===========================================================

	public BaseOptions() {
		this.mMessagePoolOptions = new HashMap<String, Integer>();
	}

	public BaseOptions(final IBaseOptions pBaseOptions) {
		this();
		this.mHostIP = pBaseOptions.getHostIP();
		this.mTCPPort = pBaseOptions.getTCPPort();
		this.mUDPPort = pBaseOptions.getUDPPort();
		this.mStepsBeforeCrisis = pBaseOptions.getStepsBeforeCrisis();
		this.mStandardTickLength = pBaseOptions.getStandardTickLength();
		this.mAckWindowSize = pBaseOptions.getAckWindowSize();
		this.mVersion = pBaseOptions.getVersionNumber();
		this.mBufferSize = pBaseOptions.getNetworkBufferSize();
		this.mPingRTT = pBaseOptions.getPingRTT();
		this.mClientName = pBaseOptions.getClientName();
		Iterator<Entry<String, Integer>> entries = pBaseOptions.getMessagePoolProperties().entrySet().iterator();
		while (entries.hasNext()) {
			Entry<String, Integer> entry = entries.next();
			this.mMessagePoolOptions.put(entry.getKey(), entry.getValue());
		}
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void setStepsBeforeCrisis(int pStepsBeforeCrisis) {
		this.mStepsBeforeCrisis = pStepsBeforeCrisis;
	}

	@Override
	public int getStepsBeforeCrisis() {
		return this.mStepsBeforeCrisis;
	}

	@Override
	public void setStandardTickLength(long pStepLength) {
		this.mStandardTickLength = pStepLength;
	}

	@Override
	public long getStandardTickLength() {
		return this.mStandardTickLength;
	}

	@Override
	public void setAckWindowSize(int pWindowSize) {
		this.mAckWindowSize = pWindowSize;
	}

	@Override
	public int getAckWindowSize() {
		return this.mAckWindowSize;
	}

	@Override
	public void setVersionNumber(int pVersionNumber) {
		this.mVersion = pVersionNumber;
	}

	@Override
	public int getVersionNumber() {
		return this.mVersion;
	}

	@Override
	public void setNetworkBufferSize(int pSize) {
		this.mBufferSize = pSize;
	}

	@Override
	public int getNetworkBufferSize() {
		return this.mBufferSize;
	}

	@Override
	public void setHostIP(InetAddress pHostIP) {
		this.mHostIP = pHostIP;
	}

	@Override
	public InetAddress getHostIP() {
		return this.mHostIP;
	}

	@Override
	public void setTCPPort(int pTCPPort) {
		this.mTCPPort = pTCPPort;
	}

	@Override
	public int getTCPPort() {
		return this.mTCPPort;
	}

	@Override
	public void setUDPPort(int pUDPPort) {
		this.mUDPPort = pUDPPort;
	}

	@Override
	public int getUDPPort() {
		return this.mUDPPort;
	}

	@Override
	public void setPingRTT(long pDuration) {
		this.mPingRTT = pDuration;
	}

	@Override
	public long getPingRTT() {
		return this.mPingRTT;
	}

	@Override
	public void setClientName(String pClientName) {
		this.mClientName = pClientName;
	}

	@Override
	public String getClientName() {
		return this.mClientName;
	}

	@Override
	public void addPoolProperties(String pTag, int pValue) {
		this.mMessagePoolOptions.put(pTag, pValue);
	}

	@Override
	public int getPoolProperties(String pTag) {
		return this.mMessagePoolOptions.get(pTag);
	}

	@Override
	public HashMap<String, Integer> getMessagePoolProperties() {
		return this.mMessagePoolOptions;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}