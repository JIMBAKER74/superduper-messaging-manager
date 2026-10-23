package com.sdmmllc.superdupermm.data;


import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.scan.AppCategory;
import com.sdmmllc.superdupermm.scan.AppMonitoredPermissions;
import com.sdmmllc.superdupermm.scan.AppUsesPermissions;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.UserNotify;
import com.sdmmllc.superdupersmsmanager.AppEvent;
import com.sdmmllc.superdupersmsmanager.RegisteredApps;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class SDDBHelper extends SQLiteOpenHelper{
 
    //use the default path of the database.
    private static String DB_PATH = "";
    private static String DB_NAME = "SDSMSDB.db";
	private final Context sdsContext;
	private String TAG = "SDSDBHelper", TAG2 = "";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    
    // common names for db elements
    public static String 
    	TABLES_FOUND = "TabFound", 
    	COLUMNS_FOUND = "ColFound",
    	DB_CREATED = "DBCreated", 
    	DB_VERSION = "dbversion", 
    	DB_DATA_VER = "DataVersion";
    
    String tablesFound = "", columnsFound = "", dbCreated = "";
    
    // DB version 1 - released with 1.0
	public static final int DATABASE_VERSION = 1, 
		DATA_VERSION1 = 1;
    int isFoundCount = 0;
	
	public SDDBHelper(Context context) {
    	super(context, DB_NAME, null, DATABASE_VERSION);
        DB_PATH = context.getResources().getString(R.string.dbPathName);
        this.sdsContext = context;
        settings = sdsContext.getSharedPreferences(SdmmsConsts.DB_FILE, 0);
        editor = settings.edit();
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "constructor complete");
    }	
 
	public SDDBHelper(Context context, boolean updateCheck) {
    	super(context, DB_NAME, null, DATABASE_VERSION);
        DB_PATH = context.getResources().getString(R.string.dbPathName);
        this.sdsContext = context;
        settings = sdsContext.getSharedPreferences(SdmmsConsts.DB_FILE, 0);
        editor = settings.edit();
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "constructor complete");
    }	
 
	@Override
	public void onCreate(SQLiteDatabase db) {
		createDB(db);
		//this.openDataBase();
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
	
    public void createDB(SQLiteDatabase appDB) throws SQLException{
    	boolean dbExist = (settings.contains(DB_CREATED))&&isFound();
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDB: Checking database for existence...");
    	if(!dbExist || SdmmsConsts.DEBUG_NEW_DB){
            if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDB: Database does not exist. Attempting creation...");
        	createDBTables(appDB);
    	}
    	// check for db version 4
		int dbVersion = settings.getInt(DB_VERSION, 0);
		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "openDataBase: Database opened - checking for log table.");
        if (!(dbVersion == DATABASE_VERSION)) {
        	//onUpgrade(appDB, settings.getInt(DB_VERSION, 0), DATABASE_VERSION);
            editor.putInt(DB_VERSION, DATABASE_VERSION);
            editor.commit();
        }
		//do nothing - database already exist
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDB: Database exists.");
		if (appDB == null) appDB = SQLiteDatabase.openDatabase(DB_PATH+DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
    }
    
	public String strNotNull(String str) {
		if (str == null) return "";
		else return str;
	}

	public boolean strToBoolean(String str) {
		return str.equals("Y");
	}
	
	public String booleanToStr(boolean val) {
		if (val) {
			return "Y";
		} else {
			return "N";
		}
	}
	
	public void setTAG2(String tag) {
		TAG2 = "(" + tag + ")";
	}
 
    /**
     * Creates a empty database on the system 
     * */
    public void createDBTables(SQLiteDatabase appDB) throws SQLException{
 
        String DATABASE_CREATE_METADATA =
        		"CREATE TABLE IF NOT EXISTS android_metadata (locale TEXT DEFAULT 'en_US'); COMMIT;";
        String INSERT_METADATA_DATA = 
        		"INSERT INTO android_metadata (locale) VALUES ('en_US'); COMMIT;";
	    // Scan Table
        String DATABASE_CREATE_SCAN_TBL =
        	"CREATE TABLE IF NOT EXISTS " + SDAppList.APP_LIST_SCAN_TBL + " ("
	        	+ SDAppList.ALL_DB_COLUMNS + "); COMMIT;";
	    // Packages Table
        String DATABASE_CREATE_SCAN_ITEMS_TBL =
        	"CREATE TABLE IF NOT EXISTS " + AppScanInfo.SCAN_ITEMS_TBL + " ("
	        	+ AppScanInfo.ALL_DB_COLUMNS + "); COMMIT;";
	    // Permission Table
        String DATABASE_CREATE_PERMISSIONS_TBL =
        	"CREATE TABLE IF NOT EXISTS " + AppMonitoredPermissions.APP_PERMISSION_TBL + " ("
	        	+ AppMonitoredPermissions.APP_PERMISSION_COLUMNS[0] + " INTEGER primary key autoincrement, "
	        	+ AppMonitoredPermissions.APP_PERMISSION_COLUMNS[1] + " TEXT NOT NULL, " 
	        	+ AppMonitoredPermissions.APP_PERMISSION_COLUMNS[2] + " TEXT NOT NULL, " 
	        	+ AppMonitoredPermissions.APP_PERMISSION_COLUMNS[3] + " TEXT NOT NULL, " 
	        	+ AppMonitoredPermissions.APP_PERMISSION_COLUMNS[4] + " INTEGER NOT NULL); COMMIT;";
	    // ScanItem Table
        String DATABASE_CREATE_INSTALLED_APPS_TBL =
        	"CREATE TABLE IF NOT EXISTS " + AppInstalledInfo.PACKAGE_STATUS_TBL + " ("
                	+ AppInstalledInfo.ALL_DB_COLUMNS + "); COMMIT;";
	    // UsesPermission Table
        String DATABASE_CREATE_USES_PERMISSION_TBL =
            	"CREATE TABLE IF NOT EXISTS " + AppUsesPermissions.APP_USES_PERMISSIONS_TBL + " ("
	            	+ AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[0] + " INTEGER NOT NULL, "
	            	+ AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[1] + " INTEGER NOT NULL, " 
	            	+ AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[2] + " INTEGER NOT NULL, " 
	            	+ AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[3] + " INTEGER NOT NULL, " 
	            	+ AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[4] + " INTEGER NOT NULL, "
	            	+ " primary key (" + AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[0] + 
		            ", " + AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[1] + 
        			", " + AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[2] + ")); COMMIT;";
		// Category Table
        String DATABASE_CREATE_CATEGORY_TBL =
        	"CREATE TABLE IF NOT EXISTS " + AppCategory.CATEGORY_TBL + " ("
	        	+ AppCategory.CATEGORY_COLUMNS[0] + " INTEGER primary key autoincrement, "
	        	+ AppCategory.CATEGORY_COLUMNS[1] + " TEXT NOT NULL, " 
	        	+ AppCategory.CATEGORY_COLUMNS[2] + " TEXT NOT NULL, " 
	        	+ AppCategory.CATEGORY_COLUMNS[3] + " INTEGER NOT NULL); COMMIT;";
	    // RiskListTable
        String DATABASE_CREATE_REGISTERED_APPS_TBL =
        	"CREATE TABLE IF NOT EXISTS " + RegisteredApps.REGISTERED_APPS_TBL + " ("
	        	+ RegisteredApps.REGISTERED_APPS_COLUMNS[0] + " INTEGER primary key autoincrement, "
	        	+ RegisteredApps.REGISTERED_APPS_COLUMNS[1] + " INTEGER NOT NULL, "
	        	+ RegisteredApps.REGISTERED_APPS_COLUMNS[2] + " INTEGER NOT NULL, " 
	        	+ RegisteredApps.REGISTERED_APPS_COLUMNS[3] + " LONG NOT NULL, " 
	        	+ RegisteredApps.REGISTERED_APPS_COLUMNS[4] + " LONG); COMMIT;";
	    // RiskListPackageTable
        String DATABASE_CREATE_REGISTERED_APP_TBL =
        	"CREATE TABLE IF NOT EXISTS " + RegisteredApps.REGISTERED_APP_TBL + " ("
	        	+ RegisteredApps.ALL_APP_DB_COLUMNS + ", "
	            + " primary key (" + RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_list_ID] + 
	            ", " + RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_package_ID] + ")); COMMIT;";
        String DATABASE_CREATE_MESSAGE_TBL =
        		"CREATE TABLE IF NOT EXISTS " + SDMessages.MESSAGES_TBL + " ("
        				+ SDMessages.MESSAGES_COLUMNS[0] + " integer primary key autoincrement, "
        				+ SDMessages.MESSAGES_COLUMNS[1] + " TEXT NOT NULL, "
        				+ SDMessages.MESSAGES_COLUMNS[2] + " TEXT, "
        				+ SDMessages.MESSAGES_COLUMNS[3] + " TEXT, "
        				+ SDMessages.MESSAGES_COLUMNS[4] + " TEXT, "
        				+ SDMessages.MESSAGES_COLUMNS[5] + " INT NOT NULL DEFAULT 0 " 
        				+ "); COMMIT;";
        String DATABASE_CREATE_APP_EVENT_TBL =
        		"CREATE TABLE IF NOT EXISTS " + AppEvent.APP_EVENT_TBL + " ("
        				+ AppEvent.APP_EVENT_COLUMNS[0] + " integer primary key autoincrement, "
        				+ AppEvent.APP_EVENT_COLUMNS[1] + " TEXT NOT NULL, "
        				+ AppEvent.APP_EVENT_COLUMNS[2] + " TEXT, "
        				+ AppEvent.APP_EVENT_COLUMNS[3] + " INT NOT NULL DEFAULT 0 " 
        				+ "); COMMIT;";
        String DATABASE_CREATE_USERNOTIFY =
            	"CREATE TABLE IF NOT EXISTS " + UserNotify.USER_NOTIFY_TBL + " ("
	            	+ UserNotify.USER_NOTIFY_COLUMNS[0] + " integer primary key autoincrement, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[1] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[2] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[3] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[4] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[5] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[6] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[7] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[8] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[9] + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[10]  + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[11]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[12]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[13]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[14]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[15]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[16]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[17]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[18]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[19]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[20]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[21]  + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[22]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[23]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[24]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[25]  + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[26]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[27]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[28]  + " TEXT NOT NULL, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[29]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[30]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[31]  + " TEXT, "
	            	+ UserNotify.USER_NOTIFY_COLUMNS[32] + " TEXT); COMMIT;";
        
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_METADATA);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + INSERT_METADATA_DATA);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_SCAN_ITEMS_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_SCAN_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_PERMISSIONS_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_INSTALLED_APPS_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_USES_PERMISSION_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_MESSAGE_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_CATEGORY_TBL);
		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_REGISTERED_APPS_TBL);
		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_REGISTERED_APP_TBL);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_APP_EVENT_TBL);
		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + DATABASE_CREATE_USERNOTIFY);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables:" + "Creating Metadata table.");
    	if (!isFoundTable(appDB, "android_metadata")) {
	    	try {
	    		appDB.execSQL(DATABASE_CREATE_METADATA);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating METADATAA table SQL");
	    	}
	    	try { 
	    		appDB.execSQL(INSERT_METADATA_DATA);
	    	} catch (SQLException se) {
	    		throw new Error("Error inserting METADATA data SQL");
	    	}
    	}
    	
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
        		AppScanInfo.SCAN_ITEMS_TBL + " table.");
		if (!isFoundTable(appDB, AppScanInfo.SCAN_ITEMS_TBL))
	    	try {
	    		appDB.execSQL(DATABASE_CREATE_SCAN_ITEMS_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
        		AppScanInfo.SCAN_ITEMS_TBL + " table SQL");
	    	}

        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
        		AppMonitoredPermissions.APP_PERMISSION_TBL + " table.");
		if (!isFoundTable(appDB, AppMonitoredPermissions.APP_PERMISSION_TBL))
	    	try {
	    		appDB.execSQL(DATABASE_CREATE_PERMISSIONS_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
        		AppMonitoredPermissions.APP_PERMISSION_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: " +
				AppInstalledInfo.PACKAGE_STATUS_TBL + " table.");
		if (!isFoundTable(appDB, AppInstalledInfo.PACKAGE_STATUS_TBL))
	    	try {
	    		appDB.execSQL(DATABASE_CREATE_INSTALLED_APPS_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
				AppInstalledInfo.PACKAGE_STATUS_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
				SDMessages.MESSAGES_TBL + " table.");
		if (!isFoundTable(appDB, SDMessages.MESSAGES_TBL))
	    	try {
	    		appDB.execSQL(DATABASE_CREATE_MESSAGE_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
				SDMessages.MESSAGES_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
				AppEvent.APP_EVENT_TBL + " table.");
		if (!isFoundTable(appDB, AppEvent.APP_EVENT_TBL))
	    	try {
	    		appDB.execSQL(DATABASE_CREATE_APP_EVENT_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
				AppEvent.APP_EVENT_TBL + " table SQL");
	    	}
		
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
        		SDAppList.APP_LIST_SCAN_TBL + " table.");
		if (!isFoundTable(appDB, SDAppList.APP_LIST_SCAN_TBL)) try {
	    		appDB.execSQL(DATABASE_CREATE_SCAN_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
        		SDAppList.APP_LIST_SCAN_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
				AppUsesPermissions.APP_USES_PERMISSIONS_TBL + " table.");
		if (!isFoundTable(appDB, AppUsesPermissions.APP_USES_PERMISSIONS_TBL)) try {
	    		appDB.execSQL(DATABASE_CREATE_USES_PERMISSION_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
				AppUsesPermissions.APP_USES_PERMISSIONS_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
				AppCategory.CATEGORY_TBL + " table.");
		if (!isFoundTable(appDB, AppCategory.CATEGORY_TBL)) try {
	    		appDB.execSQL(DATABASE_CREATE_CATEGORY_TBL);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " +
				AppCategory.CATEGORY_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
				RegisteredApps.REGISTERED_APPS_TBL + "table.");
		if (!isFoundTable(appDB, RegisteredApps.REGISTERED_APPS_TBL)) try {
	    		appDB.execSQL(DATABASE_CREATE_REGISTERED_APPS_TBL);
	    		String insert = "INSERT INTO " + RegisteredApps.REGISTERED_APPS_TBL + " ( " + 
	    				RegisteredApps.REGISTERED_APPS_COLUMNS[0] + ", " +
	    				RegisteredApps.REGISTERED_APPS_COLUMNS[1] + ", " +
	    				RegisteredApps.REGISTERED_APPS_COLUMNS[2] + ", " +
	    				RegisteredApps.REGISTERED_APPS_COLUMNS[3] + ", " +
	    				RegisteredApps.REGISTERED_APPS_COLUMNS[4] + " " +
		    			" ) VALUES ( "
		    			+ "0, 1, 0, " + Calendar.getInstance().getTimeInMillis()
		    			+ ", " + Calendar.getInstance().getTimeInMillis()
		    			+ ");";
	    		appDB.execSQL(insert);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " + RegisteredApps.REGISTERED_APPS_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating " +
				RegisteredApps.REGISTERED_APP_TBL + " table.");
		String insert = "";
		if (!isFoundTable(appDB, RegisteredApps.REGISTERED_APP_TBL)) try {
	    		appDB.execSQL(DATABASE_CREATE_REGISTERED_APP_TBL);
	    		Calendar cal1 = Calendar.getInstance();
	    		cal1.set(2012, 3, 1);
	    		long riskListTS = cal1.getTimeInMillis();
	    		insert = "INSERT INTO " + 						RegisteredApps.REGISTERED_APP_TBL
	    			+ " SELECT 0 as " + 						RegisteredApps.REGISTERED_APP_COLUMNS[0] + ", "
	    			+ " 1 as " + 								RegisteredApps.REGISTERED_APP_COLUMNS[1] + ", "
	    			+ " 'com.sirispublishing.gotext' as " + 	RegisteredApps.REGISTERED_APP_COLUMNS[2] + ", "
	    			+ " 0 as " + 								RegisteredApps.REGISTERED_APP_COLUMNS[3] + "," 
	    			+ " 0 as " + 								RegisteredApps.REGISTERED_APP_COLUMNS[4] + "," 
	    			+ " null as " + 							RegisteredApps.REGISTERED_APP_COLUMNS[5] + ","
	    			+ " null as " + 							RegisteredApps.REGISTERED_APP_COLUMNS[6] + ","
	    			+ " null as " + 							RegisteredApps.REGISTERED_APP_COLUMNS[7] + "," 
	    			+ " " + AppScanInfo.SMS_REGISTERED + " as " + RegisteredApps.REGISTERED_APP_COLUMNS[8] + ","
	    			+ " null as " + 							RegisteredApps.REGISTERED_APP_COLUMNS[9] + ","
	    			+ " null as " + 							RegisteredApps.REGISTERED_APP_COLUMNS[10] + ","
	    			+ " " + riskListTS + " as " + 				RegisteredApps.REGISTERED_APP_COLUMNS[11] + ","
	    			+ " " + riskListTS + " " + 					RegisteredApps.REGISTERED_APP_COLUMNS[12] + " "
	    			+ " UNION SELECT 0,2,'com.sirispublishing.gamedaysms', 		0, 0, null, null, null, " + AppScanInfo.SMS_REGISTERED + ", null, null," + riskListTS + "," + riskListTS + "  ";
	    		if (SdmmsConsts.DEBUG_SCAN) 
	    			insert += " UNION SELECT 0,100000,'com.android.soundrecorder', 		0, 0, null, null, null, " + AppScanInfo.SMS_REGISTERED + ", null, null," + riskListTS + "," + riskListTS + "  ";
    			insert += " UNION SELECT 0,57,'com.crazyapps.chit.chat.robo.chat.bathroom.time.cha', 0, 0, null,'Crazy Apps', null, " + AppScanInfo.SMS_REGISTERED + ", null, null," + riskListTS + "," + riskListTS + " ;";
	      		appDB.execSQL(insert);
			} catch (SQLException se) {
				Log.w(TAG, "SQL Error: " + insert);
				se.printStackTrace();
	    		throw new Error("Error creating " + RegisteredApps.REGISTERED_APP_TBL + " table SQL");
	    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "createDBTables: Creating "
				+ UserNotify.USER_NOTIFY_TBL + " table.");
		if (!isFoundTable(appDB, UserNotify.USER_NOTIFY_TBL)) try {
	    		appDB.execSQL(DATABASE_CREATE_USERNOTIFY);
	    	} catch (SQLException se) {
	    		throw new Error("Error creating " + UserNotify.USER_NOTIFY_TBL + " table SQL");
	    	}
    
    }
    
    public boolean execSQLString(SQLiteDatabase appDB, String sqlStr) throws SQLException {
    	try {
    		appDB.execSQL(sqlStr);
		} catch (SQLException se) {
    		throw new Error("Error executing: " + se + " / \nSQL: " + sqlStr);
    	}
		return true;
    }

 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean isFound() {
        if (SdmmsConsts.DEBUG_DB) {
        	isFoundCount++;
        	Log.i(TAG, TAG2 + "isFound: Called " + isFoundCount + " times.");
        }
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "isFound: Checking DB - checkDataBase");
    	SQLiteDatabase checkDB = null;
    	try{
            if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "isFound: Checking DB - attempting openDatabase");
    		//String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(DB_PATH+DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
            if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "isFound: Checking DB - openDatabase done");
    	}catch(SQLiteException e){
    		//database does't exist yet.
    		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "isFound: Database does not exist.");
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	return checkDB != null ? true : false;
    }
 
	public boolean isFoundTable(SQLiteDatabase appDB, String str) {
		if (appDB == null) return false;
        if (SdmmsConsts.DEBUG_DB) 
        	Log.i(TAG, TAG2 + "isFoundTable: Checking Table - " + str); 
		Cursor mCursor = appDB.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='"
				+ str + "'", null);
		if (mCursor == null) return false;
		int countTable = mCursor.getCount();
		mCursor.moveToFirst();
		mCursor.close();
        if (SdmmsConsts.DEBUG_DB) 
        	Log.i(TAG, TAG2 + "isFoundTable: Found Table: " + (countTable > 0)); 
		return (countTable > 0);    
	}
 
	public boolean isFoundColumn(SQLiteDatabase appDB, String tbl, String col) {
		if (appDB == null) return false;
		int idx = -1;
		Cursor mCursor = appDB.rawQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='"
				+ tbl + "'", null);
		if (mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			String sql = mCursor.getString(0);
	        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "isFoundColumn: Table SQL - " + sql); 
			idx = sql.indexOf(col);
		}
		mCursor.close();
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "isFoundColumn: Table - " + tbl 
        		+ " : Column - " + col + " index is " + idx + " and found: " + !(idx < 0));
		return !(idx < 0);    
	}
 
	public void wipeData(SQLiteDatabase appDB) {
        String DATABASE_DROP_INSTALLED_APPS =
        	"DROP TABLE IF EXISTS " + AppInstalledInfo.PACKAGE_STATUS_TBL + "; COMMIT;";
        String DATABASE_DROP_MESSAGES =
        	"DROP TABLE IF EXISTS " + SDMessages.MESSAGES_TBL + "; COMMIT;";
        String DATABASE_DROP_APP_EVENT =
        	"DROP TABLE IF EXISTS " + AppEvent.APP_EVENT_TBL + "; COMMIT;";
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "wipeData: Dropping wiping tables.");
    	try {
    		appDB.execSQL(DATABASE_DROP_INSTALLED_APPS);
    	} catch (SQLException se) {
    		throw new Error("Error dropping InstalledApps table SQL");
    	}

        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "wipeData: Dropping Messages table.");
    	try {
    		appDB.execSQL(DATABASE_DROP_MESSAGES);
    	} catch (SQLException se) {
    		throw new Error("Error dropping Messages table SQL");
    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "wipeData: Dropping AppEvent table.");
    	try {
    		appDB.execSQL(DATABASE_DROP_APP_EVENT);
    	} catch (SQLException se) {
    		throw new Error("Error dropping App Event table SQL");
    	}

		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "wipeData: Dropping Notify table.");
    	editor.remove(DB_VERSION);
    	editor.remove(TABLES_FOUND);
    	editor.remove(COLUMNS_FOUND);
    	editor.remove(DB_CREATED);
    	createDB(appDB);
	}
}