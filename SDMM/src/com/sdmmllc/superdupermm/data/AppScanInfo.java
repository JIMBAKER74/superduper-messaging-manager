package com.sdmmllc.superdupermm.data;

import java.util.Calendar;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.sdmmllc.superdupermm.scan.AppUsesPermissions;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class AppScanInfo {
	
	private String TAG = "PkgScanItem";
	
	public PackageInfo pkgPerms;	
		
	public static final String SCAN_ITEMS_TBL = "ScanItem";
	
	public static final String[] SCAN_ITEMS_COLUMNS = {
		"_id",
    	"si_scan_ID",
    	"si_package_ID",
        "si_version_CD", 
        "si_category_ID", 
        "si_list_TS", // 5
        "si_name_TXT", 
        "si_app_name_TXT", 
        "si_version_name_TXT", 
        "si_shared_user_ID", 
        "si_shared_user_LBL", // 10
        "si_install_TS", 
        "si_update_TS",    
        "si_uninstall_TS",  
        "si_entry_TS",       
		"si_sms_receiver_priority_num", // 15
	    "si_risk_category_Flg", 
		"si_assigned_perm_type", 
		"si_sds_priority",  
		"si_user_priority",
		"si_feature_flgs",  // 20
        "si_flags",    
        "si_user_flags",
        "si_user_TS",  
        "si_sms_received_receiver",
        "si_sms_delivered_receiver", // 25
        "si_wap_push_deliver_receiver",
	    "si_sms_respond_via_message",
        "si_sms_send",
        "si_wap_send_to",
        "si_sms_to", // 30
        "si_sms",
        "si_mms_to",
        "si_mms",

	};
	
	public static final int
		_id = 							0, 
    	si_scan_ID = 					1,
    	si_package_ID = 				2,
        si_version_CD = 				3,
        si_category_ID = 				4, 
        si_list_TS = 					5,
        si_pkg_name_TXT = 				6,
        si_app_name_TXT = 				7,  
        si_version_name_TXT =			8, 
        si_shared_user_ID = 			9,
        si_shared_user_LBL =			10,
        si_install_TS = 				11, 
        si_update_TS =     				12,
        si_uninstall_TS =   			13,
        si_entry_TS =  					14,
		si_sms_receiver_priority_num = 	15, 
	    si_risk_category_Flg = 			16,
		si_assigned_perm_type = 		17,
		si_sds_priority = 				18,
		si_user_priority = 				19,
		si_feature_flgs =  				20,
        si_flags =     					21,
        si_user_flags = 				22,
        si_user_TS =   					23,
	    si_sms_received_receiver =		24,
	    si_sms_delivered_receiver = 	25,
	    si_wap_push_deliver_receiver = 	26,
	    si_sms_respond_via_message = 	27,
	    si_sms_send = 					28,
	    si_sms_send_to = 				29,
	    si_sms_to = 					30,
	    si_sms = 						31,
		si_mms_to = 					32,
		si_mms = 						33;
	
	public static final String ALL_DB_COLUMNS =
		AppScanInfo.SCAN_ITEMS_COLUMNS[_id] + " INTEGER primary key autoincrement, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_scan_ID] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_package_ID] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_version_CD] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_category_ID] + " INTEGER, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_list_TS] + " LONG NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_pkg_name_TXT] + " TEXT NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_app_name_TXT] + " TEXT NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_version_name_TXT] + " TEXT NOT NULL, " + 
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_shared_user_ID] + " LONG NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_shared_user_LBL] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_install_TS] + " LONG NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_update_TS] + " LONG, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_uninstall_TS] + " LONG, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_entry_TS] + " LONG NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_receiver_priority_num] + " INTEGER, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_risk_category_Flg] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_assigned_perm_type] + " INTEGER, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sds_priority] + " INTEGER, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_user_priority] + " INTEGER, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_feature_flgs] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_flags] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_user_flags] + " INTEGER NOT NULL, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_user_TS] + " LONG, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_received_receiver] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_delivered_receiver] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_wap_push_deliver_receiver] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_respond_via_message] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_send] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_send_to] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_to] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_mms_to] + " TEXT, " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_mms] + " TEXT " ;
	
	public static final String ALL_COLUMNS =
		AppScanInfo.SCAN_ITEMS_COLUMNS[_id] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_scan_ID] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_package_ID] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_version_CD] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_category_ID] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_list_TS] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_pkg_name_TXT] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_app_name_TXT] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_version_name_TXT] + ", " + 
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_shared_user_ID] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_shared_user_LBL] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_install_TS] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_update_TS] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_uninstall_TS] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_entry_TS] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_receiver_priority_num] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_risk_category_Flg] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_assigned_perm_type] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sds_priority] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_user_priority] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_feature_flgs] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_flags] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_user_flags] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_user_TS] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_received_receiver] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_delivered_receiver] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_wap_push_deliver_receiver] + ", "+
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_respond_via_message] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_send] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_send_to] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms_to] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_sms] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_mms_to] + ", " +
		AppScanInfo.SCAN_ITEMS_COLUMNS[si_mms];

	public int 
		_ID = 0,
		pkgId = 0, 
		versionCode = 0, 
		appCatId = 0,
		appCats = 0, 
		recPriority = 0, 
		userFlgs = 0, 
		appListId = 0, 
		scanListId = 0, 
		assigned_perm_type = 0,
		sds_priority = 0,
		user_priority = 0,
		pkgFlgs = 0,
		featureFlgs = 0, 
		smsReceiverPriority = 0;

	
    public String 
    	appName = "", 
    	pkgName = "", 
    	versionName = "", 
    	permFound = "",
    	reqPermFound = "", 
    	pkgType = "", 
    	pkgCreatedBy = "", 
    	pkgOtherNames = "",
    	riskDescr = "", 
    	recommendAction = "", 
    	sharedUserId = "", 
    	sharedUserLbl = "",
	    smsReceivedReceiver = "",
	    smsDeliveredReceiver = "",
	    wapPushDeliverReceiver = "",
	    smsRespondViaMessage = "",
	    smsSend = "",
	    smsSendTo = "",
	    smsTo = "",
	    sms = "",
	    mmsTo = "",
	    mms = "";
    
    public AppUsesPermissions usesPerms;
    public Drawable icon;
    public long 
    	installDt = 0, 
    	entryDt = 0, 
    	updateDt = 0, 
    	userDt = 0, 
    	listDt = 0, 
    	uninstallDt = 0;
    
    public boolean installedApp  = false;
    
    //public Hashtable<String, String> filtersFound; 
    
	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);

    public void print(String TAG2) {
        Log.i(TAG2, TAG
        	+ " appName: " + appName + " - pkgName: " + pkgName + "\n" 
        	+ " versionName: " + versionName + " versionCode: " + versionCode + "\n"
        	+ " listDt: " + listDt + " userDt: " + userDt + "\n"
        	);
    }
	
    public void print() {
        Log.i(TAG, appName + "\t" + pkgName + "\t" + versionName + "\t" + versionCode);
    }
	
    public AppScanInfo () {
    	//filtersFound = new Hashtable<String, String>();
    	usesPerms = new AppUsesPermissions();
    }
    
    private void setUsrDate() {
    	userDt = Calendar.getInstance().getTimeInMillis();
    }
    
    // CATEGORY FLAGS
	public static int 
		SAFE = 1 << 1, 
		UNKNOWN = 1 << 2, 
		UNSAFE = 1 << 3, 
		UNKNOWN_HIGH_RISK = 1 << 4,
		WATCH_LIST = 1 << 5,
		NEW_INSTALL = 1 << 6;
	
	public static int
		TYPE_SYS_APP 			= 1 << 7,
		TYPE_REGISTERED_APP 	= 1 << 8,
		TYPE_SYSRESERVED		= 1 << 9,
		TYPE_SECURE				= 1 << 10,
		TYPE_SMS_PREMIUM		= 1 << 11,
		TYPE_ANTISPAM 			= 1 << 12,
		TYPE_PLUGIN 			= 1 << 13,
		TYPE_AUTORESPONDER 		= 1 << 14,
		TYPE_SMS_RECEIVED 		= 1 << 15,
		TYPE_MESSAGING_PREMIUM	= 1 << 16,
		TYPE_MESSAGING			= 1 << 17,
		TYPE_BACKUP				= 1 << 18,
		TYPE_UNREGISTERED		= 1 << 19,
		TYPE_OBSERVER			= 1 << 20;
	
	public static int 
		CATEGORY_SMS = 1 << 25, 
		CATEGORY_PHONE = 1 << 26,
		CATEGORY_LOC = 1 << 27,
		CATEGORY_EMAIL = 1 << 28,
		CATEGORY_CAMERA = 1 << 29,
		CATEGORY_MIC = 1 << 30,
		CATEGORY_FILES = 1 << 31;
	
	public static int 
		USR_SAFE = 1 << 1,
		USR_UNSAFE = 1 << 2,
		USR_WATCH_LIST = 1 << 3;
	
	// PACKAGE FLAGS
	public static int
		UNINSTALLED = 1 << 8,
		INSTALLED = 1 << 9,
	
		SMS_RECEIVED = 1 << 10,
		SMS_DELIVERED = 1 << 11,
		SMS_KITKAT = 1 << 12,
		SMS_PRE_KITKAT = 1 << 13,
		SMS_DEFAULT = 1 << 14,
		SMS_REGISTERED = 1 << 15,
		SMS_BLOCK_REGISTERED = 1 << 16,
		SMS_APP = 1 << 17,
		SMS_OTHER = 1 << 18,
		SMS_NONE = 1 << 19,
		
		RESPOND_VIA_MSG = 1 << 20,
		RESPOND_VIA_MSG_DEFAULT = 1 << 21,
		SMS_SEND = 1 << 22,
		SMS_SEND_DEFAULT = 1 << 23,
		SMS_SENDTO = 1<< 24,
		SMS_SENDTO_DEFAULT = 1 << 25,
		MMS_SCHEME = 1 << 26,
		MMSTO_SCHEME = 1 << 27,
		SMS_SCHEME = 1 << 28,
		SMSTO_SCHEME = 1 << 29;

	// FEATURE FLAGS
	public static final int 
		// user has assigned priorities
		user_assigned_priority = 1 << 0,
		
		// use user assigned priority
		use_user_assigned_priority = 1 << 1,
		
		// rebroadcast SMS_RECEIVED to app to verify blocked message 
		rebroadcast_sms_received = 1 << 2,
		
		// rebroadcast SMS_DELIVERED to app to verify blocked message
		rebroadcast_sms_delivered = 1 << 3,
		
		// app is designed for KitKat text message default based on intent filters
		kitkat_sms_default_eligible = 1 << 4,
		
		// app listens for KitKat SMS_DELIVERED intent
		listens_sms_delivered = 1 << 5;
	
	public boolean getRespondViaMessage() {
		return (pkgFlgs & RESPOND_VIA_MSG) == RESPOND_VIA_MSG;
	}

	public void setRespondViaMessage(boolean respondViaMsg) {
		if (respondViaMsg) pkgFlgs |= RESPOND_VIA_MSG;
		else pkgFlgs = pkgFlgs & ~RESPOND_VIA_MSG;
	}

	public boolean getRespondViaMessageDefault() {
		return (pkgFlgs & RESPOND_VIA_MSG_DEFAULT) == RESPOND_VIA_MSG_DEFAULT;
	}

	public void setRespondViaMessageDefault(boolean respondViaMsgDefault) {
		if (respondViaMsgDefault) pkgFlgs |= RESPOND_VIA_MSG_DEFAULT;
		else pkgFlgs = pkgFlgs & ~RESPOND_VIA_MSG_DEFAULT;
	}

	public boolean getSmsSend() {
		return (pkgFlgs & SMS_SEND) == SMS_SEND;
	}

	public void setSmsSend(boolean smsSend) {
		if (smsSend) pkgFlgs |= SMS_SEND;
		else pkgFlgs = pkgFlgs & ~SMS_SEND;
	}

	public boolean getSmsSendDefault() {
		return (pkgFlgs & SMS_SEND_DEFAULT) == SMS_SEND_DEFAULT;
	}

	public void setSmsSendDefault(boolean respondViaMsgDefault) {
		if (respondViaMsgDefault) pkgFlgs |= SMS_SEND_DEFAULT;
		else pkgFlgs = pkgFlgs & ~SMS_SEND_DEFAULT;
	}

	public boolean getSmsSendto() {
		return (pkgFlgs & SMS_SENDTO) == SMS_SENDTO;
	}

	public void setSmsSendto(boolean smsSendto) {
		if (smsSendto) pkgFlgs |= SMS_SENDTO;
		else pkgFlgs = pkgFlgs & ~SMS_SENDTO;
	}

	public boolean getSmsSendtoDefault() {
		return (pkgFlgs & SMS_SENDTO_DEFAULT) == SMS_SENDTO_DEFAULT;
	}

	public void setSmsSendtoDefault(boolean smsSendto) {
		if (smsSendto) pkgFlgs |= SMS_SENDTO_DEFAULT;
		else pkgFlgs = pkgFlgs & ~SMS_SENDTO_DEFAULT;
	}

	public boolean getSmsScheme() {
		return (pkgFlgs & SMS_SCHEME) == SMS_SCHEME;
	}

	public void setSmsScheme(boolean smsScheme) {
		if (smsScheme) pkgFlgs |= SMS_SCHEME;
		else pkgFlgs = pkgFlgs & ~SMS_SCHEME;
	}

	public boolean getSmstoScheme() {
		return (pkgFlgs & SMSTO_SCHEME) == SMSTO_SCHEME;
	}

	public void setSmstoScheme(boolean smstoScheme) {
		if (smstoScheme) pkgFlgs |= SMSTO_SCHEME;
		else pkgFlgs = pkgFlgs & ~SMSTO_SCHEME;
	}

	public boolean getMmsScheme() {
		return (pkgFlgs & MMS_SCHEME) == MMS_SCHEME;
	}

	public void setMmsScheme(boolean mmsScheme) {
		if (mmsScheme) pkgFlgs |= MMS_SCHEME;
		else pkgFlgs = pkgFlgs & ~MMS_SCHEME;
	}

	public boolean getMmstoScheme() {
		return (pkgFlgs & MMSTO_SCHEME) == MMSTO_SCHEME;
	}

	public void setMmstoScheme(boolean mmstoScheme) {
		if (mmstoScheme) pkgFlgs |= MMSTO_SCHEME;
		else pkgFlgs = pkgFlgs & ~MMSTO_SCHEME;
	}

	public int getFeatureFlgs() {
		return featureFlgs;
	}
	
	public void setFeatureFlgs(int featureFlgs) {
		this.featureFlgs = featureFlgs;
	}
	
	public int getSmsReceiverPriority() {
		return smsReceiverPriority;
	}
	
	public void setSmsReceiverPriority(int smsReceiverPriority) {
		this.smsReceiverPriority = smsReceiverPriority;
	}
	
	public boolean getUserAssignedPriority() {
		return (featureFlgs & user_assigned_priority) == user_assigned_priority;
	}
	
	public void setUserAssignedPriority(boolean userAssignedPriority) {
		if (userAssignedPriority) featureFlgs |= user_assigned_priority;
		else featureFlgs = featureFlgs & ~user_assigned_priority;
	}
	
	public boolean getUseUserAssignedPriority() {
		return (featureFlgs & use_user_assigned_priority) == use_user_assigned_priority;
	}
	
	public void setUseUserAssignedPriority(boolean useUserAssignedPriority) {
		if (useUserAssignedPriority) featureFlgs |= use_user_assigned_priority;
		else featureFlgs = featureFlgs & ~use_user_assigned_priority;
	}
	
	public boolean getRebroadcastSmsReceived() {
		return (featureFlgs & rebroadcast_sms_received) == rebroadcast_sms_received;
	}
	
	public void setRebroadcastSmsReceived(boolean rebroadcastSmsReceived) {
		if (rebroadcastSmsReceived) featureFlgs |= rebroadcast_sms_received;
		else featureFlgs = featureFlgs & ~rebroadcast_sms_received;
	}
	
	public boolean getRebroadcastSmsDelivered() {
		return (featureFlgs & rebroadcast_sms_delivered) == rebroadcast_sms_delivered;
	}
	
	public void setRebroadcastSmsDelivered(boolean rebroadcastSmsDelivered) {
		if (rebroadcastSmsDelivered) featureFlgs |= rebroadcast_sms_delivered;
		else featureFlgs = featureFlgs & ~rebroadcast_sms_delivered;
	}
	
	public boolean getKitkatSmsDefaultEligible() {
		return (featureFlgs & kitkat_sms_default_eligible) == kitkat_sms_default_eligible;
	}
	
	public void setKitkatSmsDefaultEligible(boolean kitkatSmsDefaultEligible) {
		if (kitkatSmsDefaultEligible) featureFlgs |= kitkat_sms_default_eligible;
		else featureFlgs = featureFlgs & ~kitkat_sms_default_eligible;
	}
	
	public boolean getListensSmsDelivered() {
		return (featureFlgs & listens_sms_delivered) == listens_sms_delivered;
	}
	
	public void setListensSmsDelivered(boolean listensSmsDelivered) {
		if (listensSmsDelivered) featureFlgs |= listens_sms_delivered;
		else featureFlgs = featureFlgs & ~listens_sms_delivered;
	}
	
	public void setFilterSMSReceived(boolean sms_received) {
		if (sms_received) pkgFlgs |= SMS_RECEIVED;
		else pkgFlgs &= ~SMS_RECEIVED;
	}
	
	public boolean hasFilterSMSReceived() {
		return ((pkgFlgs & SMS_RECEIVED) == SMS_RECEIVED);
	}
	
	public void setFilterSMSDelivered(boolean sms_delivered) {
		if (sms_delivered) pkgFlgs |= SMS_DELIVERED;
		else pkgFlgs &= ~SMS_DELIVERED;
	}
	
	public boolean hasFilterSMSDelivered() {
		return ((pkgFlgs & SMS_DELIVERED) == SMS_DELIVERED);
	}
	
	public void setKitKatSMS(boolean sms_kikat) {
		if (sms_kikat) pkgFlgs |= SMS_KITKAT;
		else pkgFlgs &= ~SMS_KITKAT;
	}
	
	public boolean isKitKatSMS() {
		return ((pkgFlgs & SMS_KITKAT) == SMS_KITKAT);
	}
	
	public void setKitKatDefaultSMS(boolean sms_default) {
		if (sms_default) pkgFlgs |= SMS_DEFAULT;
		else pkgFlgs &= ~SMS_DEFAULT;
	}
	
	public boolean isKitKatDefaultSMS() {
		return ((pkgFlgs & SMS_DEFAULT) == SMS_DEFAULT);
	}
	
	public void setPreKitKatSMS(boolean sms_prekikat) {
		if (sms_prekikat) pkgFlgs |= SMS_PRE_KITKAT;
		else pkgFlgs &= ~SMS_PRE_KITKAT;
	}
	
	public boolean isPreKitKatSMS() {
		return ((pkgFlgs & SMS_PRE_KITKAT) == SMS_PRE_KITKAT);
	}
	
	public boolean isInstalledApp() {
		return ((pkgFlgs & INSTALLED) == INSTALLED);
	}
	
	public void setInstalledApp(boolean isInstalledApp) {
		if (isInstalledApp) pkgFlgs |= INSTALLED;
		else pkgFlgs &= ~INSTALLED;
	}
	
	public boolean isUninstalled() {
		return ((pkgFlgs & UNINSTALLED) == UNINSTALLED);
	}
	
	public void setUNINSTALLED(boolean uninstalled) {
		if (uninstalled) pkgFlgs |= UNINSTALLED;
		else pkgFlgs &= ~UNINSTALLED;
	}
	

	/**
	 *  App Categories and Types
	 */

	public void setTypeRegistered(boolean type_registered) {
		if (type_registered) appCats |= TYPE_REGISTERED_APP;
		else appCats &= ~TYPE_REGISTERED_APP;
	}
	
	public boolean isTypeRegistered() {
		return ((appCats & TYPE_REGISTERED_APP) == TYPE_REGISTERED_APP);
	}

	public void setTypeSysReserved(boolean type_res) {
		if (type_res) appCats |= TYPE_SYSRESERVED;
		else appCats &= ~TYPE_SYSRESERVED;
	}
	
	public boolean isTypeSysReserved() {
		return ((appCats & TYPE_SYSRESERVED) == TYPE_SYSRESERVED);
	}

	public void setTypeSecure(boolean type_secure) {
		if (type_secure) appCats |= TYPE_SECURE;
		else appCats &= ~TYPE_SECURE;
	}
	
	public boolean isTypeSecure() {
		return ((appCats & TYPE_SECURE) == TYPE_SECURE);
	}

	public void setTypeSMSPremium(boolean sms_premium) {
		if (sms_premium) appCats |= TYPE_SMS_PREMIUM;
		else appCats &= ~TYPE_SMS_PREMIUM;
	}
	
	public boolean isTypeSMSPremium() {
		return ((appCats & TYPE_SMS_PREMIUM) == TYPE_SMS_PREMIUM);
	}

	public void setTypeAntispam(boolean antispam) {
		if (antispam) appCats |= TYPE_ANTISPAM;
		else appCats &= ~TYPE_ANTISPAM;
	}
	
	public boolean isTypeAntispam() {
		return ((appCats & TYPE_ANTISPAM) == TYPE_ANTISPAM);
	}
	
	public void setTypePlugin(boolean plugin) {
		if (plugin) appCats |= TYPE_PLUGIN;
		else appCats &= ~TYPE_PLUGIN;
	}
	
	public boolean isTypePlugin() {
		return ((appCats & TYPE_PLUGIN) == TYPE_PLUGIN);
	}
	
	public void setTypeAutoresponder(boolean autoresponder) {
		if (autoresponder) appCats |= TYPE_AUTORESPONDER;
		else appCats &= ~TYPE_AUTORESPONDER;
	}
	
	public boolean isTypeAutoResponder() {
		return ((appCats & TYPE_AUTORESPONDER) == TYPE_AUTORESPONDER);
	}

	public void setTypeSMSReceived(boolean sms_only) {
		if (sms_only) appCats |= TYPE_SMS_RECEIVED;
		else appCats &= ~TYPE_SMS_RECEIVED;
	}
	
	public boolean isTypeSMSReceived() {
		return ((appCats & TYPE_SMS_RECEIVED) == TYPE_SMS_RECEIVED)
				& !this.isTypeSysReserved() 
				& !this.isTypeSecure();
	}

	public void setTypeMsgPremium(boolean msg_premium) {
		if (msg_premium) appCats |= TYPE_MESSAGING_PREMIUM;
		else appCats &= ~TYPE_MESSAGING_PREMIUM;
	}
	
	public boolean isTypeMsgPremium() {
		return ((appCats & TYPE_MESSAGING_PREMIUM) == TYPE_MESSAGING_PREMIUM);
	}

	public void setTypeMessaging(boolean msg_type) {
		if (msg_type) appCats |= TYPE_MESSAGING;
		else appCats &= ~TYPE_MESSAGING;
	}
	
	public boolean isTypeMessaging() {
		return ((appCats & TYPE_MESSAGING) == TYPE_MESSAGING) 
				& !this.isTypeSysReserved() 
				& !this.isTypeSecure();
	}

	public void setTypeBackup(boolean backup) {
		if (backup) appCats |= TYPE_BACKUP;
		else appCats &= ~TYPE_BACKUP;
	}
	
	public boolean isTypeBackup() {
		return ((appCats & TYPE_BACKUP) == TYPE_BACKUP);
	}

	public void setTypeUnregistered(boolean unregistered) {
		if (unregistered) appCats |= TYPE_UNREGISTERED;
		else appCats &= ~TYPE_UNREGISTERED;
	}
	
	public boolean isTypeUnregistered() {
		return ((appCats & TYPE_UNREGISTERED) == TYPE_UNREGISTERED);
	}

	public void setTypeObserver(boolean observer) {
		if (observer) appCats |= TYPE_OBSERVER;
		else appCats &= ~TYPE_OBSERVER;
	}
	
	public boolean isTypeObserver() {
		return ((appCats & TYPE_OBSERVER) == TYPE_OBSERVER);
	}

	
	
	public void setCategoryFiles(boolean file_risk) {
		if (file_risk) appCats |= CATEGORY_FILES;
		else appCats &= ~CATEGORY_FILES;
	}
	
	public boolean isCategoryFiles() {
		return ((appCats & CATEGORY_FILES) == CATEGORY_FILES);
	}

	public void setCategoryMic(boolean mic_risk) {
		if (mic_risk) appCats |= CATEGORY_MIC;
		else appCats &= ~CATEGORY_MIC;
	}
	
	public boolean isCategoryMic() {
		return ((appCats & CATEGORY_MIC) == CATEGORY_MIC);
	}

	public void setCategoryCamera(boolean camera_risk) {
		if (camera_risk) appCats |= CATEGORY_CAMERA;
		else appCats &= ~CATEGORY_CAMERA;
	}
	
	public boolean isCategoryCamera() {
		return ((appCats & CATEGORY_CAMERA) == CATEGORY_CAMERA);
	}

	public void setCategoryEmail(boolean email_risk) {
		if (email_risk) appCats |= CATEGORY_EMAIL;
		else appCats &= ~CATEGORY_EMAIL;
	}
	
	public boolean isCategoryEmail() {
		return ((appCats & CATEGORY_EMAIL) == CATEGORY_EMAIL);
	}

	public void setCategoryLocation(boolean loc_risk) {
		if (loc_risk) appCats |= CATEGORY_LOC;
		else appCats &= ~CATEGORY_LOC;
	}
	
	public boolean isCategoryLocation() {
		return ((appCats & CATEGORY_LOC) == CATEGORY_LOC);
	}

	public void setCategoryPhone(boolean phone_risk) {
		if (phone_risk) appCats |= CATEGORY_PHONE;
		else appCats &= ~CATEGORY_PHONE;
	}
	
	public boolean isCategoryPhone() {
		return ((appCats & CATEGORY_PHONE) == CATEGORY_PHONE);
	}

	public void setCategorySMS(boolean sms_risk) {
		if (sms_risk) appCats |= CATEGORY_SMS;
		else appCats &= ~CATEGORY_SMS;
	}
	
	public boolean isCategorySMS() {
		return ((appCats & CATEGORY_SMS) == CATEGORY_SMS);
	}

	public boolean old_onListWatchlist() {
		if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "onListWatchlist: userDt: " + userDt + " : " + df.format(userDt));
		if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "onListWatchlist: listDt: " + listDt + " : " + df.format(listDt));
		if (isUserWatchlist()&&(userDt > listDt)) return true;
		else if (userDt > listDt) return false;
		else return ((appCats & WATCH_LIST) == WATCH_LIST);
	}

	public boolean old_onListUnsafe() {
		if (isUserUnsafe()&&(userDt > listDt)) return true;
		else if (userDt > listDt) return false;
		else return ((appCats & UNSAFE) == UNSAFE);
	}

	public boolean old_onListHighRisk() {
		if (userDt > listDt) return false;
		else return ((appCats & UNKNOWN_HIGH_RISK) == UNKNOWN_HIGH_RISK);
	}

	public boolean old_onListUnknown() {
		if (userDt > listDt) return false;
		else return ((appCats & UNKNOWN) == UNKNOWN);
	}

	public boolean old_onListSafe() {
		if (isUserSafe()&&(userDt > listDt)) return true;
		else if (userDt > listDt) return false;
		else return ((appCats & SAFE) == SAFE);
	}


	public void setNewInstall(boolean new_install) {
		if (new_install) appCats |= NEW_INSTALL;
		else appCats &= ~NEW_INSTALL;
	}
	
	public boolean isNewInstall() {
		return ((appCats & NEW_INSTALL) == NEW_INSTALL);
	}

	public void setUserSafe(boolean usr_safe) {
		if (usr_safe^isUserSafe()) setUsrDate();
		if (usr_safe) {
			userFlgs |= USR_SAFE;
			userFlgs &= ~USR_WATCH_LIST;
			userFlgs &= ~USR_UNSAFE;
		}
		else userFlgs &= ~USR_SAFE;
	}
	
	public boolean isUserSafe() {
		return ((userFlgs & USR_SAFE) == USR_SAFE);
	}

	public void setUserUnsafe(boolean usr_unsafe) {
		if (usr_unsafe^isUserUnsafe()) setUsrDate();
		if (usr_unsafe) {
			userFlgs |= USR_UNSAFE;
			userFlgs &= ~USR_WATCH_LIST;
			userFlgs &= ~USR_SAFE;
		}
		else userFlgs &= ~USR_UNSAFE;
	}
	
	public boolean isUserUnsafe() {
		return ((userFlgs & USR_UNSAFE) == USR_UNSAFE);
	}

	public void setUserWatchlist(boolean usr_watchlist) {
		if (usr_watchlist^isUserWatchlist()) setUsrDate();
		if (usr_watchlist) {
			userFlgs |= USR_WATCH_LIST;
			userFlgs &= ~USR_UNSAFE;
			userFlgs &= ~USR_SAFE;
		}
		else userFlgs &= ~USR_UNSAFE;
	}
	
	public boolean isUserWatchlist() {
		return ((userFlgs & USR_WATCH_LIST) == USR_WATCH_LIST);
	}
	
	public boolean isUserClassified() {
		return (userFlgs > 0);
	}

	public void setSafe(boolean safe) {
		if (safe) {
			appCats |= SAFE;
			appCats &= ~UNSAFE;
			appCats &= ~WATCH_LIST;
			appCats &= ~UNKNOWN;
			appCats &= ~UNKNOWN_HIGH_RISK;
		}
		else appCats &= ~SAFE;
	}
	
	public boolean isSafe() {
		return ((appCats & SAFE) == SAFE);
	}

	public void setUnknown(boolean unknown) {
		if (unknown) {
			appCats |= UNKNOWN;
			appCats &= ~UNKNOWN_HIGH_RISK;
			appCats &= ~WATCH_LIST;
			appCats &= ~UNSAFE;
			appCats &= ~SAFE;
		}
		else appCats = appCats & ~UNKNOWN;
	}
	
	public boolean isUnknown() {
		return ((appCats & UNKNOWN) == UNKNOWN);
	}

	public void setHighRisk(boolean high_risk) {
		if (high_risk) {
			appCats |= UNKNOWN_HIGH_RISK;
			appCats &= ~WATCH_LIST;
			appCats &= ~SAFE;
			appCats &= ~UNSAFE;
			appCats &= ~UNKNOWN;
		}
		else appCats &= ~UNKNOWN_HIGH_RISK;
	}
	
	public boolean isHighRisk() {
		return ((appCats & UNKNOWN_HIGH_RISK) == UNKNOWN_HIGH_RISK);
	}

	public void setUnsafe(boolean unsafe) {
		if (unsafe) {
			appCats |= UNSAFE;
			appCats &= ~WATCH_LIST;
			appCats &= ~UNKNOWN_HIGH_RISK;
			appCats &= ~UNKNOWN;
			appCats &= ~SAFE;
		}
		else appCats = appCats & ~UNSAFE;
	}
	
	public boolean isUnsafe() {
		return ((appCats & UNSAFE) == UNSAFE);
	}

	public void setWatchlist(boolean watchlist) {
		if (watchlist) {
			appCats |= WATCH_LIST;
			appCats &= ~UNSAFE;
			appCats &= ~UNKNOWN_HIGH_RISK;
			appCats &= ~UNKNOWN;
			appCats &= ~SAFE;
		}
		else appCats = appCats & ~UNSAFE;
	}
	
	public boolean isWatchlist() {
		return ((appCats & WATCH_LIST) == WATCH_LIST);
	}

	public void setSysApp(boolean sysapp) {
		if (sysapp) appCats |= TYPE_SYS_APP;
		else appCats = appCats & ~TYPE_SYS_APP;
	}
	
	public boolean isSysApp() {
		return ((appCats & TYPE_SYS_APP) == TYPE_SYS_APP);
	}

}
