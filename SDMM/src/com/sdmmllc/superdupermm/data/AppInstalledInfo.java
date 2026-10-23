package com.sdmmllc.superdupermm.data;


public class AppInstalledInfo {

	public static final String PACKAGE_STATUS_TBL = "Package";
	
	public static final String[] PACKAGE_STATUS_INFO_COLUMNS = {
    	"pkg_ID",
        "pkg_version_CD", 
	    "pkg_category_ID", 
        "pkg_name_TXT", 
        "pkg_app_name_TXT", 
        "pkg_version_name_TXT", // 5
        "pkg_shared_user_ID", 
        "pkg_shared_user_LBL", 
        "pkg_install_TS", 
        "pkg_update_TS",    
        "pkg_uninstall_TS", // 10 
        "pkg_entry_TS",
		"pkg_sms_receiver_priority_num",
	    "pkg_risk_category_Flg", 
		"pkg_assigned_perm_type", 
		"pkg_sds_priority",  // 15
		"pkg_user_priority",
		"pkg_feature_flgs", 
        "pkg_flags",    
        "pkg_user_flags",
        "pkg_user_TS",  //20
        "pkg_sms_received_receiver",
        "pkg_sms_delivered_receiver",
        "pkg_wap_push_deliver_receiver",
	    "pkg_sms_respond_via_message",
        "pkg_sms_send", // 25
        "pkg_sms_send_to",
        "pkg_sms_to", 
        "pkg_sms",
        "pkg_mms_to", 
        "pkg_mms", // 30
	};

	public static final int
		pkg_ID = 						0,
	    pkg_version_CD =  				1,
	    pkg_category_ID =  				2,
	    pkg_name_TXT =  				3,
	    pkg_app_name_TXT =  			4,
	    pkg_version_name_TXT = 			5,
	    pkg_shared_user_ID =  			6,
	    pkg_shared_user_LBL =  			7,
	    pkg_install_TS =  				8,
	    pkg_update_TS =     			9,
	    pkg_uninstall_TS =  			10,
	    pkg_entry_TS = 					11,
		pkg_sms_receiver_priority_num = 12, 
	    pkg_risk_category_Flg =  		13,
		pkg_assigned_perm_type =  		14,
		pkg_sds_priority =  			15,
		pkg_user_priority = 			16,
		pkg_feature_flgs =  			17,
	    pkg_flags =     				18,
	    pkg_user_flags = 				19,
	    pkg_user_TS =   				20,
	    pkg_sms_received_receiver = 	21,
	    pkg_sms_delivered_receiver = 	22,
	    pkg_wap_push_deliver_receiver = 23,
	    pkg_sms_respond_via_message =	24,
	    pkg_sms_send = 					25,
	    pkg_sms_send_to =				26,
	    pkg_sms_to = 					27,
	    pkg_sms =						28,
	    pkg_mms_to = 					29,
	    pkg_mms =						30;
	
	public static final String ALL_COLUMNS = 
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_ID] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_version_CD] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_category_ID] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_name_TXT] + ", " + 
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_app_name_TXT] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_version_name_TXT] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_shared_user_ID] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_shared_user_LBL] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_install_TS] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_update_TS] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_uninstall_TS] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_entry_TS] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_receiver_priority_num] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_risk_category_Flg] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_assigned_perm_type] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sds_priority] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_user_priority] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_feature_flgs] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_flags] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_user_flags] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_user_TS] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_received_receiver] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_delivered_receiver] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_wap_push_deliver_receiver] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_respond_via_message] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_send] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_send_to] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_to] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_mms_to] + ", " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_mms];

	public static final String ALL_DB_COLUMNS = 
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_ID] + " INTEGER primary key autoincrement, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_version_CD] + " INTEGER NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_category_ID] + " INTEGER NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_name_TXT] + " TEXT NOT NULL, " + 
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_app_name_TXT] + " TEXT NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_version_name_TXT] + " TEXT NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_shared_user_ID] + " INTEGER, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_shared_user_LBL] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_install_TS] + " LONG NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_update_TS] + " LONG, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_uninstall_TS] + " LONG, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_entry_TS] + " LONG NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_receiver_priority_num] + " INTEGER, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_risk_category_Flg] + " INTEGER NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_assigned_perm_type] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sds_priority] + " INTEGER, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_user_priority] + " INTEGER, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_feature_flgs] + " INTEGER NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_flags] + " INTEGER NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_user_flags] + " INTEGER NOT NULL, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_user_TS] + " LONG, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_received_receiver] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_delivered_receiver] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_wap_push_deliver_receiver] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_respond_via_message] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_send] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_send_to] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms_to] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_sms] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_mms_to] + " TEXT, " +
		AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[pkg_mms] + " TEXT ";

	public AppInstalledInfo() {
		super();
	}

}
