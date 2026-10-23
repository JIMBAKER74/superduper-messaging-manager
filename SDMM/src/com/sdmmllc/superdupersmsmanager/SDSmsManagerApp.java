package com.sdmmllc.superdupersmsmanager;

import java.util.ArrayList;
import java.util.Collection;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.Tracker;
import com.messagingappsllc.superdupermms.mms.GoTxtmeApp;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.MessagingServiceListener;
import com.sdmmllc.superdupermm.MessagingServiceModel;
import com.sdmmllc.superdupermm.Utils;
import com.sdmmllc.superdupermm.comms.CommManager;
import com.sdmmllc.superdupermm.data.AppRegistrationModel;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.notifications.ASNotificationManager;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class SDSmsManagerApp extends GoTxtmeApp {

	private static String TAG = "SDSmsManagerApp";
	private static Context mContext;
	private static SDSmsManagerApp mInstance;
	private static SmsManager sManager;
	private static SDDB appDB;
	private static CommManager mCommManager;
	private static SDScanManager mSDScanManager;
	private static SDAppList currentScan, installedPackages;
	private static ASNotificationManager mNotificationManager;
    private static SDSmsManager mSdmmsManager;
    private static MessagingServiceModel mControllers;
    private static AppRegistrationModel mRegistrations;

	private static boolean isDefaultSms;
	private static boolean useKitKatFeatures;
	private static int version;
	
	/*
	 * Google Analytics
	 */
	private static GoogleAnalytics mGa;
	private static Tracker mTracker;
	// Dispatch period in seconds.
	private static final int GA_DISPATCH_PERIOD = 30;
	// Prevent hits from being sent to reports, i.e. during testing.
	private static final boolean GA_IS_DRY_RUN = false;
	// GA Logger verbosity.
	private static final LogLevel GA_LOG_VERBOSITY = LogLevel.INFO;
	// Key used to store a user's tracking preferences in SharedPreferences.
	private static final String TRACKING_PREF_KEY = "trackingPreference";
	private void initializeGa() {
		mGa = GoogleAnalytics.getInstance(this);
		mTracker = mGa.getTracker(getString(R.string.ga_trackingId));
		// Set dispatch period.
		GAServiceManager.getInstance().setLocalDispatchPeriod(GA_DISPATCH_PERIOD);
		// Set dryRun flag.
		mGa.setDryRun(GA_IS_DRY_RUN);
		// Set Logger verbosity.
		mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);
	}

	public static Tracker getGaTracker() {
		return mTracker;
	}

	public static GoogleAnalytics getGaInstance() {
		return mGa;
	}
	
	public static final String CHECK_SDSMS_STATUS = "CheckSDSmsStatus";

	public SDSmsManagerApp() {
		mInstance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
        ACRA.init(this);
		mContext = this.getApplicationContext();
		GoTxtmeApp.setContext(mContext);
		appDB = new SDDB(this, true);
		Intent checkStatus = new Intent(SDSmsManagerApp.CHECK_SDSMS_STATUS);
		this.sendBroadcast(checkStatus);
		init();
		sManager = SmsManager.getDefault();
		mCommManager = new CommManager(this);
		mSDScanManager = new SDScanManager(this); 
		mNotificationManager = new ASNotificationManager(this);
        mControllers = new MessagingServiceModel();
        mSdmmsManager = SDSmsManager.getDefault(this, "SDSmsManager", SDSmsManager.ACCESS_TYPE_SYSTEM,
        		"com.sdmmllc.superdupermm.SDSmsSendService",
        		"com.sdmmllc.superdupermm.SmsReceiver");
        mRegistrations = new AppRegistrationModel(this);
        initializeGa();
        version = Integer.valueOf(this.getString(R.string.app_version));
    }
	
	public static MessagingServiceModel getMessagingServiceModel() {
		if (mControllers == null) mControllers = new MessagingServiceModel();
		return mControllers;
	}
	
	public static SDSmsManager getSDSmsManager() {
		if (mInstance == null) mInstance = new SDSmsManagerApp();
		return SDSmsManagerApp.mSdmmsManager;
	}
	
	private void init() {
        if (Utils.hasKitKat()) {
            if (Utils.isDefaultSmsApp(this)) {
                // This app is the default, remove the "make this app the default" layout and
                // enable message sending components.
            	isDefaultSms = true;
            } else {
                // Not the default, show the "make this app the default" layout and disable
                // message sending components.
            	isDefaultSms = false;
            }
        } else {
        	// none KitKat settings
        	isDefaultSms = false;
        }
	}
	
	public static int getVersion() {
		return version;
	}
	
	public static AppRegistrationModel getRegistrations() {
		if (mInstance == null) mInstance = new SDSmsManagerApp();
		return mRegistrations;
	}
	
	public static SDAppList getCurrentScan() {
		currentScan = mSDScanManager.getCurrentScan();
		if (currentScan == null) currentScan = new SDAppList(new ArrayList<AppScanInfo>());
		return currentScan;
	}
	
	public static SDAppList setCurrentScan(SDAppList scan) {
		currentScan = scan;
		if (currentScan == null) currentScan = new SDAppList(new ArrayList<AppScanInfo>());
		return currentScan;
	}
	
	public static SDAppList getInstalledPackages() {
		installedPackages = mSDScanManager.getInstalledPackages();
		if (installedPackages == null) installedPackages = new SDAppList(new ArrayList<AppScanInfo>());
		return installedPackages;
	}
	
	public static SDAppList getRegisteredPackages() {
		SDAppList retval = new SDAppList();
		Collection<String> apps = mRegistrations.getRegistrations().values();
		SDAppList packages = mSDScanManager.getInstalledPackages();
		for (String pkgName : apps) {
			if (!pkgName.equals(SDSmsManager.SDSMS_PACKAGE)) 
				retval.addPkg(packages.getPackage(pkgName));
		}
		return retval;
	}
	
	public static void reconnectApps() {
		SDAppList apps = getRegisteredPackages();
		for (AppScanInfo app : apps.scanList) {
			if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "connecting to :" + app.pkgName);
			SDSmsManager.connect(app.pkgName);
		}
	}
	
	public static SDAppList setInstalledPackages(SDAppList packages) {
		installedPackages = packages;
		if (installedPackages == null) installedPackages = new SDAppList(new ArrayList<AppScanInfo>());
		return installedPackages;
	}
	
	public static void testRegisteredApps(MessagingServiceListener listener) {
		mControllers.testControllers(listener);
	}
	
	public static boolean isDefaultSms() {
		return isDefaultSms;
	}
	
	public static boolean useKitKatFeatures() {
		return useKitKatFeatures;
	}
	
	public static SmsManager getSmsManager() {
		if (sManager == null) sManager = SmsManager.getDefault();
		return sManager;
	}
	
	public static Context getContext() {
		if (mContext == null) mInstance = new SDSmsManagerApp();
		return mContext;
	}
	
	public static boolean getBoolean(int bool) {
		if (mInstance == null) mInstance = new SDSmsManagerApp();
		return mInstance.getApplicationContext().getResources().getBoolean(bool);
	}
	
	public static SDDB getSDDB() {
		if (appDB == null) appDB = new SDDB(mContext);
		return appDB;
	}
	
	public static CommManager getCommManager() {
		return mCommManager;
	}

	public static SDScanManager getScanManager() {
		return mSDScanManager;
	}
	
	public static ASNotificationManager getNotificationManager() {
		return mNotificationManager;
	}
}
