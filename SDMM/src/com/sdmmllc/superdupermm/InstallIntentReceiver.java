package com.sdmmllc.superdupermm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDBConn;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupermm.ui.Act_Home;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class InstallIntentReceiver extends BroadcastReceiver {

	private String TAG = "InstallIntentReceiver";
	private SDAppList scanList;
	private Context ctx;
	private PackageManager pkgMgr;
	private NotificationManager nm;
	private String packageName = "";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String APP_NAME_ADDED = "app_name_added";
	
	@Override
	public void onReceive(Context context, Intent intent) {
    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "app install receiver for SDMM checking install...");
        if(new Installation(context).isNewInstall(context)) {
        	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "new install, finishing");
        	return;
        }

	    ctx = context;
	    Installation installation = new Installation(context);
	    if (installation.isNewInstall(context)) return;
	    SDDB spaDB = SDSmsManagerApp.getSDDB();
	    SDDBConn conn = spaDB.open(false);
	    spaDB.close(conn);
	    packageName = intent.getDataString();
	    packageName = packageName.substring(packageName.indexOf(":")+1, packageName.length());
	    if ((packageName == null)||(packageName.length()<1)) return;
	    if (packageName.equals(SDSmsConsts.SDSMS_INSTALL_PKG)
	    		|| packageName.equals(context.getPackageName())) return;
    	SDScanManager scanManager = SDSmsManagerApp.getScanManager();
	    prefs = context.getSharedPreferences(SDScanManager.SCAN_PREFS, 0);
		editor = prefs.edit();

	    if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")&&
	    		!scanManager.packageInstalled(packageName)) {
	    	// do nothing - we did not know about this package... no info to retrieve
	    } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
        	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "removing app");
	    	scanManager.logUninstall(packageName);
	    } else if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanning PACKAGE_REPLACED for SDMM compatible apps");
	    	// if we already got PACKAGE_ADDED, then do not scan.
	    	if (prefs.getString(APP_NAME_ADDED, "").equals(packageName)) {
	    		editor.putString(APP_NAME_ADDED, "");
	    	} else scanManager.scanInstallNow(packageName);
	    } else if (intent.getAction().equals("android.intent.action.PACKAGE_INSTALL")) {
	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanning PACKAGE_INSTALL for SDMM compatible apps");
	    	scanManager.scanInstallNow(packageName);
	    } else if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanning PACKAGE_ADDED for SDMM compatible apps");
			editor.putString(APP_NAME_ADDED, packageName);
			editor.commit();
	    	scanManager.scanInstallNow(packageName);
	    } else if (intent.getAction().equals("android.intent.action.PACKAGE_CHANGED")) {
	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanning PACKAGE_CHANGED for SDMM compatible apps");
	    	scanManager.scanInstallNow(packageName);
	    } else {
	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanning OTHER ACTION for SDMM compatible apps");
	    	scanManager.scanInstallNow(packageName);
	    }
	}

	public void warnHMT(Context context) {
	    pkgMgr = context.getPackageManager();
    	SDScanManager scanManager = SDSmsManagerApp.getScanManager();
    	scanManager.scanInstallNow(packageName);
    	scanList = scanManager.getCurrentScan();
		// add the app - returns true if added (non-system packages are not added
		if (scanList.scanList.size() > 0) {
	    	if (SdmmsConsts.DEBUG_INSTALL) 
	    		Log.i(TAG, "scanned : scanList.has999App(): " + scanList.has999App()
					+ " scanList.hasOver999App(): " + scanList.hasOver999App());
			if (scanList.getMessagingCompatibleCount() > 0) {
		    	if (SdmmsConsts.DEBUG_INSTALL) 
		    		Log.i(TAG, "found compatible install");
				SharedPreferences settings = ctx.getSharedPreferences(SdmmsConsts.SCAN_FILE, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean(SdmmsConsts.warnInstall, true);
				editor.putString(SdmmsConsts.warnPackage, packageName);
				editor.commit();
		    	sendNotification(ctx);
			}
	    	if (SdmmsConsts.DEBUG_INSTALL) Log.i(TAG, "scanned for SMS apps");
		}
	}

	private  void sendNotification(Context context) {
		Intent notificationIntent = new Intent(context, Act_Home.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		int icon = R.drawable.notification_icon;
		Notification notification;
		CharSequence tickerText = context.getString(R.string.statusNotificationTicker);
		CharSequence contentTitle = context.getString(R.string.statusNotificationTitle);
		CharSequence contentText = context.getString(R.string.statusNotificationText);
		long when = System.currentTimeMillis();
		notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		nm.notify(R.string.statusNotificationID, notification);
	}
	
}
