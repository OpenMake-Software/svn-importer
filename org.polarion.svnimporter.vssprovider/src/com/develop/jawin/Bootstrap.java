package com.develop.jawin;

/**
 * @author Marcus Nylander
 *
 */
public class Bootstrap {

	public static void init() {}

	static native int loadLibrary(String s) throws COMException;

	static native int loadFunction(int i, String s) throws COMException;

	static native void freeLibrary(int i);

	static native void revokeGIT(int i);

	static native int registerGUID(byte abyte0[]);

	static native int queryInterface(int i, int j);

	static native int unmarshalFromGIT(int i, int j);

	static native int marshalToGIT(int i, int j);

	static native int directCOM(int i, int j);

	static {
		DispatchPtr.init();
		IEnumVariant.init();
		IUnkownPtr.init();
	}
}
