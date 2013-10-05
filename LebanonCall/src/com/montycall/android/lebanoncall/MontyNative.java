package com.montycall.android.lebanoncall;

public class MontyNative {
	
	public native static String getPrivateKey();
	
	public native static String getTokenString(String valFromServer);
	
}
