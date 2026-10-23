package com.sdmmllc.superdupermm;

import java.util.List; 
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;

interface ISDSmsReceiveService {
	boolean registerApp(in Intent intent);
	String registerReceiver(String id, int type, in Intent intent, in SDSmsReceiveCallback callback);
	String testConnection(String test);
}

