package com.sdmmllc.superdupermm.scan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sdmmllc.superdupermm.notifications.ASNotificationManager;

public class SDSmsAppInstallMonitor extends BroadcastReceiver {
	String TAG = "SDSmsAppInstallMonitor";
    private ASNotificationManager mNotificationManager;
    private SDScanManager mScanManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) return;
		mNotificationManager = ASNotificationManager.getNotificationManager(context);
		mScanManager = SDScanManager.getScanManager(context);
//		if (SmsIntentReceiver.debugLevel > 8) 
			Log.i(TAG, "onReceive: processing notification request");
		if (intent.getAction().equals(ASNotificationManager.SCAN_ALARM)) {
			mScanManager.scanNow();
			mNotificationManager.resetBackgroundScan();
		}
	}
}
