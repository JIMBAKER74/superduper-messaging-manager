package com.sdmmllc.superdupermm.data;

public class SDMessageAction {

	// 
	public static final int 
		// no action
		NO_ACTION = 0,
		
		AUTHORIZED_SEND = 1,
		AUTHORIZED_RECEIVE = 2,
		AUTHORIZED_UPDATE = 3,
		AUTHORIZED_DELETE = 4,
	
		UNAUTHORIZED_SEND = 11,
		UNAUTHORIZED_RECEIVE = 12,
		UNAUTHORIZED_UPDATE = 13,
		UNAUTHORIZED_DELETE = 14,
		
		ATTEMPTED_SEND = 21,
		ATTEMPTED_RECEIVE = 22,
		ATTEMPTED_UPDATE = 23,
		ATTEMPTED_DELETE = 24;

}
