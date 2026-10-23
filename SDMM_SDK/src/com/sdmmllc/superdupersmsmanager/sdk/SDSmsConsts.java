package com.sdmmllc.superdupersmsmanager.sdk;

import android.net.Uri;

public class SDSmsConsts {

	public static final Uri SDCONTENT_URI = Uri.parse("content://sdmms-sdsms/");
	public static final Uri SDMMS_URI = Uri.parse("content://sdmms/");
	public static final Uri SDSMS_URI = Uri.parse("content://sdsms/");

	public static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
	public static final Uri MMS_URI = Uri.parse("content://mms/");
	public static final Uri SMS_URI = Uri.parse("content://sms/");
	
	public static final String SDSMS_ID = "SDSmsSdkId";
	public static final String SDSMS_TYPE = "SDSmsSdkType";
	public static final String SDSMS_PACKAGE_NAME = "PackageName";
	public static final String SDSMS_RECEIVER_NAME = "ReceiverName";
	
	public static final String SDSMS_INSTALL_PKG = "com.sdmmllc.superdupermessagingmanager";
	public static final String SDSMS_INTENT_RECEIVER = "com.sdmmllc.superdupermm.SDSmsIntentReceiver";
	public static final String SDSMS_RELOAD_RECEIVER = "com.sdmmllc.superdupermm.SDSmsIntentRegistrationReceiver";
	public static final String SDSMS_RELOAD = "com.sdmmllc.superdupersms.RELOAD_CONNECTION";
	
	public static final String SDSMS_ABORT = "abort";
	public static final String SDSMS_DUPLICATE = "duplicate";
	
	public static final boolean 
		DEBUG_UTILS = false,
		DEBUG_SERVICE = false,
		DEBUG_DEFAULT = false,
		DEBUG_SEND_SMS = false,
		DEBUG_SEND_MMS = false,
		DEBUG_DRAFT = false, 
		DEBUG_INSTALL_RECEIVER = false,
		DEBUG_RECEIVE_SMS = false;
}
