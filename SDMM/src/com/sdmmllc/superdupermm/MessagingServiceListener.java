package com.sdmmllc.superdupermm;

public abstract class MessagingServiceListener {

	public abstract void testSent(String pkgName);
	public abstract void testResponse(String pkgName, String response);
	
}
