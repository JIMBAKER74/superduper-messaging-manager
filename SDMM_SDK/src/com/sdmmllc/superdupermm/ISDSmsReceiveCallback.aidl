package com.sdmmllc.superdupermm;

import java.util.List; 
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;

interface ISDSmsReceiveCallback {
	boolean onReceive(inout SDSmsReceiveCallback intent);
	String testCallbackConnection(String text);
}

