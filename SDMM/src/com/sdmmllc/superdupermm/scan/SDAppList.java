package com.sdmmllc.superdupermm.scan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDBConn;
import com.sdmmllc.superdupermm.scan.SDScanManager.AppListComparator;
import com.sdmmllc.superdupermm.xml.SpaXmlParser;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class SDAppList {

	private String TAG = "ScanPkgList";

	public static final String APP_LIST_SCAN_TBL = "Scan";
	
	public static final String[] APP_LIST_SCAN_COLUMNS = {
    	"scan_ID",
        "scan_start_TS", 
        "scan_end_TS", 
        "scan_FLG", 
        "scan_app_list_version_ID",
	};

	public int 
		scanID, 
		appListID, 
		appListType, 
		appListMinAppVersion, 
		scanFlgs = 0;

	public long 
		scanStartDt, 
		scanEndDt, 
		appListDate, 
		appListDownloadDate;

	public static final int
		scan_ID = 					0,
	    scan_start_TS = 			1,
	    scan_end_TS = 				2,
	    scan_FLG = 					3,
	    scan_app_list_version_ID = 	4;

	public static final String ALL_COLUMNS = 
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_ID] + ", " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_start_TS] + ", " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_end_TS] + ", " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_FLG] + ", " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_app_list_version_ID];

	public static final String ALL_DB_COLUMNS = 
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_ID] + " INTEGER primary key autoincrement, " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_start_TS] + " LONG NOT NULL, " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_end_TS] + " LONG NOT NULL, " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_FLG] + " INTEGER NOT NULL, " +
			SDAppList.APP_LIST_SCAN_COLUMNS[scan_app_list_version_ID] + " TEXT ";

	public static String 
		DISPLAY_OPTION = "DisplayOption",
		SCAN_OPTION = "ScanType";
	
	public static int 
		DISPLAY_TYPE = 1, 
		DISPLAY_CATEGORY = 2;
	
	public static int CURRENT_SCAN = 1 << 1;
	
	private SDAppListUpdateListener mSDAppListUpdateListener; 
	private int scanItemsTotal = 0, scanItemsProgress = 0;
	
	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, 
			java.text.DateFormat.SHORT);
	
	public List<AppScanInfo> scanList;

	public SDAppList () {
		scanList = new ArrayList<AppScanInfo>();
	}
	
	public SDAppList (List<AppScanInfo> tmpList) {
		scanList = tmpList;
	}
	
	public void setCurrentScan(boolean current_scan) {
		if (current_scan) scanFlgs |= CURRENT_SCAN;
		else scanFlgs &= ~CURRENT_SCAN;
	}
	
	public boolean isCurrentScan() {
		return ((scanFlgs & CURRENT_SCAN) == CURRENT_SCAN);
	}
	
	public void setAppListUpdateListener(SDAppListUpdateListener appListUpdateListener) {
		setSDAppListUpdateListener(appListUpdateListener);
	}
	
	public void removeAppListUpdateListener(SDAppListUpdateListener appListUpdateListener) {
		setSDAppListUpdateListener(null);
	}
	
	public int size() {
		return scanList.size();
	}
	
	public void setUserSafe(Context context, String pkgName) {
		int position = getPkgIndex(pkgName);
		scanList.get(position).setUserSafe(true);
		scanList.get(position).userDt = Calendar.getInstance().getTimeInMillis();
    	if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "setUserSafe: updating package: " + pkgName);
		SDDB tmpDB = SDSmsManagerApp.getSDDB();
		SDDBConn conn = tmpDB.open(true);
		tmpDB.updateScanItemUserInfo(scanList.get(position));
		tmpDB.updatePackageUserInfo(scanList.get(position));
		tmpDB.close(conn);
		AppListUpdate update = new AppListUpdate(context);
        update.sendPackageUpdate(scanList.get(position), scanStartDt, scanEndDt, scanID, Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID), scanList.size(), this.getSysCount(), this.getUserSafeCount());
	}
	
	public void setUserWatchlist(Context context, String pkgName) {
		int position = getPkgIndex(pkgName);
		scanList.get(position).setUserWatchlist(true);
		scanList.get(position).userDt = Calendar.getInstance().getTimeInMillis();
    	if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "setUserWatchlist: updating package: " + pkgName);
		SDDB tmpDB = SDSmsManagerApp.getSDDB();
		SDDBConn conn = tmpDB.open(true);
		tmpDB.updateScanItemUserInfo(scanList.get(position));
		tmpDB.updatePackageUserInfo(scanList.get(position));
		tmpDB.close(conn);
		AppListUpdate update = new AppListUpdate(context);
        update.sendPackageUpdate(scanList.get(position), scanStartDt, scanEndDt, scanID, Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID), scanList.size(), this.getSysCount(), this.getUserWatchlistCount());
	}
	
	public void setUserUnsafe(Context context, String pkgName) {
		int position = getPkgIndex(pkgName);
		scanList.get(position).setUserUnsafe(true);
		scanList.get(position).userDt = Calendar.getInstance().getTimeInMillis();
    	if (SdmmsConsts.DEBUG_SCAN) 
     		Log.i(TAG, "setUserUnsafe: updating package: " + pkgName);
		SDDB tmpDB = SDSmsManagerApp.getSDDB();
		SDDBConn conn = tmpDB.open(true);
		tmpDB.updateScanItemUserInfo(scanList.get(position));
		tmpDB.updatePackageUserInfo(scanList.get(position));
		tmpDB.close(conn);
		AppListUpdate update = new AppListUpdate(context);
		boolean success = SpaXmlParser.parseXmlPackageUpdateResponse(context, 
				update.sendPackageUpdate(scanList.get(position), scanStartDt, 
						scanEndDt, scanID, Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID), scanList.size(), this.getSysCount(), this.getUserUnsafeCount()));
    	if (SdmmsConsts.DEBUG_SCAN) 
     		Log.i(TAG, "setUserUnsafe: update status: " + success);
	}
	
	public void addPkg(AppScanInfo item) {
		scanList.add(item);
	}
	
	public void set(int idx, AppScanInfo item) {
		scanList.set(idx, item);
	}
	
	public AppScanInfo get(int idx) {
		return scanList.get(idx);
	}
	
	public int getPkgIndex(String pkgName) {
		for (int i = 0; i < scanList.size(); i++) {
			if (scanList.get(i).pkgName.equals(pkgName)) return i;
		}
		return -1;
	}
	
	public int getSysCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (item.isSysApp()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getSysCount: " + count);
		return count;
	}

	public int getRegisteredCount() {
		int count = 0;
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getRegisteredCount: scanList.size():" + scanList.size());
		for (AppScanInfo item:scanList) {
			if (item.isTypeRegistered()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "old_getSafeCount / getRegisteredCount: " + count);
		return count;
	}

	public int getUserSafeCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (item.isUserSafe()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getUserSafeCount: " + count);
		return count;
	}

	public List<AppScanInfo> getRegisteredList() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if (item.isTypeRegistered()) tmpList.add(item);
		}
        Collections.sort(tmpList, AppListComparator.getComparator(AppListComparator.INSTALL_DT));
        Collections.reverse(tmpList);
		return tmpList;
	}
	
	public int getUserUnsafeCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (item.isUserUnsafe()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getUserUnsafeCount: " + count);
		return count;
	}

	public int getUserWatchlistCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (item.isUserWatchlist()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getUserWatchlistCount: " + count);
		return count;
	}

	public List<AppScanInfo> old_getWatchlist() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if (item.old_onListWatchlist()) tmpList.add(item);
		}
		return tmpList;
	}
	
	public int getSMSOtherCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (!item.hasFilterSMSDelivered() && item.hasFilterSMSReceived() 
					&& !item.isTypeSecure()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getSMSOtherCount: " + count);
		return count;
	}

	public List<AppScanInfo> getSMSOtherList() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if (!item.hasFilterSMSDelivered() && item.hasFilterSMSReceived() 
					&& !item.isTypeSecure()) tmpList.add(item);
		}
        Collections.sort(tmpList, AppListComparator.getComparator(AppListComparator.INSTALL_DT));
        Collections.reverse(tmpList);
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getSMSOtherList.size(): " + tmpList.size());
		return tmpList;
	}
	
	public int getMessagingCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (item.isTypeMessaging() && !item.isTypeSecure()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getMessagingCount: " + count);
		return count;
	}

	public List<AppScanInfo> getMessaging() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if (item.isTypeMessaging() && !item.isTypeSecure()) tmpList.add(item);
		}
        Collections.sort(tmpList, AppListComparator.getComparator(AppListComparator.INSTALL_DT));
        Collections.reverse(tmpList);
        if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getMessaging.size(): " + tmpList.size());
    	
		return tmpList;
	}
	
	public int getMessagingCompatibleCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if ((item.isTypeMsgPremium() || 
					(item.isTypeMessaging() && item.isTypeRegistered()))
					 && !item.isTypeSecure()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getMessagingCompatibleCount: " + count);
		return count;
	}

	public List<AppScanInfo> getMessagingCompatibleList() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if ((item.isTypeMsgPremium() || 
					(item.isTypeMessaging() & item.isTypeRegistered()))
					&& !item.isTypeSecure()) tmpList.add(item);
		}
        Collections.sort(tmpList, AppListComparator.getComparator(AppListComparator.INSTALL_DT));
        Collections.reverse(tmpList);
        if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getMessagingCompatibleList.size(): " + tmpList.size());
		return tmpList;
	}
	
	public int getMessagingNonCompatibleCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if ((item.isTypeMessaging() && !item.isTypeRegistered())
					 && !item.isTypeSecure()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getMessagingNonCompatibleCount: " + count);
		return count;
	}

	public List<AppScanInfo> getMessagingNonCompatibleList() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if ((item.isTypeMessaging() && !item.isTypeRegistered())
					 && !item.isTypeSecure()) tmpList.add(item);
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getMessagingNonCompatibleList.size(): " + tmpList.size());
		return tmpList;
	}
	
	public int getNonSMSCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if ((!item.hasFilterSMSDelivered() && !item.hasFilterSMSReceived()) ||
					item.isTypeSecure()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getNonSMSCount: " + count);
		return count;
	}

	public List<AppScanInfo> getNonSMSList() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if ((!item.hasFilterSMSDelivered() && !item.hasFilterSMSReceived()) ||
					item.isTypeSecure()) tmpList.add(item);
		}
        Collections.sort(tmpList, AppListComparator.getComparator(AppListComparator.INSTALL_DT));
        Collections.reverse(tmpList);
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getNonSMSList.size(): " + tmpList.size());
		return tmpList;
	}
	
	public int getSMSCount() {
		int count = 0;
		for (AppScanInfo item:scanList) {
			if (item.hasFilterSMSDelivered() && !item.isTypeSecure()) count++;
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getSMSCount: " + count);
		return count;
	}

	public List<AppScanInfo> getSMSList() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>();
		for (AppScanInfo item:scanList) {
			if (item.hasFilterSMSDelivered() && !item.isTypeSecure()) tmpList.add(item);
		}
        Collections.sort(tmpList, AppListComparator.getComparator(AppListComparator.INSTALL_DT));
        Collections.reverse(tmpList);
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getSMSList.size(): " + tmpList.size());
		return tmpList;
	}
	
	public AppScanInfo getPreferredSMSProvider() {
		if (getSMSList() == null) return new AppScanInfo();
	    SharedPreferences prefs = SDSmsManagerApp.getContext().getSharedPreferences(SDScanManager.SCAN_PREFS, 0);
		String pkgName = prefs.getString(SDScanManager.PRIMARY_MESSAGING_APP, "");
		if (pkgName.length() > 0) {
			return getPackage(pkgName);
		}
		List <AppScanInfo> apps = getMessagingCompatibleList();
		if (apps.size() < 1) {
			return get(getPkgIndex(SDSmsConsts.SDSMS_PACKAGE_NAME));
		}
		return apps.get(0);
	}
	
	public List<AppScanInfo> getList(int display_option, int list_type) {
		
		if (display_option == SDAppList.DISPLAY_CATEGORY) {
			if (list_type == AppScanInfo.SAFE) return getRegisteredList();
			if (list_type == AppScanInfo.WATCH_LIST) return old_getWatchlist();
		} else {
			if (list_type == AppScanInfo.SMS_APP) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "list_type == AppScanInfo.SMS_APP");
				return getSMSList();
			}
			if (list_type == AppScanInfo.SMS_OTHER) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "list_type == AppScanInfo.SMS_OTHER");
		    	return getSMSOtherList();
			}
			if (list_type == AppScanInfo.SMS_NONE) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "list_type == AppScanInfo.SMS_NONE");
				return getNonSMSList();
			}
			if (list_type == AppScanInfo.TYPE_REGISTERED_APP) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "list_type == AppScanInfo.TYPE_REGISTERED_APP");
				return getRegisteredList();
			}
			if (list_type == AppScanInfo.TYPE_MESSAGING) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "list_type == AppScanInfo.TYPE_MESSAGING");
		    	return getMessaging();
			}
			if (((list_type & AppScanInfo.TYPE_MESSAGING) == AppScanInfo.TYPE_MESSAGING) &&
					((list_type & AppScanInfo.TYPE_REGISTERED_APP) == AppScanInfo.TYPE_REGISTERED_APP)) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "(((list_type & AppScanInfo.TYPE_MESSAGING) & " +
					"(list_type & AppScanInfo.TYPE_REGISTERED_APP)) > 0)");
				return getMessagingCompatibleList();
			}
			if (((list_type & AppScanInfo.TYPE_MESSAGING) == AppScanInfo.TYPE_MESSAGING) && 
					((list_type & AppScanInfo.TYPE_UNREGISTERED) == AppScanInfo.TYPE_UNREGISTERED)) {
		    	if (SdmmsConsts.DEBUG_APP_LIST) Log.i(TAG, "(((list_type & AppScanInfo.TYPE_MESSAGING) & " +
					" (AppScanInfo.TYPE_UNREGISTERED)) > 0)");
				return getMessagingNonCompatibleList();
			}
		}
		
		return scanList;
	}
	
	public List<AppScanInfo> getNonSpaApps() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>(); 
		for (AppScanInfo tmpItem:scanList) {
			if (!tmpItem.pkgName.startsWith("com.secondphoneapps")) {
				tmpList.add(tmpItem);
			}
		}
    	if (SdmmsConsts.DEBUG_APP_LIST) 
			Log.i(TAG, "getNonSpaApps.size(): " + tmpList.size());
		return tmpList;
	}
	
	public List<AppScanInfo> getSpaApps() {
		List<AppScanInfo> tmpList = new ArrayList<AppScanInfo>(); 
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.pkgName.startsWith("com.secondphoneapps")) {
				tmpList.add(tmpItem);
			}
		}
		return tmpList;
	}
	
	public boolean hasThisSpaApp(Context ctx) {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.pkgName.equals(ctx.getPackageName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasFreeVersionSpaApp(Context ctx) {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.appName.equals(ctx.getString(R.string.app_name)+"Lic")) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasPaidVersionSpaApp(Context ctx) {
		for (AppScanInfo tmpItem:scanList) {
			if ((tmpItem.appName+"Lic").equals(ctx.getString(R.string.app_name))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isThisSpaApp(Context ctx) {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.appName.equals(ctx.getString(R.string.app_name))) {
				return true;
			}
		}
		return false;
	}
	
	public int packageIndex(String pkgName) {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.pkgName.equals(pkgName)) {
				return scanList.indexOf(tmpItem);
			}
		}
		return -1;
	}
	
	public AppScanInfo getPackage(String pkgName) {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.pkgName.equals(pkgName)) {
				return tmpItem;
			}
		}
		return new AppScanInfo();
	}
	
	public boolean hasPackage(String pkgName) {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.pkgName.equals(pkgName)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean has999App() {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.recPriority == 999) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasOver999App() {
		for (AppScanInfo tmpItem:scanList) {
			if (tmpItem.recPriority > 999) {
				return true;
			}
		}
		return false;
	}

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append("ScanPkgList");
        sb.append("{df=").append(df);
        sb.append(", TAG='").append(TAG).append('\'');
        sb.append(", scanID=").append(scanID);
        sb.append(", riskListID=").append(appListID);
        sb.append(", riskListType=").append(appListType);
        sb.append(", riskListMinAppVersion=").append(appListMinAppVersion);
        sb.append(", scanFlgs=").append(scanFlgs);
        sb.append(", scanStartDt=").append(scanStartDt);
        sb.append(", scanEndDt=").append(scanEndDt);
        sb.append(", riskListDate=").append(appListDate);
        sb.append(", riskListDownloadDate=").append(appListDownloadDate);
        sb.append(", scanList=").append(scanList);
        sb.append('}');
        return sb.toString();
    }

	public SDAppListUpdateListener getSDAppListUpdateListener() {
		return mSDAppListUpdateListener;
	}

	public void setSDAppListUpdateListener(SDAppListUpdateListener mSDAppListUpdateListener) {
		this.mSDAppListUpdateListener = mSDAppListUpdateListener;
	}

	public int getScanItemsTotal() {
		return scanItemsTotal;
	}

	public void setScanItemsTotal(int scanItemsTotal) {
		this.scanItemsTotal = scanItemsTotal;
	}

	public int getScanItemsProgress() {
		return scanItemsProgress;
	}

	public void setScanItemsProgress(int scanItemsProgress) {
		this.scanItemsProgress = scanItemsProgress;
	}
}
