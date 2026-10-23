package com.sdmmllc.superdupermm.scan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.location.GpsStatus.Listener;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDBConn;
import com.sdmmllc.superdupermm.data.SDIntentFilters;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class SDScanEngine {
	
	public static String TAG = "ASScanEngine";
	
	private List<SDScanListener> listeners = new ArrayList<SDScanListener>();
	
	public void addListener(SDScanListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(SDScanListener listener) {
		listeners.remove(listener);
	}

	public List<AppScanInfo> getInstalledApps(SDAppList appList, PackageManager pm, SDDB tmpDB, boolean getSysPackages) {
		ArrayList<AppScanInfo> scanList = new ArrayList<AppScanInfo>();
        appList.scanStartDt = System.currentTimeMillis();
	    List<PackageInfo> packs = pm.getInstalledPackages(
	    	PackageManager.GET_PERMISSIONS|
	    	PackageManager.GET_RECEIVERS);
        int totalPacks = packs.size();
        int increment = (totalPacks/10);
        int i = 0;
        for (SDScanListener mListener : listeners) {
        	mListener.updateScanProgress(totalPacks, 0);
        }
	    for (PackageInfo p:packs) {
	        if (getSysPackages | (
		        		!(
			        		((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) 
			        				== ApplicationInfo.FLAG_SYSTEM)|
			        		((p.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) 
			        			 == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
	        			)
	        		)) {
		        if (appList.packageIndex(p.packageName) < 0) {
		        	AppScanInfo newInfo = new AppScanInfo();
		        	newInfo.pkgName = p.packageName;
		        	newInfo.setInstalledApp(true);
			        scanList.add(getAppInfo(newInfo, tmpDB, pm, p));
		         }
	         }
	        i++;
	        if (((i % increment) == 0) || (i >= totalPacks)) {
	            for (SDScanListener mListener : listeners) {
	            	mListener.updateScanProgress(totalPacks, i);
	            }
	        }
	    }
		appList.scanList = scanList;
		appList.setScanItemsTotal(appList.scanList.size());
        appList.scanEndDt = System.currentTimeMillis();
		SDDBConn conn = tmpDB.open(true);
		tmpDB.updatePackageScanInfo(appList);
		tmpDB.close(conn);
	    return scanList;
	}
	
	public List<AppScanInfo> addInstalledApp(SDAppList scanList, PackageManager pm, SDDB tmpDB, String appName) {
		PackageInfo p;
		try {
			p = pm.getPackageInfo(appName, 
					PackageManager.GET_PERMISSIONS|
					PackageManager.GET_RECEIVERS);
	        if (scanList.packageIndex(p.packageName) < 0) {
	        	AppScanInfo newInfo = new AppScanInfo();
	        	newInfo.pkgName = p.packageName;
	        	newInfo.setInstalledApp(true);
		        scanList.addPkg(getAppInfo(newInfo, tmpDB, pm, p));
		    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
			    	Log.i(TAG, "addInstalledApp: app found: " + newInfo.appName);
	         }
		} catch (NameNotFoundException e1) {
			e1.printStackTrace();
	    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
				Log.i(TAG, "addInstalledApp: app not found: " + appName);
			return scanList.scanList;
		}
		return scanList.scanList;
	}

	public AppScanInfo getAppInfo(AppScanInfo pkgInfo, SDDB tmpDB,
			PackageManager pm, PackageInfo p) {
        pkgInfo.setInstalledApp(true);
		SDDBConn conn = tmpDB.open(true);
        if (tmpDB.hasPackage(pkgInfo.pkgName)) {
         	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
        		Log.i(TAG, "getAppInfo - existing app: " + p.packageName);
        	boolean changed = false;
        	pkgInfo = tmpDB.getInstalledPackage(pkgInfo.pkgName);
        	if (p.versionCode != pkgInfo.versionCode) {
        		changed = true;
        		pkgInfo.versionCode = p.versionCode;
        	}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				if (pkgInfo.installDt != p.firstInstallTime) {
					changed = true;
					pkgInfo.installDt = p.firstInstallTime;
				}
			    if (pkgInfo.updateDt != p.lastUpdateTime) {
			    	changed = true;
			    	pkgInfo.updateDt = p.lastUpdateTime;
			    }
			} else {
				changed = true;
				pkgInfo.updateDt = Calendar.getInstance().getTimeInMillis();
			}
	        SDAppList tmpList = new SDAppList();
	        tmpList.addPkg(pkgInfo);
	        pkgInfo = scanListForSMS(pm, tmpList).get(0);
        	Enumeration<Integer> permKeys = pkgInfo.usesPerms.keys();
        	while (permKeys != null && permKeys.hasMoreElements()) {
        		tmpDB.deletePackagePerms(pkgInfo.pkgId);
        		Integer key = (Integer) permKeys.nextElement();
        		tmpDB.insertPermInfo(pkgInfo.pkgId, key, pkgInfo.usesPerms.getFilter(key));
        	}
			if (changed) tmpDB.updatePackageDt(pkgInfo);
			tmpDB.updatePackageFeatures(pkgInfo);
        } else {
         	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
    			Log.i(TAG, "getAppInfo - new app: " + p.packageName);
	        pkgInfo.pkgName = p.packageName;
	        try {
	        	pkgInfo.appName = p.applicationInfo.loadLabel(pm).toString();
	        } catch (NotFoundException e) {
	         	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
	        		Log.i(TAG, "Error getting package permissions for:" + p.packageName);
	         	e.printStackTrace();
	         	pkgInfo.appName = p.packageName;
	        }
	        pkgInfo.versionName = p.versionName;
	        pkgInfo.versionCode = p.versionCode;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				pkgInfo.installDt = p.firstInstallTime;
			    pkgInfo.updateDt = p.lastUpdateTime;
			}
			pkgInfo.icon = p.applicationInfo.loadIcon(pm);
	        pkgInfo.sharedUserId = p.sharedUserId;
	        pkgInfo.sharedUserLbl = p.sharedUserLabel+"";
	        try {
	        	pkgInfo.pkgPerms = pm.getPackageInfo(
	        		p.packageName, 
	        		PackageManager.GET_PERMISSIONS
	        	);
	        } catch (NameNotFoundException e) {
	         	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
	        		Log.i(TAG, "Error getting package permissions for:" + p.packageName);
	         	e.printStackTrace();
	        }
	        pkgInfo.setSafe(true);
         	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
	        	Log.i(TAG, "getAppInfo - before insert assume safe, package flags: " + pkgInfo.pkgFlgs);
	        if (((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)|
	        	 ((p.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) 
	        			 == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) pkgInfo.setSysApp(true);
	        SDAppList tmpList = new SDAppList();
	        tmpList.addPkg(pkgInfo);
	        pkgInfo = scanListForSMS(pm, tmpList).get(0);
        	pkgInfo.pkgId = tmpDB.insertPackageInfo(pkgInfo);
        	Enumeration<Integer> permKeys = pkgInfo.usesPerms.keys();
        	while (permKeys != null && permKeys.hasMoreElements()) {
        		Integer key = (Integer) permKeys.nextElement();
        		tmpDB.insertPermInfo(pkgInfo.pkgId, key, pkgInfo.usesPerms.getFilter(key));
        	}
         	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
        		Log.i(TAG, "getAppInfo - after insert, package flags: " + pkgInfo.pkgFlgs);
        }
        tmpDB.close(conn);
        return pkgInfo;
	}
	
	public List<AppScanInfo> scanListForPermissions(List<AppScanInfo> scanList) {
		for (AppScanInfo tmpInfo : scanList) {
	    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
	    		Log.i(TAG, "Getting permissions for " + tmpInfo.pkgName);
    	    PermissionInfo[] permissions = tmpInfo.pkgPerms.permissions;
    	    if (permissions == null) {
    	    	tmpInfo.permFound = "No declared permissions";
    	    }
    	    else {
    	    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
    	    		Log.i(TAG, "Permissions found:" + permissions.length);
	    		String permissionsText = "";
	        	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
	        		Log.i(TAG, "Getting permissions for " + tmpInfo.pkgName);
	    		for (int j = 0; j < permissions.length; j++) {
	    		    permissionsText += permissions[j].name + "\n";
	    		}
	    		tmpInfo.permFound = permissionsText;
    	    }

    	    String[] requestedPermissions = tmpInfo.pkgPerms.requestedPermissions;
    	    if (requestedPermissions == null) {
    	    	tmpInfo.reqPermFound = "No requested permissions";
    	    }
    	    else {
	    		String reqPermText = "";
	    		for (int j = 0; j < requestedPermissions.length; j++) {
	    		    reqPermText += requestedPermissions[j] + "\n";
	    		}
	    		tmpInfo.reqPermFound = reqPermText;
    	    }
		}
		return scanList;
	}
	
	private final String 
		RECEIVERS = "BroadcastReceiver",
		ACTIVITIES = "Activity",
		SERVICES = "Service";

	public List<AppScanInfo> scanListForSMS(PackageManager pm, SDAppList scanList) {
    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
    		Log.i(TAG, "Getting intent receivers");
    	List<ResolveInfo> resolves;
    	for (String intentFilter: SDIntentFilters.Filters.keySet()) {
    		Intent intent = new Intent(intentFilter);
    		intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
    			|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		    resolves = pm.queryBroadcastReceivers(intent, PackageManager.GET_RESOLVED_FILTER);
		    checkFilter(pm, scanList, resolves, intentFilter, intent, RECEIVERS);
		    
		    intent = new Intent(intentFilter);
    		intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
    			|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
    		resolves = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
		    checkFilter(pm, scanList, resolves, intentFilter, intent, ACTIVITIES);
		    
		    intent = new Intent(intentFilter);
    		intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
    			|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
    		resolves = pm.queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER);
		    checkFilter(pm, scanList, resolves, intentFilter, intent, SERVICES);
    	}	    
    	scanList.scanList = checkForRegisteredApps(scanList);
		return scanList.scanList;
	}
	
	private List<AppScanInfo>  checkForRegisteredApps(SDAppList scanList) {
    	for (AppScanInfo app : scanList.scanList) {
    		if (app.pkgName.startsWith("com.secondphoneapps.SecondPhoneText")) {
    			app.setTypeSecure(true);
    			app.setTypeRegistered(true);
    			Log.i(TAG, "found SPA App:" + app.pkgName);
    		} else if (app.pkgName.startsWith(SDSmsConsts.SDSMS_INSTALL_PKG)) {
    			app.setTypeSysReserved(true);
    			app.setTypeMessaging(false);
    			Log.i(TAG, "found Manager App:" + app.pkgName);
    		} else if (app.pkgName.equals("me.gotxt.app")) {
    			app.setTypeSMSPremium(true);
    			app.setTypeMessaging(true);
    			app.setTypeRegistered(true);
    			Log.i(TAG, "found Premium App:" + app.pkgName);
    		} else if (app.pkgName.equals("com.gamedaysms")) {
    			app.setTypeSMSPremium(true);
    			app.setTypeMessaging(true);
    			app.setTypeRegistered(true);
    			Log.i(TAG, "found Premium App:" + app.pkgName);
    		} else {
    			app.setTypeSecure(false);
    			app.setTypeSysReserved(false);
    			app.setTypeSMSPremium(false);
    			app.setTypeRegistered(false);
    		}
    	}
    	return scanList.scanList;
	}
	
	private void checkFilter(PackageManager pm, SDAppList appList, List<ResolveInfo> resolves, 
			String filter, Intent intent, String type) {
		resolvesToSms(pm, appList, resolves, filter, intent, false);
	}
	
	private void resolvesToSms(PackageManager pm, SDAppList scanList, 
			List<ResolveInfo> resolves, String filter, Intent intent, boolean getSysPackages) {
	    for (ResolveInfo resolve : resolves) {
		    if (resolve != null) {
		    	if (scanList.scanList.size() < 1) {
					try {
				        PackageInfo p = pm.getPackageInfo(resolve.activityInfo.applicationInfo.packageName, 
								PackageManager.GET_PERMISSIONS|
								PackageManager.GET_RECEIVERS|
								PackageManager.GET_ACTIVITIES);
				         if (getSysPackages || (
					        		!(
						        		((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) 
						        				== ApplicationInfo.FLAG_SYSTEM) ||
						        		((p.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) 
						        				== ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
					        		)
				        		)) {
					        if (scanList.packageIndex(p.packageName) < 0) {
						        AppScanInfo newInfo = new AppScanInfo();
						        newInfo.appName = p.applicationInfo.loadLabel(pm).toString();
						        newInfo.pkgName = p.packageName;
						        newInfo.versionName = p.versionName;
						        newInfo.versionCode = p.versionCode;
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
									newInfo.installDt = p.firstInstallTime;
								    newInfo.updateDt = p.lastUpdateTime;
								}
						        newInfo.icon = p.applicationInfo.loadIcon(pm);
						        newInfo.recPriority = resolve.priority;
						        newInfo.pkgPerms = p;
						        newInfo.usesPerms.setFilter(SDIntentFilters.Filters.get(filter), 0, resolve.priority);
						    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
						    		Log.i(TAG, "Found intent receiver for:" + resolve.activityInfo.applicationInfo.packageName
			    		    			+ " with priority: " + newInfo.recPriority);
						    	setFilterData(resolve, intent, filter, newInfo);
								scanList.addPkg(newInfo);
					        } else if (scanList.getPackage(p.packageName).usesPerms.contains(SDIntentFilters.Filters.get(filter))){
					        	scanList.getPackage(p.packageName).usesPerms.setFilterPriority(SDIntentFilters.Filters.get(filter), resolve.priority);
						    	setFilterData(resolve, intent, filter, scanList.getPackage(p.packageName));
					        	//scanList.getPackage(p.packageName).recPriority = resolve.priority;
					        } else {
					        	int idx = scanList.packageIndex(p.packageName);
						        AppScanInfo updateInfo = scanList.get(idx);
						        updateInfo.usesPerms.setFilter(SDIntentFilters.Filters.get(filter), 0, resolve.priority);
						    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
						    		Log.i(TAG, "Found intent receiver for:" + resolve.activityInfo.applicationInfo.packageName
			    		    			+ " with priority: " + updateInfo.recPriority);
						    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
						    		Log.i(TAG, "Found intent receiver for:" + resolve.activityInfo.applicationInfo.packageName
			    		    			+ " with priority: " + updateInfo.recPriority);
						    	setFilterData(resolve, intent, filter, updateInfo);
					        	scanList.set(idx, updateInfo);
					        }
				         }
					} catch (NameNotFoundException e) {
						e.printStackTrace();
				    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
				    		Log.e(TAG, "scanForSMS: Error getting packageInfo");
					}
		    	} else {
		        	if (SdmmsConsts.DEBUG_SCAN_ENGINE) { 
		        		Log.i(TAG, "Found intent filter: " + filter);
		        		Log.i(TAG, "for:" + resolve.activityInfo.applicationInfo.packageName
    		    			+ " with priority: " + resolve.priority);
		        	}
    				for (AppScanInfo tmpInfo : scanList.scanList) {
    					if (tmpInfo.pkgName.equals(resolve.activityInfo.applicationInfo.packageName)) {
    				    	if (SdmmsConsts.DEBUG_SCAN_ENGINE) 
    				    		Log.i(TAG, "Package updated:" + resolve.activityInfo.applicationInfo.packageName);
    				    	tmpInfo.usesPerms.setFilter(SDIntentFilters.Filters.get(filter), 0, resolve.priority);
					    	setFilterData(resolve, intent, filter, tmpInfo);
    					}
    				}
		    	}
		    }
	    }
	}
	
	private void setFilterData(ResolveInfo resolve, Intent intent, String filter, AppScanInfo app) {
		Uri data = intent.getData();
    	if (filter.equals(SDIntentFilters.SMS_RECEIVED) || 
    			filter.equals(SDIntentFilters.SMS_DELIVERED)) {
	    	if (filter.equals(SDIntentFilters.SMS_RECEIVED)) {
	    		app.setFilterSMSReceived(true); 
	    		app.setTypeSMSReceived(true);
	    		app.smsReceivedReceiver = resolve.activityInfo.name;
	    		if (SdmmsConsts.DEBUG_SCAN_FILTERS) Log.i(TAG, "setFilterData SMS_RECEIVED activity:" + app.smsReceivedReceiver);
		        app.recPriority = resolve.priority;
	    	}
	    	if (filter.equals(SDIntentFilters.SMS_DELIVERED)) {
	    		app.setKitKatSMS(true);
	    		app.setFilterSMSDelivered(true);
	    		app.setTypeMessaging(true);
	    		app.smsDeliveredReceiver = resolve.activityInfo.name;
	    		if (SdmmsConsts.DEBUG_SCAN_FILTERS) Log.i(TAG, "setFilterData SMS_DELIVERED activity:" + app.smsDeliveredReceiver);
	    	}
	    	app.usesPerms.setFilterPriority(SDIntentFilters.Filters.get(filter), resolve.priority);
	    	app.setCategorySMS(true);
	    	app.setHighRisk(true);
    	} else
    	if (filter.equals(SDIntentFilters.WAP_PUSH_DELIVER)) {
    		app.wapPushDeliverReceiver = resolve.activityInfo.name;
    		if (SdmmsConsts.DEBUG_SCAN_FILTERS) Log.i(TAG, "setFilterData WAP_PUSH_DELIVER activity:" + app.wapPushDeliverReceiver);
    	} else
    	if (filter.equals(SDIntentFilters.ACTION_RESPOND_VIA_MESSAGE)) {
    		app.setRespondViaMessage(true);
    		for (String cat: intent.getCategories()) {
    			if (cat.equals(SDIntentFilters.DEFAULT_CATEGORY)) app.setRespondViaMessageDefault(true);
    		}
    		app.smsRespondViaMessage = resolve.activityInfo.name;
    		if (SdmmsConsts.DEBUG_SCAN_FILTERS) Log.i(TAG, "setFilterData ACTION_RESPOND_VIA_MESSAGE activity:" + app.smsRespondViaMessage);
    	} else
    	if (filter.equals(SDIntentFilters.ACTION_SEND)) {
    		app.setSmsSend(true);
    		for (String cat: intent.getCategories()) {
    			if (cat.equals(SDIntentFilters.DEFAULT_CATEGORY)) app.setSmsSendDefault(true);
    		}
    		app.smsSend = resolve.activityInfo.name;
    		if (SdmmsConsts.DEBUG_SCAN_FILTERS) Log.i(TAG, "setFilterData ACTION_SEND activity:" + app.smsSend);
    	} else
    	if (filter.equals(SDIntentFilters.ACTION_SENDTO)) {
    		app.setSmsSendto(true);
    		for (String cat: intent.getCategories()) {
    			if (cat.equals(SDIntentFilters.DEFAULT_CATEGORY)) app.setSmsSendtoDefault(true);
    		}
    		app.smsSendTo = resolve.activityInfo.name;
    		if (SdmmsConsts.DEBUG_SCAN_FILTERS) Log.i(TAG, "setFilterData ACTION_SENDTO activity:" + app.smsSendTo);
    	} 
	}
}
