package com.develop.jawin;

/**
 * @author Marcus Nylander
 *
 */
public class IUnkownPtr extends COMPtr {

	static public final GUID proxyIID = IID_IUnknown;
	static public final int iidToken;
	static {
		iidToken = IdentityManager.registerProxy(proxyIID, IUnkownPtr.class);
	}

	/**
	 * 
	 */
	public IUnkownPtr() {
		super();
	}

	public static void init() {}
	
	/**
	 * @param peer
	 * @param unk
	 */
	public IUnkownPtr(int peer, int unk) {
		super(peer, unk);
	}

	public int getGuidToken() {
		return iidToken;
	}
}
