package com.sdmmllc.superdupermm;

import java.util.List; 

interface ISDSmsSendService {
	void sendTextMessage(
			String destinationAddress, String scAddress, String text,
			in PendingIntent sentIntent, in PendingIntent deliveryIntent);
	void sendMultipartTextMessage(
			String destinationAddress, String scAddress, in List<String> parts,
			in List<PendingIntent> sentIntents, in List<PendingIntent> deliveryIntents);
	String testConnection(String str);
}

