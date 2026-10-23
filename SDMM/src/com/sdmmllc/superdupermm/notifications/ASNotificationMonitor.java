package com.sdmmllc.superdupermm.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class ASNotificationMonitor extends BroadcastReceiver {
	String TAG = "ASNotificationMonitor";
    public ASNotificationManager notificationManager;
    boolean monitorMissedCallNotification = false;
    final static String CHECK_LISTENERS = "check", WRITE_LOG = "write_log",
    	DELETE_LOG = "del_log", ADD_LISTENER = "add_monitor", 
    	DELETE_LISTENER = "delete_monitor", MISSED_CALL_MONITOR = "missed_call_notice_monitor",
    	RESET_LISTENERS = "reset_all";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) return;
		if (SdmmsConsts.DEBUG_NOTIFICATION) 
			Log.i(TAG, "onStart: processing notification request");
		if (intent.getAction().equals(ASNotificationManager.DELAYED_ALARM)) {
			String type = intent.getStringExtra(ASNotificationManager.NOTIFY_DELAYED_FLG);
			ASNotificationManager.sendDelayedInstallScanNotification(context, type);
		}
	}
}
