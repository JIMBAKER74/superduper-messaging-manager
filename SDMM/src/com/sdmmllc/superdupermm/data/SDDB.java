package com.sdmmllc.superdupermm.data;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.sdmmllc.superdupermm.notifications.ASNotificationManager;
import com.sdmmllc.superdupermm.notifications.SpaUserNotification;
import com.sdmmllc.superdupermm.scan.AppUsesPermissions;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDAppListUpdateListener;
import com.sdmmllc.superdupermm.scan.UserNotify;
import com.sdmmllc.superdupermm.scan.AppUsesPermissions.UsesFilter;
import com.sdmmllc.superdupermm.xml.SpaXmlResultAppPackageInfo;
import com.sdmmllc.superdupermm.xml.SpaXmlResultSpywareListInfo;
import com.sdmmllc.superdupersmsmanager.RegisteredApps;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class SDDB {

	//The Android's default system path of your application database.
	private SQLiteDatabase appDB; 
	private SDDBHelper dbHelper;
	private Context spaContext;
	private String TAG = "SDDB", TAG2 = "";
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;

	private List<SDDBConn> conns;
	private Integer connPool = 0;

	// Observer classes
	private ScanListListener mScanListListener;
	private SDAppListUpdateListener mScanPkgListUpdateListener;

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

	public String safeString(String value) {
		if (value == null) return "";
		value = value.replaceAll("'", "''");
		value = value.replaceAll("\\\\", "\\\\");
		return value;
	}
	
	String[] concat(String[] A, String[] B) {
		   int aLen = A.length;
		   int bLen = B.length;
		   String[] C= new String[aLen+bLen];
		   System.arraycopy(A, 0, C, 0, aLen);
		   System.arraycopy(B, 0, C, aLen, bLen);
		   return C;
		}

	public SDDB(Context context) {
		init(context);
	}	

	public SDDB(Context context, boolean updateCheck) {
		init(context);
		if (updateCheck) { 
			settings = spaContext.getSharedPreferences(SdmmsConsts.DB_FILE, 0);
			// fix - "DB_FILE" was put in as key...
			if (settings.contains(SdmmsConsts.DB_FILE)) {
				editor = settings.edit();
				editor.putInt(SDDBHelper.DB_VERSION, settings.getInt(SdmmsConsts.DB_FILE,
						0));
				editor.remove(SdmmsConsts.DB_FILE);
				editor.commit();
			}
			// check for proper DB version
			if (!settings.contains(SDDBHelper.DB_VERSION) ||
					(settings.getInt(SDDBHelper.DB_VERSION, 0) != SDDBHelper.DATABASE_VERSION) ||
					SdmmsConsts.DEBUG_NEW_DB) {
				editor = settings.edit();
				if (!settings.contains(SDDBHelper.DB_VERSION)|
						(settings.getInt(SDDBHelper.DB_VERSION, 0) != SDDBHelper.DATABASE_VERSION) ||
						SdmmsConsts.DEBUG_NEW_DB){
					appDB = dbHelper.getWritableDatabase();
					dbHelper.createDB(appDB);
					appDB.close();
				}
				editor.putInt(SDDBHelper.DB_VERSION, SDDBHelper.DATABASE_VERSION);
				editor.commit();
			}
			updateData();
			editor = null;
			settings = null;
		}
	}

	private void init(Context context) {
		dbHelper = new SDDBHelper(context);
		spaContext = context;
		appDB = dbHelper.getReadableDatabase();
		conns = new ArrayList<SDDBConn>();
	}

	public void updateData() {
		if (settings.getInt(SDDBHelper.DB_DATA_VER, 0) != SDDBHelper.DATA_VERSION1) {
			editor = settings.edit();
			dataUpdateVersion1();
			editor.putInt(SDDBHelper.DB_DATA_VER, SDDBHelper.DATA_VERSION1);
			editor.commit();
		}
	}

	public void dataUpdateVersion1() {
		// no updates yet...
	}

	public SDDBConn open(boolean readWrite) {
		synchronized (conns) {
			if (appDB == null) {
				if (!readWrite) appDB = dbHelper.getReadableDatabase();
				else appDB = dbHelper.getWritableDatabase();
			} else {
				if (appDB.isReadOnly()&readWrite) appDB = dbHelper.getWritableDatabase();
				if (!appDB.isReadOnly()&!readWrite) appDB = dbHelper.getReadableDatabase();
				if (!appDB.isOpen()) {
					if (!readWrite) appDB = dbHelper.getReadableDatabase();
					else appDB = dbHelper.getWritableDatabase();
				}
			}
			return getConn();
		}
	}

	public void close(SDDBConn conn) {
		synchronized(conns) {
			if (closeConn(conn)) appDB.close();
		}
	}

	public void beginTransaction() {
		appDB.beginTransaction();
	}

	public void endTransaction() {
		appDB.endTransaction();
	}

	public void setTAG2(String tag) {
		TAG2 = "(" + tag + ")";
	}

	public void wipeData() {
		SDDBConn mConn = open(true);
		dbHelper.wipeData(appDB);
		close(mConn);
	}

	public SDDBConn getConn() {
		synchronized (connPool) {
			connPool++;
		}
		SDDBConn newConn = new SDDBConn(connPool);
		synchronized (conns) {
			conns.add(newConn);
		}
		return newConn;
	}

	public boolean closeConn(SDDBConn conn) {
		synchronized (conns) {
			conns.remove(conn);
		}
		return (conns.size() < 1);
	}

	public boolean execSQLString(String sqlStr) throws SQLException {
		try {
			appDB.execSQL(sqlStr);
		} catch (SQLException se) {
			throw new Error("Error executing: " + se + " / \nSQL: " + sqlStr);
		}
		return true;
	}

	public boolean isOpen() {
		return appDB.isOpen();
	}



	/** 
	 * 
	 * Scan Registered App List functions
	 * 
	 */

	//TODO 
	// change from "risk list" to "app list"
	public int getCurrentRegisteredAppListId() throws SQLException {
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, RegisteredApps.REGISTERED_APPS_TBL, new String[] {
						"MAX(" + RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_ID] + ")"
				}, 
				null, 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return -1;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getAppListId: New SQL Cursor #rows:" + spaCursor.getCount());
		int riskListId = -1;
		if (spaCursor.getCount() > 0) {
			riskListId = spaCursor.getInt(0);
		}
		spaCursor.close();
		return riskListId;
	}

	public boolean hasRegisteredAppList(String appListId) throws SQLException {
		//---indicates if a particular threat list is present---
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasRegisteredAppList: checking for riskListId:" + appListId);
		if (appListId == null) return false;
		if (appListId.length() < 1) return false;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, RegisteredApps.REGISTERED_APPS_TBL, new String[] {
						RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_ID]
				}, 
				RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_ID] + " = '" + appListId + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return false;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasRegisteredAppList: New SQL Cursor - hasAppList() - appListId:" + appListId);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasRegisteredAppList: New SQL Cursor #rows:" + spaCursor.getCount());
		boolean foundList = (spaCursor.getCount() > 0);
		spaCursor.close();
		return foundList;
	}

	public boolean hasRegisteredAppListPackage(String appListPackageName) throws SQLException {
		//---indicates if a particular package is in a threat list---
		if (appListPackageName == null) return false;
		if (appListPackageName.length() < 1) return false;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, RegisteredApps.REGISTERED_APP_TBL, new String[] {
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_pkg_name_TXT]
				}, 
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_pkg_name_TXT] + " = '" + appListPackageName + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return false;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasRegisteredAppListPackage: New SQL Cursor - appListPackageName:" + appListPackageName);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasRegisteredAppListPackage: New SQL Cursor #rows:" + spaCursor.getCount());
		boolean foundPackage = (spaCursor.getCount() > 0);
		spaCursor.close();
		return foundPackage;
	}

	public boolean getAppListInfo(SDAppList scanList) throws SQLException {
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, RegisteredApps.REGISTERED_APPS_TBL, new String[] {
						RegisteredApps.ALL_APPS_COLUMNS
				}, 
				RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_ID] + " = " + scanList.appListID, 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return false;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getAppListInfo: New SQL Cursor #rows:" + spaCursor.getCount());
		if (spaCursor.getCount() > 0) {
			scanList.appListDate = spaCursor.getLong(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_creation_TS]));
			scanList.appListMinAppVersion = spaCursor.getInt(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_min_app_version_CD]));
			scanList.appListType = spaCursor.getInt(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_type_CD]));
			scanList.appListDownloadDate = spaCursor.getLong(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.risk_list_download_TS]));
		}
		spaCursor.close();
		return true;
	}

	public int insertAppListInfo(SpaXmlResultSpywareListInfo tmpListInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListInfo: Inserting tmpListInfo entry...");
		String INSERT_STR = 
				"INSERT INTO " + RegisteredApps.REGISTERED_APPS_TBL + " ("
						+ RegisteredApps.ALL_APP_COLUMNS
						+ ") VALUES ('" 
						+ tmpListInfo.mAppListID + "', '" 
						+ tmpListInfo.mRiskListType + "', '" 
						+ tmpListInfo.mRiskListDate + "', '" 
						+ tmpListInfo.mRiskListMinAppVersion + "', '" 
						+ System.currentTimeMillis() + "'); COMMIT;";
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListInfo: Inserting app list ID" + tmpListInfo.mAppListID);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListInfo: SQL : " + INSERT_STR);
		boolean inserted = false;
		try {
			inserted = execSQLString(INSERT_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "insertAppListInfo: Error executing " + RegisteredApps.REGISTERED_APPS_TBL + " entry SQL");
		}
		int appListID = -1;
		if (inserted) {
			Cursor mCursor = appDB.rawQuery(
					"SELECT " + RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.rlp_risk_list_ID] + " " 
							+ " FROM " + RegisteredApps.REGISTERED_APPS_TBL 
							+ " WHERE + " + RegisteredApps.REGISTERED_APPS_COLUMNS[RegisteredApps.rlp_risk_list_ID] 
									+ " = " + tmpListInfo.mAppListID
							, null);
			if (mCursor != null) { 
				if (mCursor.getCount() > 0) {
					mCursor.moveToFirst();
					appListID = mCursor.getInt(0);
				}
				mCursor.close();
			}
		} 
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListInfo: New SQL Cursor - insertAppListInfo");
		return appListID;
	}

	public boolean insertAppListPackageInfo(SpaXmlResultAppPackageInfo tmpPackageInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListPackageInfo: Inserting app list entry...");
		String INSERT_STR = 
				"INSERT INTO " + RegisteredApps.REGISTERED_APP_TBL + " (" +
						RegisteredApps.ALL_APP_COLUMNS
						+ ") VALUES ('" 
						+ tmpPackageInfo.mAppListID + "', '" 
						+ tmpPackageInfo.mPackageName + "', '" 
						+ safeString(tmpPackageInfo.mPackageAppName) + "', '" 
						+ tmpPackageInfo.mVersionCode + "', '" 
						+ safeString(tmpPackageInfo.mVersionName) + "', '" 
						+ tmpPackageInfo.mAppType + "', '" 
						+ safeString(tmpPackageInfo.mCreatedByDescr) + "', '" 
						+ safeString(tmpPackageInfo.mOtherNames) + "', '" 
						+ tmpPackageInfo.mFlags + "', '" 
						+ safeString(tmpPackageInfo.mRiskDescr) + "', '" 
						+ safeString(tmpPackageInfo.mRecommendAction) + "', '" 
						+ System.currentTimeMillis() + "', '" 
						+ tmpPackageInfo.updateDate + "'); COMMIT;";
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListPackageInfo: Adding package:" + tmpPackageInfo.mPackageName);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListPackageInfo: SQL : " + INSERT_STR);
		boolean inserted = false;
		try {
			inserted = execSQLString(INSERT_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "insertAppListPackageInfo: Error executing Entry SQL");
		}
		boolean packageInsert = false;
		if (inserted) {
			Cursor mCursor = appDB.rawQuery(
					"SELECT " + RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_list_ID] 
							+ " FROM " + RegisteredApps.REGISTERED_APP_TBL
							+ " WHERE " + RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_pkg_name_TXT] 
									+ " = '" + tmpPackageInfo.mPackageName + "'"
							, null);
			if (mCursor != null) { 
				if (mCursor.getCount() > 0) packageInsert = true;
				mCursor.close();
			}
		} 
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertAppListPackageInfo: New SQL Cursor - inserted data");
		return packageInsert;
	}

	public List<AppScanInfo> getRegisteredPackages() {
		List<AppScanInfo> tmplist = new ArrayList<AppScanInfo>();
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, RegisteredApps.REGISTERED_APP_TBL, 
				RegisteredApps.REGISTERED_APP_COLUMNS, 
				null, 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmplist;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: New SQL Cursor #rows:" + spaCursor.getCount());
		while (!spaCursor.isAfterLast()) {
			AppScanInfo tmpPkg = new AppScanInfo();
			tmpPkg = getRegisteredApp(spaCursor);
			tmpPkg.installedApp = true;
			tmplist.add(tmpPkg);
			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmplist;
	}

	public List<AppScanInfo> getRegisteredPackages(int riskList) {
		List<AppScanInfo> tmplist = new ArrayList<AppScanInfo>();
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: getting packages...");
		String SELECT_STR = 
				"SELECT " +
				//+ "risk_list_ID, risk_list_type_CD, risk_list_creation_TS, "
				//+ "risk_list_min_app_version_CD, risk_list_download_TS"
				RegisteredApps.ALL_APP_COLUMNS + ", " +
				AppInstalledInfo.ALL_COLUMNS
						
				+ " FROM " + AppInstalledInfo.PACKAGE_STATUS_TBL + " "
				+ " LEFT OUTER JOIN " + RegisteredApps.REGISTERED_APP_TBL + " ON "
				+ AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] 
						+ " = " + RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_package_ID]
				+ " AND " + RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_list_ID] 
						+ " = " + riskList;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: SQL : " + SELECT_STR);
		Cursor spaCursor =
				appDB.rawQuery(SELECT_STR, null);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: New SQL Cursor #rows:" + spaCursor.getCount());
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmplist;
		int scanItemsTotal = spaCursor.getCount();
		int scanItemsProgress = 0;
		if (mScanPkgListUpdateListener != null) 
			mScanPkgListUpdateListener.updateScanProgress(scanItemsTotal, scanItemsProgress);
		while (!spaCursor.isAfterLast()) {
			AppScanInfo tmpPkg = new AppScanInfo();
			tmpPkg = getAppInfo(spaCursor);
			tmpPkg.installedApp = true;
			tmpPkg.appListId = spaCursor.getInt(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_list_ID]));
			tmpPkg.pkgType = spaCursor.getString(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_app_type_TXT]));
			tmpPkg.pkgCreatedBy = spaCursor.getString(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_created_by_DESCR]))+"";
			tmpPkg.pkgOtherNames = spaCursor.getString(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_other_name_TXT]))+"";
			tmpPkg.riskDescr = spaCursor.getString(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_DESCR]))+"";
			tmpPkg.recommendAction = spaCursor.getString(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_recommend_action_DESCR]))+"";
			tmpPkg.entryDt = spaCursor.getLong(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_entry_TS]));
			tmpPkg.updateDt = spaCursor.getLong(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_update_TS]));
			if (!spaCursor.isNull(spaCursor.getColumnIndex(
					RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_flags]))) {
				tmpPkg.pkgFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_flags]));
			}
			tmpPkg.userFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_flags]));
			tmpPkg.userDt = spaCursor.getLong(spaCursor.getColumnIndex(
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_TS]));
			//tmpPkg.print(TAG);
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "getRegisteredPackages: pkg flags: " + tmpPkg.pkgFlgs);
			tmplist.add(tmpPkg);
			spaCursor.moveToNext();
			scanItemsProgress++;
			if (mScanPkgListUpdateListener != null && (scanItemsProgress%10) == 0) 
				mScanPkgListUpdateListener.updateScanProgress(scanItemsTotal, scanItemsProgress);
		}
		spaCursor.close();
		if (mScanPkgListUpdateListener != null) 
			mScanPkgListUpdateListener.updateScanProgress(scanItemsTotal, scanItemsProgress);
		return tmplist;
	}

	/**
	 * 
	 * Get installed packages info
	 * 
	 */

	public boolean hasPackage(String packageName) throws SQLException {
		//---indicates if a particular package is in a threat list---
		if (packageName == null) return false;
		if (packageName.length() < 1) return false;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppInstalledInfo.PACKAGE_STATUS_TBL, new String[] {
						AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_name_TXT]
				}, 
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_name_TXT] + " = '" + packageName + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return false;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasPackage: New SQL Cursor - hasPackage:" + packageName);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "hasPackage: New SQL Cursor #rows:" + spaCursor.getCount());
		boolean foundPackage = (spaCursor.getCount() > 0);
		spaCursor.close();
		return foundPackage;
	}

	public int getPackageId(String packageName) {
		int pgkId = 0;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppInstalledInfo.PACKAGE_STATUS_TBL, new String[] {
						AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID]
				}, 
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_name_TXT] + " = '" + packageName + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return pgkId;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getPackageId: New SQL Cursor #rows:" + spaCursor.getCount());
		pgkId = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID]));
		spaCursor.close();
		return pgkId;
	}

	public AppScanInfo getInstalledPackage(AppScanInfo tmpPkg) {
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, "getInstalledPackage: package: " + tmpPkg.pkgId);
		if (tmpPkg.pkgId < 1) return tmpPkg;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppInstalledInfo.PACKAGE_STATUS_TBL, 
						AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS, 
						AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = '" + tmpPkg.pkgId + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpPkg;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getInstalledPackage: New SQL Cursor #rows:" + spaCursor.getCount());
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, "getInstalledPackage: found package: " + tmpPkg.pkgName);
		tmpPkg = getAppInfo(spaCursor);
		spaCursor.close();
		return tmpPkg;
	}

	public AppScanInfo getInstalledPackage(String packageName) {
		AppScanInfo tmpPkg = new AppScanInfo();
		tmpPkg.pkgName = packageName;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppInstalledInfo.PACKAGE_STATUS_TBL, 
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS, 
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_name_TXT] + " = '" + packageName + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpPkg;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getInstalledPackage: New SQL Cursor #rows:" + spaCursor.getCount());
		tmpPkg = getAppInfo(spaCursor);
		spaCursor.close();
		return tmpPkg;
	}

	public ArrayList<AppScanInfo> getInstalledPackages() {
		ArrayList<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppInstalledInfo.PACKAGE_STATUS_TBL, 
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS, 
				null, 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpList;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getInstalledPackages: New SQL Cursor #rows:" + spaCursor.getCount());
		AppScanInfo tmpPkg;
		while (!spaCursor.isAfterLast()) {
			tmpPkg = new AppScanInfo();
			tmpPkg = getAppInfo(spaCursor);
			tmpList.add(tmpPkg);
			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmpList;
	}

	public AppScanInfo getScanPackageInfo(AppScanInfo tmpPkg) {
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, "getPackage: package: " + tmpPkg.pkgId);
		if (tmpPkg.pkgId < 1) return tmpPkg;
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppInstalledInfo.PACKAGE_STATUS_TBL,
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS, 
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = '" + tmpPkg.pkgId + "'", 
				null,
				null, 
				null, 
				null, 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpPkg;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getPackage: New SQL Cursor #rows:" + spaCursor.getCount());
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, "getPackage: found package: " + tmpPkg.pkgName);
		tmpPkg = getAppInfo(spaCursor);
		spaCursor.close();
		return tmpPkg;
	}

	public List<AppScanInfo> getRegisteredPackages(int riskList, int pkgId) {
		List<AppScanInfo> tmplist = new ArrayList<AppScanInfo>();
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: getting threats...");
		String SELECT_STR = 
				"SELECT " +
				//+ "risk_list_ID, risk_list_type_CD, risk_list_creation_TS, "
				//+ "risk_list_min_app_version_CD, risk_list_download_TS"
				RegisteredApps.ALL_APP_COLUMNS + ", " +
				AppInstalledInfo.ALL_COLUMNS

				+ "FROM " + AppInstalledInfo.PACKAGE_STATUS_TBL + " "
				+ "LEFT OUTER JOIN " + AppInstalledInfo.PACKAGE_STATUS_TBL + " ON"
				+ " " + AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_app_name_TXT] + " = rlp_app_name_TXT "
				//        	+ " AND rlp_risk_list_ID = " + riskList
				+ " WHERE " + AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = " + pkgId;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: SQL : " + SELECT_STR);
		Cursor spaCursor =
				appDB.rawQuery(SELECT_STR, null);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "getRegisteredPackages: New SQL Cursor #rows:" + spaCursor.getCount());
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmplist;
		while (!spaCursor.isAfterLast()) {
			if (pkgId == spaCursor.getInt(spaCursor.getColumnIndex(
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID]))) {
				AppScanInfo tmpPkg = new AppScanInfo();
				tmpPkg = getAppInfo(spaCursor);
				tmpPkg.installedApp = true;
				tmpPkg.pkgType = spaCursor.getString(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_app_type_TXT]));
				tmpPkg.pkgCreatedBy = spaCursor.getString(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_created_by_DESCR]))+"";
				tmpPkg.pkgOtherNames = spaCursor.getString(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_other_name_TXT]))+"";
				tmpPkg.riskDescr = spaCursor.getString(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_DESCR]))+"";
				//tmpPkg.listDt = spaCursor.getLong(spaCursor.getColumnIndex("risk_list_creation_TS"));
				tmpPkg.appListId = spaCursor.getInt(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_list_ID]));
				tmpPkg.recommendAction = spaCursor.getString(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_recommend_action_DESCR]))+"";
				tmpPkg.entryDt = spaCursor.getLong(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_entry_TS]));
				if (spaCursor.isNull(spaCursor.getColumnIndex(
						RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_flags]))) {
					tmpPkg.pkgFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
							AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_flags]));
				} else {
					tmpPkg.pkgFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
							RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_flags]));
				}
				//tmpPkg.print(TAG);
				tmplist.add(getAppInfo(spaCursor));
			}
			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmplist;
	}

	public long updatePackageDt(AppScanInfo tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updatePackageDt: Updating package date entry...");
		ContentValues cv = new ContentValues();
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_version_CD], 
				tmpPkgInfo.versionCode);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_flags], 
				tmpPkgInfo.pkgFlgs);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_update_TS], 
				tmpPkgInfo.updateDt);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_uninstall_TS], 
				tmpPkgInfo.uninstallDt);
		long pkg_id = 0;
		String[] whereArgs = {tmpPkgInfo.pkgId+""};
		try {
			pkg_id = appDB.update(AppInstalledInfo.PACKAGE_STATUS_TBL, 
					cv,
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = ? ",
					whereArgs
					);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "updatePackageDt: Error executing Package Update SQL");
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "Error e:" + e.toString());
			return -1;
		}
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updatePackageDt: Row updated");
		return pkg_id;
	}

	public ArrayList<SDAppList> getAllScanList() throws SQLException {
		//---indicates if a particular scan list is present---
		ArrayList<SDAppList> tmpLists = new ArrayList<SDAppList>();
		SDAppList tmpList;
		AppScanInfo tmpPkg;
		int scanId = 0, scanIdx = 0;
		String SELECT_STR = 
				"SELECT " +
						SDAppList.ALL_COLUMNS + ", " +
						AppScanInfo.ALL_COLUMNS + ", " +
						AppInstalledInfo.ALL_COLUMNS
								
						+ " FROM " + SDAppList.APP_LIST_SCAN_TBL + " , " + AppScanInfo.SCAN_ITEMS_TBL + ", "
						+ AppInstalledInfo.PACKAGE_STATUS_TBL + " "
						+ " WHERE " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + " = " 
							+ AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_scan_ID] + " "
						+ " AND " + AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_package_ID] + " = " 
							+ AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " "
						+ " ORDER BY " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + " DESC ";
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getAllScanList: SQL : " + SELECT_STR);
		Cursor spaCursor =
				appDB.rawQuery(SELECT_STR, null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpLists;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getAllScanList: New SQL Cursor - getAllScanList()");
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getAllScanList: New SQL Cursor #rows:" + spaCursor.getCount());
		while (!spaCursor.isAfterLast()) {
			if (scanId != spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]))) {
				tmpList = getAppScanList(spaCursor);
				tmpLists.add(tmpList);
				scanIdx = tmpLists.indexOf(tmpList);
				scanId = tmpList.scanID;
				if (SdmmsConsts.DEBUG_DB) 
					Log.i(TAG, TAG2 + "getAllScanList: Getting scan list:" + tmpList.scanID);
			} 
			tmpPkg = getAppInfo(spaCursor);
			tmpPkg.scanListId = spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]));
			tmpPkg.appListId = spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_app_list_version_ID]));
			//tmpPkg.listDt = spaCursor.getLong(spaCursor.getColumnIndex("risk_list_creation_TS"));
			tmpPkg.appCats = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_risk_category_Flg]));
			tmpPkg.userFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_flags]));
			tmpPkg.user_priority = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_priority]));
			tmpPkg.userDt = spaCursor.getLong(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_TS]));

			tmpLists.get(scanIdx).scanList.add(tmpPkg);
			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmpLists;
	}
	
	public SDAppList getAllScanList(int scan_id) throws SQLException {
		//---indicates if a particular scan list is present---
		SDAppList tmpList = new SDAppList();
		AppScanInfo tmpPkg;
		int scanId = 0, scanIdx = 0;
		String SELECT_STR = 
				"SELECT " +
						SDAppList.ALL_COLUMNS + ", " +
						AppScanInfo.ALL_COLUMNS + ", " +
						AppInstalledInfo.ALL_COLUMNS
								
						+ " FROM " + SDAppList.APP_LIST_SCAN_TBL + " , " + AppScanInfo.SCAN_ITEMS_TBL + ", "
						+ AppInstalledInfo.PACKAGE_STATUS_TBL + " "
						+ " WHERE " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] 
								+ " = " + AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_scan_ID] + " "
						+ " AND " + AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_package_ID] 
								+ " = " + AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " "
						+ " AND " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] 
								+ " = '" + scan_id + "' "
						+ " ORDER BY " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + " DESC ";
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getAllScanList: SQL : " + SELECT_STR);
		Cursor spaCursor =
				appDB.rawQuery(SELECT_STR, null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpList;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getAllScanList: New SQL Cursor - getAllScanList()");
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getAllScanList: New SQL Cursor #rows:" + spaCursor.getCount());
		while (!spaCursor.isAfterLast()) {
			if (scanId != spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]))) {
				tmpList = getAppScanList(spaCursor);
				scanId = tmpList.scanID;
				if (SdmmsConsts.DEBUG_DB) 
					Log.i(TAG, TAG2 + "getAllScanList: Getting scan list:" + tmpList.scanID);
			} 
			tmpPkg = getAppInfo(spaCursor);
			tmpPkg.scanListId = spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]));
			tmpPkg.appListId = spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_app_list_version_ID]));
			//tmpPkg.listDt = spaCursor.getLong(spaCursor.getColumnIndex("risk_list_creation_TS"));
			tmpPkg.appCats = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_category_ID]));
			tmpPkg.userFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_flags]));
			tmpPkg.user_priority = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_priority]));
			tmpPkg.userDt = spaCursor.getLong(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_TS]));
			tmpList.addPkg(tmpPkg);
			
			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmpList;
	}
	
	public SDAppList getCurrentScanList() throws SQLException {
		//---indicates if a particular scan list is present---
		SDAppList tmpList = new SDAppList();
		AppScanInfo tmpPkg;
		int scanId = 0;
		String SELECT_STR = 
				"SELECT " +
						SDAppList.ALL_COLUMNS + ", " +
						AppScanInfo.ALL_COLUMNS + ", " +
						AppInstalledInfo.ALL_COLUMNS
								
						+ " FROM " + SDAppList.APP_LIST_SCAN_TBL + " , " + AppScanInfo.SCAN_ITEMS_TBL + ", "
						+ AppInstalledInfo.PACKAGE_STATUS_TBL + " "
						+ " WHERE " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] 
								+ " = " + AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_scan_ID] + " "
						+ " AND " + AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_package_ID] 
								+ " = " + AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " "
						+ " AND " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] + " & " + SDAppList.CURRENT_SCAN 
						+ " = " + SDAppList.CURRENT_SCAN
						+ " ORDER BY " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + " DESC ";
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getCurrentScanList: SQL : " + SELECT_STR);
		Cursor spaCursor =
				appDB.rawQuery(SELECT_STR, null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmpList;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getCurrentScanList: New SQL Cursor - getCurrentScanList()");
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getCurrentScanList: New SQL Cursor #rows:" + spaCursor.getCount());
		while (!spaCursor.isAfterLast()) {
			if (scanId != spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]))) {
				tmpList = getAppScanList(spaCursor);
				scanId = tmpList.scanID;
				if (SdmmsConsts.DEBUG_DB) 
					Log.i(TAG, TAG2 + "getCurrentScanList: Getting scan list:" + tmpList.scanID);
			} 
			tmpPkg = getAppInfo(spaCursor);
			tmpPkg.scanListId = spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]));
			tmpPkg.appListId = spaCursor.getInt(spaCursor.getColumnIndex(
					SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_app_list_version_ID]));
			//tmpPkg.listDt = spaCursor.getLong(spaCursor.getColumnIndex("risk_list_creation_TS"));
			tmpPkg.appCats = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_category_ID]));
			tmpPkg.userFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_flags]));
			tmpPkg.user_priority = spaCursor.getInt(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_priority]));
			tmpPkg.userDt = spaCursor.getLong(spaCursor.getColumnIndex(
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_TS]));
			tmpList.addPkg(tmpPkg);

			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmpList;
	}
	
	private SDAppList getAppScanList(Cursor spaCursor) {
		SDAppList tmpList = new SDAppList();
		tmpList.scanID = spaCursor.getInt(spaCursor.getColumnIndex(
				SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]));
		tmpList.scanStartDt = spaCursor.getLong(spaCursor.getColumnIndex(
				SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_start_TS]));
		tmpList.scanEndDt = spaCursor.getLong(spaCursor.getColumnIndex(
				SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_end_TS]));
		tmpList.scanFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
				SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG]));
		tmpList.appListID = spaCursor.getInt(spaCursor.getColumnIndex(
				SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_app_list_version_ID]));
		return tmpList;
	}
	
	private AppScanInfo getAppInfo(Cursor spaCursor) {
		AppScanInfo tmpPkg = new AppScanInfo();
		tmpPkg.pkgId = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID]));
		tmpPkg.appCatId = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_category_ID]));
		tmpPkg.versionCode = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_version_CD]));
		tmpPkg.pkgName = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_name_TXT]));
		tmpPkg.appName = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_app_name_TXT]));
		// missing category code
		tmpPkg.versionName = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_version_name_TXT]));
		tmpPkg.sharedUserId = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_shared_user_ID]));
		tmpPkg.sharedUserLbl = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_shared_user_LBL]));
		tmpPkg.installDt = spaCursor.getLong(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_install_TS]));
		tmpPkg.updateDt = spaCursor.getLong(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_update_TS]));
		tmpPkg.uninstallDt = spaCursor.getLong(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_uninstall_TS]));
		tmpPkg.entryDt = spaCursor.getLong(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_entry_TS]));
		tmpPkg.recPriority = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_receiver_priority_num]));
		tmpPkg.appCats = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_risk_category_Flg]));
		tmpPkg.assigned_perm_type = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_assigned_perm_type]));
		tmpPkg.sds_priority = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sds_priority]));
		tmpPkg.user_priority = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_priority]));
		tmpPkg.featureFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_feature_flgs]));
		tmpPkg.pkgFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_flags]));
		tmpPkg.userFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_flags]));
		tmpPkg.userDt = spaCursor.getLong(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_TS]));
		tmpPkg.smsReceivedReceiver = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_received_receiver]));
		tmpPkg.smsDeliveredReceiver = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_delivered_receiver]));
		tmpPkg.wapPushDeliverReceiver = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_wap_push_deliver_receiver]));
		tmpPkg.smsRespondViaMessage = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_respond_via_message]));
		tmpPkg.smsSend = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_send]));
		tmpPkg.smsSendTo = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_send_to]));
		tmpPkg.smsTo = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_to]));
		tmpPkg.sms = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms]));
		tmpPkg.mmsTo = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_mms_to]));
		tmpPkg.mms = spaCursor.getString(spaCursor.getColumnIndex(
				AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_mms]));
		//tmpPkg.print(TAG);
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + " Getting scan package:" + tmpPkg.appName);
		return tmpPkg;
	}
	
	private AppScanInfo getRegisteredApp(Cursor spaCursor) {
		AppScanInfo tmpPkg = new AppScanInfo();
		tmpPkg.appListId = spaCursor.getInt(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_list_ID]));
		tmpPkg.pkgName = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_package_ID]));
		tmpPkg.appName = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_pkg_name_TXT]));
		tmpPkg.versionCode = spaCursor.getInt(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_version_CD]));
		tmpPkg.versionName = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_version_name_TXT]));
		tmpPkg.pkgType = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_app_type_TXT]));
		tmpPkg.pkgCreatedBy = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_created_by_DESCR]));
		tmpPkg.pkgOtherNames = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_other_name_TXT]));
		tmpPkg.pkgFlgs = spaCursor.getInt(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_flags]));
		tmpPkg.riskDescr = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_risk_DESCR]));
		tmpPkg.recommendAction = spaCursor.getString(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_recommend_action_DESCR]));
		tmpPkg.entryDt = spaCursor.getLong(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_entry_TS]));
		tmpPkg.updateDt = spaCursor.getLong(spaCursor.getColumnIndex(
				RegisteredApps.REGISTERED_APP_COLUMNS[RegisteredApps.rlp_update_TS]));
		return tmpPkg;
	}

	public int insertScanList(SDAppList scanList) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertScanList: Inserting scanned items list...");
		String INSERT_STR = 
				"INSERT INTO " + SDAppList.APP_LIST_SCAN_TBL + " (" +
						SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_start_TS] + ", " +
						SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_end_TS] + ", " +
						SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] + ", " +
						SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_app_list_version_ID]  
						+ ") VALUES ('" 
						+ scanList.scanStartDt + "', '" 
						+ scanList.scanEndDt + "', '" 
						+ scanList.scanFlgs + "', '" 
						+ scanList.appListID + "'); COMMIT;";
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertScanList: Adding scanned list started:" + scanList.scanStartDt);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertScanList: SQL : " + INSERT_STR);
		boolean inserted = false;
		try {
			inserted = execSQLString(INSERT_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "insertScanList: Error executing ScanList insert SQL");
		}
		int scanId = -1;
		if (inserted) {
			Cursor mCursor = appDB.rawQuery(
					"SELECT MAX(" + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + ") " 
							+ " FROM " + SDAppList.APP_LIST_SCAN_TBL
							, null);
			if (mCursor != null) { 
				if (mCursor.getCount() > 0) {
					mCursor.moveToFirst();
					scanId = mCursor.getInt(0);
				}
				mCursor.close();
			}
		} 
		scanList.scanID = scanId;
        for (AppScanInfo tmpItem: scanList.scanList) {
        	tmpItem.scanListId = scanList.scanID;
        	if (SdmmsConsts.DEBUG_SCAN_DB) 
        		Log.i(TAG, "scanApps: scanItemFlgs: " + tmpItem.pkgFlgs  
        			+ " scanitem: " + tmpItem.pkgName);
        	insertScanItem(tmpItem);
        	if (!hasPackage(tmpItem.pkgName)) {
        		tmpItem.pkgId = insertPackageInfo(tmpItem);
        	} else {
        		updatePackageDt(tmpItem);
        	}
        }
        setCurrentScanList(scanList.scanID);
		return scanId;
	}

	public int insertScanItem(AppScanInfo tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "insertScanItem: Inserting scanned item...");
		int packageInsert = -1;
		Cursor mCursor = appDB.rawQuery(
				"SELECT MAX(" + AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo._id] + ")" 
						+ " FROM " + AppScanInfo.SCAN_ITEMS_TBL
						, null);
		if (mCursor != null) { 
			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				packageInsert = mCursor.getInt(0) + 1;
			}
			mCursor.close();
		} else return packageInsert;
		tmpPkgInfo._ID= packageInsert;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "insertScanItem: flags: " + tmpPkgInfo.pkgFlgs);
		String INSERT_STR = 
				"INSERT INTO " + AppScanInfo.SCAN_ITEMS_TBL + " (" +
						AppScanInfo.ALL_COLUMNS
						+ ") VALUES ('" 
						+ tmpPkgInfo._ID + "', '" 
						+ tmpPkgInfo.scanListId + "', '" 
						+ tmpPkgInfo.pkgId + "', '" 
						+ tmpPkgInfo.versionCode + "', '" 
						+ tmpPkgInfo.appCatId + "', '" 
						+ tmpPkgInfo.listDt + "', '" 
						+ safeString(tmpPkgInfo.pkgName) + "', '" 
						+ safeString(tmpPkgInfo.appName) + "', '" 
						+ safeString(tmpPkgInfo.versionName) + "', '" 
						+ tmpPkgInfo.sharedUserId + "', '" 
						+ safeString(tmpPkgInfo.sharedUserLbl) + "', '" 
						+ tmpPkgInfo.installDt + "', '" 
						+ tmpPkgInfo.updateDt + "', '" 
						+ tmpPkgInfo.uninstallDt + "', '" 
						+ tmpPkgInfo.entryDt + "', '" 
						+ tmpPkgInfo.recPriority + "', '" 
						+ tmpPkgInfo.appCats + "', '" 
						+ tmpPkgInfo.assigned_perm_type + "', '" 
						+ tmpPkgInfo.sds_priority + "', '" 
						+ tmpPkgInfo.user_priority + "', '" 
						+ tmpPkgInfo.featureFlgs + "', '" 
						+ tmpPkgInfo.pkgFlgs + "', '" 
						+ tmpPkgInfo.userFlgs + "', '" 
						+ tmpPkgInfo.userDt + "', '" 
						+ tmpPkgInfo.smsReceivedReceiver + "', '" 
						+ tmpPkgInfo.smsDeliveredReceiver + "', '" 
						+ tmpPkgInfo.wapPushDeliverReceiver + "', '" 
						+ tmpPkgInfo.smsRespondViaMessage + "', '" 
						+ tmpPkgInfo.smsSend + "', '" 
						+ tmpPkgInfo.smsSendTo + "', '" 
						+ tmpPkgInfo.smsTo + "', '" 
						+ tmpPkgInfo.sms + "', '" 
						+ tmpPkgInfo.mmsTo + "', '" 
						+ tmpPkgInfo.mms + "'); COMMIT;";
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "insertScanItem: Adding scanned item:" + tmpPkgInfo.pkgName);
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "insertScanItem: item flags:" + tmpPkgInfo.pkgFlgs);
		//if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "insertScanItem: SQL : " + INSERT_STR);
		boolean inserted = false;
		try {
			inserted = execSQLString(INSERT_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_DB) 
				Log.i(TAG, TAG2 + "insertScanItem: Error executing ScanItem insert SQL");
		}
		if (inserted) {
			return packageInsert;
		} else return -1;
	}

	public int insertPackageInfo(AppScanInfo tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPackageInfo: Inserting package info entry...");
		int packageInsert = -1;
		Cursor mCursor = appDB.rawQuery(
				"SELECT MAX(" + AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + ")"
						+ " FROM " + AppInstalledInfo.PACKAGE_STATUS_TBL
						, null);
		if (mCursor != null) { 
			if (mCursor.getCount() > 0) {
				mCursor.moveToFirst();
				packageInsert = mCursor.getInt(0) + 1;
			}
			mCursor.close();
		} else return packageInsert;
		tmpPkgInfo.pkgId = packageInsert;
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPackageInfo: flags: " + tmpPkgInfo.pkgFlgs);
		String INSERT_STR = 
				"INSERT INTO " + AppInstalledInfo.PACKAGE_STATUS_TBL + " (" +
						AppInstalledInfo.ALL_COLUMNS
						+ ") VALUES ('" 
						+ tmpPkgInfo.pkgId + "', '" 
						+ tmpPkgInfo.versionCode + "', '" 
						+ tmpPkgInfo.appCatId + "', '" 
						+ safeString(tmpPkgInfo.pkgName) + "', '" 
						+ safeString(tmpPkgInfo.appName) + "', '" 
						+ safeString(tmpPkgInfo.versionName) + "', '" 
						+ tmpPkgInfo.sharedUserId + "', '" 
						+ safeString(tmpPkgInfo.sharedUserLbl) + "', '" 
						+ tmpPkgInfo.installDt + "', '" 
						+ tmpPkgInfo.updateDt + "', '" 
						+ tmpPkgInfo.uninstallDt + "', '" 
						+ tmpPkgInfo.listDt + "', '" 
						+ tmpPkgInfo.recPriority + "', '" 
						+ tmpPkgInfo.appCats + "', '" 
						+ tmpPkgInfo.assigned_perm_type + "', '" 
						+ tmpPkgInfo.sds_priority + "', '" 
						+ tmpPkgInfo.user_priority + "', '" 
						+ tmpPkgInfo.featureFlgs + "', '" 
						+ tmpPkgInfo.pkgFlgs + "', '" 
						+ tmpPkgInfo.userFlgs + "', '" 
						+ tmpPkgInfo.userDt + "', '" 
						+ tmpPkgInfo.smsReceivedReceiver + "', '" 
						+ tmpPkgInfo.smsDeliveredReceiver + "', '" 
						+ tmpPkgInfo.wapPushDeliverReceiver + "', '" 
						+ tmpPkgInfo.smsRespondViaMessage + "', '" 
						+ tmpPkgInfo.smsSend + "', '" 
						+ tmpPkgInfo.smsSendTo + "', '" 
						+ tmpPkgInfo.smsTo + "', '" 
						+ tmpPkgInfo.sms + "', '" 
						+ tmpPkgInfo.mmsTo + "', '" 
						+ tmpPkgInfo.mms + "'); COMMIT;";
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPackageInfo: Adding package:" + tmpPkgInfo.pkgName);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPackageInfo: SQL : " + INSERT_STR);
		boolean inserted = false;
		try {
			inserted = execSQLString(INSERT_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "insertPackageInfo: Error executing insert SQL");
		}
		if (inserted) {
			return packageInsert;
		} else return -1; 
	}

	public boolean insertPermInfo(int pkgId, int filterId, UsesFilter tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPermInfo: Inserting perm/filter info entry...");
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPermInfo: priority " + tmpPkgInfo.permPriority);
		String INSERT_STR = 
				"INSERT INTO " + AppUsesPermissions.APP_USES_PERMISSIONS_TBL + " (" +
						AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[0] + ", " +
						AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[1] + ", " +
						AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[2] + ", " +
						AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[3] + ", " +
						AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[4]
						+ ") VALUES ('" 
						+ filterId + "', '" 
						+ tmpPkgInfo.permId + "', '" 
						+ pkgId + "', '" 
						+ tmpPkgInfo.permPriority + "', '" 
						+ tmpPkgInfo.permProtectionLvl + "'); COMMIT;";
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPermInfo: Adding perm/filterId: " + filterId);
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "insertPermInfo: SQL : " + INSERT_STR);
		boolean inserted = false;
		try {
			inserted = execSQLString(INSERT_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "insertPermInfo: Error executing insert SQL");
		}
		return inserted;
	}

	public List<AppScanInfo> getScanListPackages(int scan_id) {
		List<AppScanInfo> tmplist = new ArrayList<AppScanInfo>();
		Cursor spaCursor;
		spaCursor =
				appDB.query(true, AppScanInfo.SCAN_ITEMS_TBL,
				AppScanInfo.SCAN_ITEMS_COLUMNS, 
				AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_scan_ID] + " = " + scan_id, 
				null,
				null, 
				null, 
				AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_pkg_name_TXT] + " ASC", 
				null);
		if (spaCursor != null) {
			spaCursor.moveToFirst();
		} else return tmplist;
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "getScanListPackages: New SQL Cursor #rows:" + spaCursor.getCount());
		while (!spaCursor.isAfterLast()) {
			AppScanInfo tmpPkg = new AppScanInfo();
			tmpPkg = getAppInfo(spaCursor);
			tmplist.add(tmpPkg);
			spaCursor.moveToNext();
		}
		spaCursor.close();
		return tmplist;
	}

	public int updatePackageScanInfo(SDAppList scanList) {
		int count = 0;
        for (AppScanInfo tmpPkgInfo: scanList.scanList) {
			if (SdmmsConsts.DEBUG_DB) 
				Log.i(TAG, TAG2 + "updatePackageScanInfo: Updating PackageItem entry...");
			tmpPkgInfo.pkgId = getPackageId(tmpPkgInfo.pkgName);
			ContentValues cv = new ContentValues();
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_flags], tmpPkgInfo.pkgFlgs);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_received_receiver], tmpPkgInfo.smsReceivedReceiver);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_delivered_receiver], tmpPkgInfo.smsDeliveredReceiver);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_respond_via_message], tmpPkgInfo.smsRespondViaMessage);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_send], tmpPkgInfo.smsSend);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_send_to], tmpPkgInfo.smsSendTo);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms], tmpPkgInfo.sms);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_to], tmpPkgInfo.smsTo);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_mms], tmpPkgInfo.mms);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_mms_to], tmpPkgInfo.mmsTo);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_feature_flgs], tmpPkgInfo.getFeatureFlgs());
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sds_priority], tmpPkgInfo.sds_priority);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_risk_category_Flg], tmpPkgInfo.appCats);
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_receiver_priority_num], tmpPkgInfo.getSmsReceiverPriority());
			cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_entry_TS], tmpPkgInfo.listDt);
			long pkg_id = 0;
			String[] whereArgs = {tmpPkgInfo.pkgId+""};
			try {
				pkg_id = appDB.update(AppInstalledInfo.PACKAGE_STATUS_TBL, 
						cv,
						AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = ? ",
						whereArgs
						);
				count++;
			} catch (Exception e) {
				if (SdmmsConsts.DEBUG_DB) 
					Log.i(TAG, TAG2 + "updatePackageScanInfo: Error executing PackageItem Update SQL");
				if (SdmmsConsts.DEBUG_DB) 
					Log.i(TAG, TAG2 + "Error e:" + e.toString());
				return -1;
			}
			if (SdmmsConsts.DEBUG_DB) 
				Log.i(TAG, TAG2 + "updatePackageScanInfo: Row updated");
        }
		return count;
	}

	public long updateScanItemUserInfo(AppScanInfo tmpPkgInfo) {
		//if (SdmmsConsts.DEBUG_SCAN_DB) 
		Log.i(TAG, TAG2 + "updateScanItemUserInfo: Updating ScanItem entry...");
		ContentValues cv = new ContentValues();
		cv.put(AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_flags], tmpPkgInfo.userFlgs);
		cv.put(AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_priority], tmpPkgInfo.user_priority);
		cv.put(AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_user_TS], tmpPkgInfo.userDt);
		long pkg_id = 0;
		String[] whereArgs = {tmpPkgInfo.pkgId+"", tmpPkgInfo.scanListId+""};
		try {
			pkg_id = appDB.update(AppScanInfo.SCAN_ITEMS_TBL, 
					cv,
					AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_package_ID] + " = ? AND " +
							AppScanInfo.SCAN_ITEMS_COLUMNS[AppScanInfo.si_scan_ID] + " = ? ",
					whereArgs
					);
		} catch (Exception e) {
			//    		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updateScanItemUserInfo: Error executing ScanItem Update SQL");
			//    		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "Error e:" + e.toString());
			return -1;
		}
		//      if (SdmmsConsts.DEBUG_SCAN_DB) 
		Log.i(TAG, TAG2 + "updateScanItemUserInfo: Row updated, tmpPkgInfo.pkgId: "
				+ tmpPkgInfo.pkgId + ", tmpPkgInfo.scanListId: " + tmpPkgInfo.scanListId);
		return pkg_id;
	}

	public long updatePackageUserInfo(AppScanInfo tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updatePackageUserInfo: Updating PackageItem entry...");
		ContentValues cv = new ContentValues();
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_flags], tmpPkgInfo.userFlgs);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_priority], tmpPkgInfo.user_priority);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_user_TS], tmpPkgInfo.userDt);
		long pkg_id = 0;
		String[] whereArgs = {tmpPkgInfo.pkgId+""};
		try {
			pkg_id = appDB.update(AppInstalledInfo.PACKAGE_STATUS_TBL, 
					cv,
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = ? ",
					whereArgs
					);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "updatePackageUserInfo: Error executing PackageItem Update SQL");
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "Error e:" + e.toString());
			return -1;
		}
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updatePackageUserInfo: Row updated");
		return pkg_id;
	}

	public long updatePackageFeatures(AppScanInfo tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updatePackageUserInfo: Updating PackageItem entry...");
		ContentValues cv = new ContentValues();
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_flags], tmpPkgInfo.pkgFlgs);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_received_receiver], tmpPkgInfo.smsReceivedReceiver);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_delivered_receiver], tmpPkgInfo.smsDeliveredReceiver);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_respond_via_message], tmpPkgInfo.smsRespondViaMessage);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_send], tmpPkgInfo.smsSend);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_send_to], tmpPkgInfo.smsSendTo);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms], tmpPkgInfo.sms);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_to], tmpPkgInfo.smsTo);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_mms], tmpPkgInfo.mms);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_mms_to], tmpPkgInfo.mmsTo);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_feature_flgs], tmpPkgInfo.getFeatureFlgs());
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sds_priority], tmpPkgInfo.sds_priority);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_risk_category_Flg], tmpPkgInfo.appCats);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_sms_receiver_priority_num], tmpPkgInfo.getSmsReceiverPriority());
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_entry_TS], tmpPkgInfo.listDt);
		long pkg_id = 0;
		String[] whereArgs = {tmpPkgInfo.pkgId+""};
		try {
			pkg_id = appDB.update(AppInstalledInfo.PACKAGE_STATUS_TBL, 
					cv,
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = ? ",
					whereArgs
					);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "updatePackageFeatures: Error executing PackageItem Update SQL");
			if (SdmmsConsts.DEBUG_SCAN_DB) 
				Log.i(TAG, TAG2 + "Error e:" + e.toString());
			return -1;
		}
		if (SdmmsConsts.DEBUG_SCAN_DB) 
			Log.i(TAG, TAG2 + "updatePackageFeatures: Row updated");
		return pkg_id;
	}

	public long updatePackageItemUninstall(AppScanInfo tmpPkgInfo) {
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "updatePackageItemUninstall: Updating ScanItem entry...");
		ContentValues cv = new ContentValues();
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_flags], tmpPkgInfo.pkgFlgs);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_version_CD], tmpPkgInfo.versionCode);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_update_TS], tmpPkgInfo.updateDt);
		cv.put(AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_uninstall_TS], tmpPkgInfo.uninstallDt);
		long pkg_id = 0;
		String[] whereArgs = {tmpPkgInfo.pkgId+""};
		try {
			pkg_id = appDB.update(AppInstalledInfo.PACKAGE_STATUS_TBL, 
					cv,
					AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = ? ",
					whereArgs
					);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_DB) 
				Log.i(TAG, TAG2 + "updatePackageItemUninstall: Error executing ScanItem Update SQL");
			if (SdmmsConsts.DEBUG_DB) 
				Log.i(TAG, TAG2 + "Error e:" + e.toString());
			return -1;
		}
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "updateScanItemUninstall: Row updated");
		if (mScanListListener != null)
			mScanListListener.onScanUpdate();
		return pkg_id;
	}

	public int setCurrentScanList(int scan_ID) {
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "setCurrentScanList: Updating current list...");
		String UPDATE_STR = 
				"UPDATE " + SDAppList.APP_LIST_SCAN_TBL + " SET "
						+ " " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] 
								+ " = " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] + 
							" & " + ~SDAppList.CURRENT_SCAN 
						+ " WHERE " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + " != " + scan_ID 
						+ "; "
						+ "UPDATE " + SDAppList.APP_LIST_SCAN_TBL + " SET "
						+ " " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] 
								+ " = " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] + 
							" | " + SDAppList.CURRENT_SCAN 
						+ " WHERE " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID] + " = " + scan_ID 
						+ "; COMMIT;";
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "setCurrentScanList: current list ID:" + scan_ID);
		if (SdmmsConsts.DEBUG_DB) 
			Log.i(TAG, TAG2 + "setCurrentScanList: SQL : " + UPDATE_STR);
		boolean updated = false;
		try {
			updated = execSQLString(UPDATE_STR);
		} catch (Exception e) {
			if (SdmmsConsts.DEBUG_DB) 
				Log.i(TAG, TAG2 + "setCurrentScanList: Error executing ScanList insert SQL");
		}
		int scanId = -1;
		if (updated) {
			Cursor mCursor = appDB.rawQuery(
					"SELECT " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_ID]  
							+ " FROM " + SDAppList.APP_LIST_SCAN_TBL 
							+ " WHERE " + SDAppList.APP_LIST_SCAN_COLUMNS[SDAppList.scan_FLG] 
									+ " & " + SDAppList.CURRENT_SCAN 
							+ " = " + SDAppList.CURRENT_SCAN
							, null);
			if (mCursor != null) { 
				if (mCursor.getCount() > 0) {
					mCursor.moveToFirst();
					scanId = mCursor.getInt(0);
				}
				mCursor.close();
			}
		} 
		return scanId;
	}

    public boolean deletePackagePerms(int pkgId) {
    	if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePackagePerms: Deleting permissions data for: " + pkgId);
        String DELETE_STR = 
        	"DELETE FROM " 
        	+ AppUsesPermissions.APP_USES_PERMISSIONS_TBL 
        	+ " WHERE "
        	+ AppUsesPermissions.APP_USES_PERMISSIONS_COLUMNS[2] + " = '" + pkgId + "'; COMMIT;";
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePackagePerms: Deleting: pkgId:" + pkgId);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePackagePerms: SQL : " + DELETE_STR);
    	boolean deleted = false;
    	try {
    		deleted = execSQLString(DELETE_STR);
    	} catch (Exception e) {
    		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePackagePerms: Error executing pkgId Delete SQL");
    		if (SdmmsConsts.DEBUG_DB) e.printStackTrace();
    	}
    	return deleted;
    }

    public boolean deletePkgInfo(AppScanInfo tmpPkg) {
    	if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePkgInfo: Deleting data for: " + tmpPkg.pkgName);
    	deletePackagePerms(tmpPkg.pkgId);
        String DELETE_STR = 
        	"DELETE FROM " 
        	+ AppInstalledInfo.PACKAGE_STATUS_TBL 
        	+ " WHERE "
        	+ AppInstalledInfo.PACKAGE_STATUS_INFO_COLUMNS[AppInstalledInfo.pkg_ID] + " = '" + tmpPkg.pkgId + "'; COMMIT;";
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePkgInfo: SQL : " + DELETE_STR);
    	boolean deleted = false;
    	try {
    		deleted = execSQLString(DELETE_STR);
    	} catch (Exception e) {
    		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deletePkgInfo: Error executing pkgId Delete SQL");
    		if (SdmmsConsts.DEBUG_DB) e.printStackTrace();
    	}
    	return deleted;
    }

	/**
	 * 
	 * Notification functions
	 * 
	 */

    public ArrayList<SpaUserNotification> getUserNotificationList() throws SQLException {
    	//---retrieves a particular phone conversation---
    	Cursor spaCursor =
        	appDB.query(true, UserNotify.USER_NOTIFY_TBL,
	        	UserNotify.USER_NOTIFY_COLUMNS, 
                null, 
                null,
                null, 
                null, 
                null, 
                null);
    	ArrayList<SpaUserNotification> tmpNotifications = new ArrayList<SpaUserNotification>();
        if (spaCursor != null) {
        	spaCursor.moveToFirst();
        } else return tmpNotifications;
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "New SQL Cursor - Get all notifications. ");
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "New SQL Cursor #rows:" + spaCursor.getCount());
    	if (spaCursor.getCount() < 1) {
    		spaCursor.close();
    		return tmpNotifications;
    	}
    	SpaUserNotification notifyOptions = new SpaUserNotification(ASNotificationManager.BASIC_NOTIFY);
    	while (!spaCursor.isAfterLast()) {
	    	notifyOptions = new SpaUserNotification(spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[1])));
	    	if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "Cursor data: dbNotifyPhoneIDCol:"
	        	+ spaCursor.getString(spaCursor.getColumnIndex("usr_notify_type")));
	    	notifyOptions.notification_id = spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[0]));
	    	notifyOptions.timeOfDay = spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[2]));
	    	notifyOptions.dayOfWeek = spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[3]));
	    	notifyOptions.dayOfMonth = spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[4]));
	    	notifyOptions.immediateNotify = strToBoolean(spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[5])));
	    	notifyOptions.delayedNotify = strToBoolean(spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[6])));
	    	notifyOptions.delayPeriod = spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[7]));
	    	notifyOptions.hasEmailAddress = strToBoolean(spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[8])));
	    	notifyOptions.hasConfidentialEmailAddress = strToBoolean(spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[9])));
	    	notifyOptions.statusbar = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[11])));
	    	if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "Cursor data: Default Icon:"
	            	+ spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[12])));
	    	notifyOptions.statusbarDefaultIcon = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[12])));
	    	notifyOptions.statusbarDefaultNotification = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[13])));
	    	notifyOptions.statusbarTickerUseTextMsg = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[14])));
	    	notifyOptions.statusbarDefaultTicker = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[15])));
	    	notifyOptions.statusbarDefaultTitle = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[16])));
	    	notifyOptions.statusbarDefaultText = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[17])));
	    	notifyOptions.statusbarTicker = 
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[18]));
	    	notifyOptions.statusbarTitle = 
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[19]));
	    	notifyOptions.statusbarText = 
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[20]));
	    	notifyOptions.light = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[21])));
	    	notifyOptions.vibrate = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[22])));
	    	notifyOptions.vibrateDefault = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[23])));
	    	notifyOptions.soundDefault = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[24])));
	    	notifyOptions.sound = strToBoolean(
	    			spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[25])));
	    	notifyOptions.statusbarIcon = 
				spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[26]));
	        if (SdmmsConsts.DEBUG_DB) 
	        	Log.i(TAG, TAG2 + "getNotification: notifyOptions.statusbarIcon" + notifyOptions.statusbarIcon);
	    	notifyOptions.lightOn = 
				spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[27]));
	    	notifyOptions.lightOff = 
				spaCursor.getInt(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[28]));
	    	//place holder to get when encoded
	        //cv.put(UserNotify.USER_NOTIFY_COLUMNS[29], notifyData.vibratePattern);
	    	notifyOptions.soundFile = Uri.parse(
				spaCursor.getString(spaCursor.getColumnIndex(UserNotify.USER_NOTIFY_COLUMNS[30])));
	    	tmpNotifications.add(notifyOptions);
    	}
    	spaCursor.close();
        return tmpNotifications;
    }
     
    public boolean deleteNotification(String pID) {
    	if (pID == null) return false;
    	if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deleteNotification: Deleting notification entry for: " + pID);
        String DELETE_STR = 
        	"DELETE FROM " 
        	+ UserNotify.USER_NOTIFY_TBL 
        	+ " WHERE "
        	+ UserNotify.USER_NOTIFY_COLUMNS[10] + " = '" + pID + "'; COMMIT;";
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deleteNotification: Deleting: PhoneID:" + pID);
        if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deleteNotification: SQL : " + DELETE_STR);
    	boolean deleted = false;
    	try {
    		deleted = execSQLString(DELETE_STR);
    	} catch (Exception e) {
    		if (SdmmsConsts.DEBUG_DB) Log.i(TAG, TAG2 + "deleteNotification: Error executing PhoneID Delete SQL");
    		if (SdmmsConsts.DEBUG_DB) e.printStackTrace();
    	}
    	return deleted;
    }
    
	/**
	 * 
	 *   Observer classes, getters and setters
	 *   
	 */


	public interface ScanListListener {
		public abstract void onScanUpdate();
	}

	public void setScanListListener(ScanListListener scanListListener) {
		mScanListListener = scanListListener;
	}

	public void removeScanListListener() {
		mScanListListener = null;
	}

	public void setScanPkgListUpdateListener(SDAppListUpdateListener scanPkgListUpdateListener) {
		mScanPkgListUpdateListener = scanPkgListUpdateListener;
	}

	public void removeScanPkgListUpdateListener(SDAppListUpdateListener scanPkgListUpdateListener) {
		mScanPkgListUpdateListener = null;
	}




}