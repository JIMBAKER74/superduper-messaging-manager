package com.sdmmllc.superdupermm;

import java.util.Hashtable;

public class MessagingServiceAdapter extends MessagingServiceListener {
	
	private Hashtable<String, Boolean> responseSent = new Hashtable<String, Boolean>();
	private Hashtable<String, String> testResponse = new Hashtable<String, String>();

	@Override
	public void testSent(String pkgName) {
	}

	@Override
	public void testResponse(String pkgName, String response) {
	}
	
}
