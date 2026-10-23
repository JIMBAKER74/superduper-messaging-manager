package com.sdmmllc.superdupersmsmanager;

public class RegisteredApps {

	public static final String REGISTERED_APPS_TBL = "RiskList";

	public static final String[] REGISTERED_APPS_COLUMNS = {
    	"risk_list_ID",
    	"risk_list_min_app_version_CD",
        "risk_list_type_CD", 
        "risk_list_creation_TS", 
        "risk_list_download_TS",
	};
	
	public static int
		risk_list_ID = 					0,
		risk_list_min_app_version_CD = 	1,
	    risk_list_type_CD =  			2,
	    risk_list_creation_TS =  		3,
	    risk_list_download_TS = 		4;

	public static final String ALL_APPS_COLUMNS = 
			RegisteredApps.REGISTERED_APPS_COLUMNS[risk_list_ID] + ", " +
			RegisteredApps.REGISTERED_APPS_COLUMNS[risk_list_min_app_version_CD] + ", " +
			RegisteredApps.REGISTERED_APPS_COLUMNS[risk_list_type_CD] + ", " +
			RegisteredApps.REGISTERED_APPS_COLUMNS[risk_list_creation_TS] + ", " +
			RegisteredApps.REGISTERED_APPS_COLUMNS[risk_list_download_TS];

	public static final String REGISTERED_APP_TBL = "RiskListPackage";
	
	public static final String[] REGISTERED_APP_COLUMNS = {
    	"rlp_risk_list_ID",
    	"rlp_package_ID",
        "rlp_app_name_TXT", 
        "rlp_version_CD", 
        "rlp_version_name_TXT", 
        "rlp_app_type_TXT", // 5
        "rlp_created_by_DESCR", 
        "rlp_other_name_TXT", 
        "rlp_flags", 
        "rlp_risk_DESCR", 
        "rlp_recommend_action_DESCR",// 10 
        "rlp_entry_TS", 
        "rlp_update_TS",
	};

	public static final int
		rlp_risk_list_ID = 			0,
		rlp_package_ID = 			1,
	    rlp_pkg_name_TXT =  		2,
	    rlp_version_CD =  			3,
	    rlp_version_name_TXT = 		4,
	    rlp_app_type_TXT = 			5,
	    rlp_created_by_DESCR =  	6,
	    rlp_other_name_TXT =  		7,
	    rlp_flags =  				8,
	    rlp_risk_DESCR =  			9,
	    rlp_recommend_action_DESCR = 10,
	    rlp_entry_TS =  			11,
	    rlp_update_TS = 			12;
	
	public static final String ALL_APP_COLUMNS = 
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_risk_list_ID] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_package_ID] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_pkg_name_TXT] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_version_CD] + ", " + 
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_version_name_TXT] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_app_type_TXT] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_created_by_DESCR] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_other_name_TXT] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_flags] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_risk_DESCR] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_recommend_action_DESCR] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_entry_TS] + ", " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_update_TS];

	public static final String ALL_APP_DB_COLUMNS = 
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_risk_list_ID] + " INTEGER NOT NULL, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_package_ID] + " INTEGER NOT NULL, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_pkg_name_TXT] + " TEXT NOT NULL, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_version_CD] + " INTEGER NOT NULL, " + 
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_version_name_TXT] + " TEXT NOT NULL, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_app_type_TXT] + " TEXT, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_created_by_DESCR] + " TEXT, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_other_name_TXT] + " TEXT, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_flags] + " INTEGER, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_risk_DESCR] + " TEXT, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_recommend_action_DESCR] + " TEXT, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_entry_TS] + " LONG NOT NULL, " +
			RegisteredApps.REGISTERED_APP_COLUMNS[rlp_update_TS] + " LONG ";
	
	public class RegisteredApp {
		
	}
}
