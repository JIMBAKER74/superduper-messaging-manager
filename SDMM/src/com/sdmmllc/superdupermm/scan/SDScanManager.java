package com.sdmmllc.superdupermm.scan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDBConn;
import com.sdmmllc.superdupermm.notifications.ASNotificationManager;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class SDScanManager {
	
	private String TAG = "SDScanManager";
	public static String 
		BG_SCAN = "1", 
		MANUAL_SCAN = "2", 
		INSTALL_SCAN = "3";
	
	public static String 
		SCAN_PREFS = "ScanPrefs", 
		SCAN_HOUR = "scanHour", 
		SCAN_MIN = "scanMin",
		SCAN_DAY_OF_WEEK = "scanDOW", 
		SCAN_DAY_OF_MONTH = "scanDOM",
		INITIAL_SCAN_COMPLETE = "initScanDone";
	
	public static String
		ACTION_PACKAGE_FULL_SCAN = "full_scan",
		ACTION_PACKAGE_REMOVED = "package_removed",
		ACTION_PACKAGE_ADDED = "package_added",
		ACTION_PACKAGE_UPDATED = "package_updated";
	
	public static final String PRIMARY_MESSAGING_APP = "primaryMessagingApp";
		
    private Context spaContext; 
    private SDDB sddb;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Boolean scanInProgress = false, newScanAvailable = false, scanComplete = false,
    		scanUninstallInProgress = false;
    private String currentScanPkg = "";

    private SDScanEngine scanEngine;
	private List<SDScanListener> mScanListener = new ArrayList<SDScanListener>();
	private SDAppListUpdateListener mAppListUpdateListener;

	
	public SDScanManager (Context context) {
		spaContext = context;
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
		sddb = SDSmsManagerApp.getSDDB();
	    prefs = context.getSharedPreferences(SCAN_PREFS, 0);
        SDDBConn conn = sddb.open(false);
    	sddb.close(conn);
    	//if (!prefs.getBoolean(INITIAL_SCAN_COMPLETE, false)) 
    	//   scanNow();
    	scanEngine = new SDScanEngine();
	}
	
	public void addScanListener(SDScanListener scanListener) {
		mScanListener.add(scanListener);
	}
	
	public void removeScanListener(SDScanListener scanListener) {
		mScanListener.remove(scanListener);
	}
	
	public void setSDAppListUpdateListener(SDAppListUpdateListener appListUpdateListener) {
		mAppListUpdateListener = appListUpdateListener;
	}
	
	public void removeSDAppListUpdateListener(SDAppListUpdateListener appListUpdateListener) {
		mAppListUpdateListener = null;
	}
	
	public static SDScanManager getScanManager(Context context) {
		return SDSmsManagerApp.getScanManager();
	}
	
	public AppScanInfo getPreferredMessagingApp() {
		String pkgName = prefs.getString(PRIMARY_MESSAGING_APP, "");
		if (pkgName.length() > 0) {
			return getCurrentScan().getPackage(pkgName);
		}
		List<AppScanInfo> app = getCurrentScan().getMessagingCompatibleList();
		if (app.size() < 1) {
			return getCurrentScan().getPackage(SDSmsConsts.SDSMS_PACKAGE_NAME);
		}
		return app.get(0);
	}

	public void setPreferredMessagingApp(String pkgName) {
		editor = prefs.edit();
		editor.putString(PRIMARY_MESSAGING_APP, pkgName);
		editor.commit();
	}
	
	public SDAppList getCurrentScan() {
    	SDAppList tmpList = new SDAppList();    	
        SDDBConn conn = sddb.open(true);
        tmpList = sddb.getCurrentScanList();
        sddb.close(conn);
        SDSmsManagerApp.setCurrentScan(tmpList);
		return tmpList;
	}
	
	public SDAppList getInstalledPackages() {
    	SDAppList tmpList = new SDAppList();    	
        SDDBConn conn = sddb.open(true);
        tmpList.scanList = sddb.getInstalledPackages();
        sddb.close(conn);
		return tmpList;
	}
	
    public void scanNow() {
    	scanComplete = false;
        synchronized (scanInProgress) {
        	scanInProgress = true;
        }
    	//ASNotificationManager.sendScanNotifications(spaContext, ASNotificationManager.BG_SCAN_PROGRESS_NOTIFY);
        Thread thread = new Thread(null, scanApps, "ScanningApps");
        thread.start();
	}
    
    Runnable scanApps = new Runnable() {
        @Override
        public void run() {
        	SDAppList scanList = new SDAppList();
        	scanList.setAppListUpdateListener(mAppListUpdateListener);
        	if (SdmmsConsts.DEBUG_SCAN) 
				Log.i(TAG, "scanApps: starting, list size: " + scanList.size());
            try {
                scanList = scanForRegisteredApps(spaContext);
                scanList.setCurrentScan(true);
            } catch (Exception e) {
                ErrorReporting.onError(TAG, e);
                //Log.e(TAG, e.getMessage());
                if (!SdmmsConsts.PRODUCTION) 
                	if (e != null) e.printStackTrace();
            }
        	if (SdmmsConsts.DEBUG_SCAN) 
            	Log.i(TAG, "scanApps: scan complete, list size: " + scanList.size());
            SDDBConn conn = sddb.open(true);
            scanList.scanID = sddb.insertScanList(scanList);
            sddb.close(conn);
        	if (SdmmsConsts.DEBUG_SCAN) 
            	Log.i(TAG, "scanApps: saved, list size: " + scanList.size());
        	
        	boolean sendNotifications = false;
        	
        	if (sendNotifications) {
	            if (scanList.getRegisteredCount() > 0) {
	            	ASNotificationManager.sendScanNotifications(spaContext, ASNotificationManager.BG_SCAN_COMPLETE_WARNING_NOTIFY);
	            } else ASNotificationManager.sendScanNotifications(spaContext, ASNotificationManager.BG_SCAN_COMPLETE_CLEAR_NOTIFY);
        	}
        	
            scanComplete = true;
            synchronized (scanInProgress) {
            	scanInProgress = false;
            }
            newScanAvailable = true;
            for (SDScanListener listener : mScanListener) {
            	listener.scanComplete(null, ACTION_PACKAGE_FULL_SCAN);
            }
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(INITIAL_SCAN_COMPLETE, true);
            edit.commit();
        }
    };
    
    public boolean packageInstalled(String packageName) {
    	boolean found = false;
        SDDBConn conn = sddb.open(true);
        found = sddb.hasPackage(packageName);
        sddb.close(conn);
    	return found;
    }

    public void scanInstallNow(final String packageName) {
        synchronized (scanUninstallInProgress) {
    	if (packageName.equals(currentScanPkg) && !scanUninstallInProgress) return;
    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanInstallNow packageName:" + packageName);
    		synchronized (scanInProgress) {
	        	synchronized (scanComplete) {
	        		while (!scanComplete && scanInProgress) {
	        			try {
	        				wait(1000);
	        			} catch (Exception e) {
	        				// do nothing...
	        			}
	        	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanInstallNow waiting to scan:" + packageName);
	        		}
	        	}
	        	currentScanPkg = packageName;
	        	scanComplete = false;
	        	scanInProgress = true;
            }
        }
    	//ASNotificationManager.sendInstallScanNotification(spaContext, 
    	//		ASNotificationManager.INSTALL_SCAN_PROGRESS_NOTIFY,
    	//		packageName);
        Thread thread = new Thread(null, new Runnable() {
            @Override
            public void run() {
                SDAppList scanList = new SDAppList();
                try {
                	scanList = scanRegisteredApp(spaContext, packageName);
                    scanList.setCurrentScan(true);
                } catch (Exception e) {
                    ErrorReporting.onError(TAG, e);
                    if (e != null) {
                    	if (e.getMessage() != null) Log.e(TAG, e.getMessage());
                    	e.printStackTrace();
                	}
                }
/**                SDDBConn conn = spaDB.open(true);
                scanList.scanID = spaDB.insertScanList(scanList.scanStartDt, scanList.scanEndDt, scanList.scanFlgs, scanList.appListID);
                for (SDAppInfo tmpPkgInfo: scanList.scanList) {
                	tmpPkgInfo.scanListId = scanList.scanID;
                	tmpPkgInfo.setUNINSTALLED(false);
            		spaDB.insertScanItem(tmpPkgInfo);
                	if (tmpPkgInfo.listDt == scanList.appListDate) 
                		spaDB.updatePackageScanInfo(tmpPkgInfo);
                	// send notifications for each installed package
                    if (tmpPkgInfo.isUnsafe()) {
                    	ASNotificationManager.sendInstallScanNotification(spaContext, 
                    		ASNotificationManager.INSTALL_SCAN_COMPLETE_WARNING_NOTIFY,
                    		tmpPkgInfo.appName);
                    } else ASNotificationManager.sendInstallScanNotification(spaContext, 
                    		ASNotificationManager.INSTALL_SCAN_COMPLETE_CLEAR_NOTIFY,
                    		tmpPkgInfo.appName);
                }
                spaDB.setCurrentScanList(scanList.scanID);
                mScanList = spaDB.getAllScanList();
                spaDB.close(conn);
                scanComplete = true;
**/                
	        	currentScanPkg = "";
                synchronized (scanInProgress) {
                	scanInProgress = false;
                }
                newScanAvailable = true;
                for (SDScanListener listener : mScanListener) {
                	if (listener != null) listener.scanComplete(packageName, ACTION_PACKAGE_ADDED);
                }
            }
        }, "ScanningInstalledApp");
        thread.start();
	}
    
    public void scanUpdateNow(final String packageName) {
    	scanComplete = false;
        synchronized (scanInProgress) {
        	currentScanPkg = packageName;
        	scanInProgress = true;
        }
        Thread thread = new Thread(null, new Runnable() {
            @Override
            public void run() {
                SDAppList scanList = new SDAppList();
                try {
                	scanList = scanForRegisteredApps(spaContext);
                    scanList.setCurrentScan(true);
                } catch (Exception e) {
                    ErrorReporting.onError(TAG, e);
                    if (e != null) {
                    	if (e.getMessage() != null) Log.e(TAG, e.getMessage());
                    	e.printStackTrace();
                    }
                }
	        	currentScanPkg = "";
				synchronized (scanInProgress) {
                	scanInProgress = false;
                }
                newScanAvailable = true;
                for (SDScanListener listener : mScanListener) {
                	listener.scanComplete(packageName, ACTION_PACKAGE_UPDATED);
                }
            }
        }, "ScanningInstalledApp");
        thread.start();
	}
    
	public SDAppList scanUninstall(Context context, String installPkgName) {
		SDAppList appList = new SDAppList();
		appList.scanStartDt = Calendar.getInstance().getTimeInMillis();
		SDDB spaDB = SDSmsManagerApp.getSDDB();
		SDDBConn conn = spaDB.open(false);
		spaDB.deletePkgInfo(spaDB.getInstalledPackage(installPkgName));
		spaDB.close(conn);
		appList.scanEndDt = Calendar.getInstance().getTimeInMillis();
		return appList;
	}
	
    public void logUninstall(final String packageName) {
    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "logUninstall uninstalling:" + packageName);
    	scanComplete = false;
        synchronized (scanInProgress) {
        	currentScanPkg = packageName;
        	scanInProgress = true;
        }
        synchronized (scanUninstallInProgress) {
        	scanUninstallInProgress = true;
        }
        
    	//ASNotificationManager.sendInstallScanNotification(spaContext, 
    	//		ASNotificationManager.UNINSTALL_SCAN_PROGRESS_NOTIFY,
    	//		packageName);
        Thread thread = new Thread(null, new Runnable() {
            @Override
            public void run() {
                SDAppList scanList = new SDAppList();
                try {
                    scanList = scanUninstall(spaContext, packageName);
                    scanList.setCurrentScan(true);
                } catch (Exception e) {
                    ErrorReporting.onError(TAG, e);
                    if (e != null) {
                    	if (e.getMessage() != null) Log.e(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
                String appName = "";
                for (AppScanInfo tmpPkgInfo: scanList.scanList) {
                	tmpPkgInfo.setUNINSTALLED(true);
                	if (appName.length() < 1) appName = tmpPkgInfo.appName;
                }
                
                SDSmsManagerApp.getRegistrations().removeRegistration(packageName);
                
                SDDBConn conn = sddb.open(true);
                scanList.scanID = sddb.insertScanList(scanList);
                sddb.setCurrentScanList(scanList.scanID);
                sddb.close(conn);
                scanComplete = true;
	        	currentScanPkg = "";
                synchronized (scanUninstallInProgress) {
                	scanUninstallInProgress = false;
                }
                synchronized (scanInProgress) {
                	scanInProgress = false;
                }
                newScanAvailable = true;
                for (SDScanListener listener : mScanListener) {
                	if (listener != null) listener.scanComplete(packageName, ACTION_PACKAGE_REMOVED);
                }
                boolean sendNotifications = false;
                if (sendNotifications) for (AppScanInfo tmpPkgInfo: scanList.scanList) {
                	// send notifications for each installed package
                    if (tmpPkgInfo.isUnsafe()) {
                    	ASNotificationManager.sendScanNotifications(spaContext, 
                    		ASNotificationManager.UNINSTALL_SCAN_COMPLETE_WARNING_NOTIFY,
                    		tmpPkgInfo.appName);
                    } else ASNotificationManager.sendScanNotifications(spaContext, 
                    		ASNotificationManager.UNINSTALL_SCAN_COMPLETE_CLEAR_NOTIFY,
                    		tmpPkgInfo.appName);
                }
                //if (getInstalledPackages().getMessagingCompatibleList().size() < 1)
                //	ASNotificationManager.sendScanNotifications(spaContext, 
                //		ASNotificationManager.UNINSTALL_NO_COMPATIBLE_SMS_APPS_NOTIFY,
                //		appName);
            }
        }, "ScanningUninstalledApp");
        thread.start();
	}
    
    public boolean isScanComplete() {
    	return scanComplete;
    }
    
    public boolean hasNewScan() {
    	return newScanAvailable;
    }
    
    public boolean scanInProgress() {
    	synchronized (scanInProgress) {
    		return scanInProgress;
    	}
    }
    
    public SDAppList getScanPkgInfo(SDAppList scanList) {
		SDDBConn conn = sddb.open(false);
    	if (scanList.scanList == null || scanList.scanList.size() < 1||newScanAvailable) {
    		for (int i = 0; i < scanList.scanList.size() ; i++) {
    	    	if (SdmmsConsts.DEBUG_SCAN) 
    				Log.i(TAG, "getScanPkgInfo: scanList.scanList.get(i) package Id before: " + scanList.scanList.get(i).pkgId);
    			if (scanList.scanList.get(i).pkgId < 1) scanList.scanList.get(i).pkgId 
    				= sddb.getPackageId(scanList.scanList.get(i).pkgName);
    	    	if (SdmmsConsts.DEBUG_SCAN) 
    				Log.i(TAG, "getScanPkgInfo: scanList.scanList.get(i) package Id after: " + scanList.scanList.get(i).pkgId);
    			sddb.getInstalledPackage(scanList.scanList.get(i));
    	    	if (SdmmsConsts.DEBUG_SCAN) 
    				Log.i(TAG, "getScanPkgInfo: scanList.scanList.get(i) package name after: " + scanList.scanList.get(i).pkgName);
    		}
    		scanList.scanList = sddb.getScanListPackages(scanList.scanID);
    	}
    	for (int i = 0; i < scanList.scanList.size(); i++ ) {
    		if (scanList.scanList.get(i).pkgName.length() < 1) {
    	    	if (SdmmsConsts.DEBUG_SCAN) 
    				Log.i(TAG, "getScanPkgInfo: scanList.scanList.get(i) package name empty, pkgId: " + scanList.scanList.get(i).pkgId);
    			sddb.getScanPackageInfo(scanList.scanList.get(i));
    		}
    	}
		sddb.close(conn);
    	newScanAvailable = false;
		return scanList;
    }
	
    public SDAppList getScanList(SDAppList scanList) {
    	if (scanList.scanList == null || scanList.scanList.size() < 1||newScanAvailable) {
    		SDDBConn conn = sddb.open(false);
    		scanList.scanList = sddb.getScanListPackages(scanList.scanID);
    		sddb.close(conn);
    	}
        getScanPkgInfo(scanList);
    	newScanAvailable = false;
		return scanList;
    }
	
    public SDAppList getScanList(int scanListId) {
        SDDBConn conn = sddb.open(false);
        SDAppList appList = sddb.getCurrentScanList();
        sddb.close(conn);
		return appList;
    }
	
    public ArrayList<SDAppList> getScanHistoryList() {
    	ArrayList<SDAppList> mScanList = new ArrayList<SDAppList>();
		SDDBConn conn = sddb.open(false);
    	mScanList = sddb.getAllScanList();
		sddb.close(conn);
    	if (mScanList == null || mScanList.size() < 1) return mScanList;
		newScanAvailable = false;
		return mScanList;
    }
    
	public void saveScanList(SDDB tmpDB, SDAppList appList) {
		SDDBConn conn = tmpDB.open(true);
		appList.scanID = tmpDB.insertScanList(appList);
		tmpDB.close(conn);
	}
	
	public SDAppList getRegisteredList(SDDB tmpDB) {
		SDAppList appList = new SDAppList();
		SDDBConn tmpConn = tmpDB.open(false);
		appList.scanList = tmpDB.getRegisteredPackages();
		appList.appListID = tmpDB.getCurrentRegisteredAppListId();
		tmpDB.getAppListInfo(appList);
		tmpDB.closeConn(tmpConn);
		return appList;
	}
	
	public SDAppList scanForRegisteredApps(Context context) {
		SDAppList appList = new SDAppList();
    	if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "scanForRegisteredApps: starting ");
		appList.scanStartDt = Calendar.getInstance().getTimeInMillis();
		SDDB spaDB = SDSmsManagerApp.getSDDB();
		SDAppListUpdateListener listener = appList.getSDAppListUpdateListener();
		spaDB.setScanPkgListUpdateListener(listener);
        for (SDScanListener mListener : mScanListener) {
        	scanEngine.addListener(mListener);
        }
		scanEngine.getInstalledApps(appList, context.getPackageManager(), spaDB, true);
        for (SDScanListener mListener : mScanListener) {
        	scanEngine.removeListener(mListener);
        }
		if (listener != null) 
			listener.updateScanProgress(appList.getScanItemsTotal(), appList.getScanItemsProgress());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: installed apps, list size: " + appList.scanList.size());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: nonSMS apps, list size: " + appList.getNonSMSCount());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: otherSMS apps, list size: " + appList.getSMSOtherCount());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: SMS apps, list size: " + appList.getSMSCount());
		//scanList = ASScanEngine.getRegisteredInstalledLists(spaDB);
        //ASScanEngine.scanListForSMS(context.getPackageManager(), this);
        if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "scanForRegisteredApps: finished, list size: " + appList.scanList.size());
        saveScanList(spaDB, appList);
		spaDB.removeScanPkgListUpdateListener(listener);
		return appList;
	}
	
	public SDAppList scanRegisteredApp(Context context, String pkgName) {
		// TODO 
		// scan for single app install
		
		boolean ignore = true;
		
		if (ignore) return scanForRegisteredApps(context);
		SDAppList appList = new SDAppList();
    	if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "scanForRegisteredApps: starting ");
		appList.scanStartDt = Calendar.getInstance().getTimeInMillis();
		SDDB spaDB = SDSmsManagerApp.getSDDB();
		SDAppListUpdateListener listener = appList.getSDAppListUpdateListener();
		spaDB.setScanPkgListUpdateListener(listener);
        for (SDScanListener mListener : mScanListener) {
        	scanEngine.addListener(mListener);
        }
        AppScanInfo appInfo = spaDB.getInstalledPackage(pkgName);
        appList.scanList.add(appInfo);
        scanEngine.addInstalledApp(appList, context.getPackageManager(), spaDB, pkgName);
		//scanEngine.getInstalledApps(appList, context.getPackageManager(), spaDB, true);
        for (SDScanListener mListener : mScanListener) {
        	scanEngine.removeListener(mListener);
        }
		if (listener != null) 
			listener.updateScanProgress(appList.getScanItemsTotal(), appList.getScanItemsProgress());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: installed apps, list size: " + appList.scanList.size());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: nonSMS apps, list size: " + appList.getNonSMSCount());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: otherSMS apps, list size: " + appList.getSMSOtherCount());
    	if (SdmmsConsts.DEBUG_SCAN) 
        	Log.i(TAG, "scanForRegisteredApps: SMS apps, list size: " + appList.getSMSCount());
		//scanList = ASScanEngine.getRegisteredInstalledLists(spaDB);
        //ASScanEngine.scanListForSMS(context.getPackageManager(), this);
        if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "scanForRegisteredApps: finished, list size: " + appList.scanList.size());
        saveScanList(spaDB, appList);
		spaDB.removeScanPkgListUpdateListener(listener);
		return appList;
	}
	
/**	public boolean scanInstallForRegistration(Context context, String installPkgName) {
	scanStartDt = Calendar.getInstance().getTimeInMillis();
	SDDB spaDB = SDSmsManagerApp.getSDDB();
	scanList = ASScanEngine.addInstalledApp(this, context.getPackageManager(), spaDB, installPkgName);
    //ASScanEngine.scanListForSMS(context.getPackageManager(), this);
	if (scanList.size() > 0) scanList = ASScanEngine.getRegisteredInstalledApp(spaDB, scanList.get(0).pkgId);
	scanEndDt = Calendar.getInstance().getTimeInMillis();
	return true;
}
**/	
	
	public enum AppListComparator implements Comparator<AppScanInfo> {
	    INSTALL_DT {
	        public int compare(AppScanInfo o1, AppScanInfo o2) {
	            return Long.valueOf(o1.installDt).compareTo(o2.installDt);
	        }},
	        
	    PRIORITY {
	        public int compare(AppScanInfo o1, AppScanInfo o2) {
	            return Integer.valueOf(o1.sds_priority).compareTo(o2.sds_priority);
	        }},
	        
	    USER_PRIORITY {
	        public int compare(AppScanInfo o1, AppScanInfo o2) {
	            return Integer.valueOf(o1.user_priority).compareTo(o2.user_priority);
	        }},
	        
	    APP_NAME {
	        public int compare(AppScanInfo o1, AppScanInfo o2) {
	            return o1.appName.compareTo(o2.appName);
	        }},
	        
	    PACKAGE_NAME {
	        public int compare(AppScanInfo o1, AppScanInfo o2) {
	            return o1.pkgName.compareTo(o2.pkgName);
	        }};

	    public static Comparator<AppScanInfo> decending(final Comparator<AppScanInfo> other) {
	        return new Comparator<AppScanInfo>() {
	            public int compare(AppScanInfo o1, AppScanInfo o2) {
	                return -1 * other.compare(o1, o2);
	            }
	        };
	    }

	    public static Comparator<AppScanInfo> getComparator(final AppListComparator... multipleOptions) {
	        return new Comparator<AppScanInfo>() {
	            public int compare(AppScanInfo o1, AppScanInfo o2) {
	                for (AppListComparator option : multipleOptions) {
	                    int result = option.compare(o1, o2);
	                    if (result != 0) {
	                        return result;
	                    }
	                }
	                return 0;
	            }
	        };
	    }
	}
}
