package com.sdmmllc.superdupersmsmanager;

import android.net.Uri;

public class SdmmsConsts {

	public static final Uri SDCONTENT_URI = Uri.parse("content://sdmms-sdsms/");
	public static final Uri SDMMS_URI = Uri.parse("content://sdmms");
	public static final Uri SDSMS_URI = Uri.parse("content://sdsms");

	public static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
	public static final Uri MMS_URI = Uri.parse("content://mms");
	public static final Uri SMS_URI = Uri.parse("content://sms");
	
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER";

	// temporary
    public static String 
		SCAN_ITEM_ID = "ScanItemName", 
		SCAN_LIST_ID = "ScanListId",
		APP_VERSION = "AppVersion", 
		SPYWARE_VERSION = "SpywareVersion",
		VIEWED_SETUP = "viewed_setup";
    
	public static int 
		SCANLIST_SCREEN = 1,
		WELCOME_SCEEN = 2,
		INITIAL_SCAN_SCREEN = 3,
		INITIAL_SELECT_SCREEN = 4,
		CHANGE_DEF_SMS_SCREEN = 5,
		INITIAL_SCAN_VIEW_SCREEN = 6,
		INITIAL_SCAN_VERIFY_SCREEN = 7;

	// debug flags
	public static boolean 
		PRODUCTION = true,
		DEBUG_DB = false,
		DEBUG_NEW_DB = false,
		DEBUG_SCAN = false,
		DEBUG_SCAN_DB = false,
		DEBUG_SCAN_ENGINE = false,
		DEBUG_COMMS = false,
		DEBUG_XML = false,
		DEBUG_NOTIFICATION = false,
		DEBUG_FILTERS = false,
		DEBUG_SCAN_ACT = false,
		DEBUG_VIEW_ACT = false,
		DEBUG_UTILS = false,
		DEBUG_DRAFT = false,
		DEBUG_SEND_MMS = false,
		DEBUG_MMS_PROVIDER = false,
		DEBUG_SMS_PROVIDER = false,
		DEBUG_MMS_SMS_PROVIDER = false,
		DEBUG_TEMP_FILE = false,
		DEBUG_SCAN_FILTERS = false,
		DEBUG_APP_LIST = false,
		DEBUG_INSTALL = false,
		DEBUG_VERIFY_APPS = false;
	
	// prefs files
    public static final String
    	DB_FILE ="DBFile",
    	SCAN_FILE = "ScanFile",
    	INSTALL_FILE ="InstallFile",
    	scanDone = "need_scan", warnInstall = "warn_install", warnPackage = "warn_package",
    	logEntry = "log_entry", passwordKey = "passwordTxt", wipeKey = "wipeTxt",
    	logKey = "logEntry", licenseChecks = "num_license_chks",
    	deviceID = "deviceId", serialID = "serialId", uuid = "UUID", 
    	installTS = "InstallTS", accountTS = "AccountTS", newInstall = "NewInstall";

    
    // SCAN file keys
    public static final String
    	APP_INFO_VERSION = "AppInfoVersion",
    	SDSMS_VERSION = "SDSmsVersion";
    
}
