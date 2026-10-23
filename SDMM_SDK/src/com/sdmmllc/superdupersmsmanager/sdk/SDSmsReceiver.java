package com.sdmmllc.superdupersmsmanager.sdk;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SDSmsReceiver extends BroadcastReceiver {

	public static final String TAG = "SDSmsReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "onReceive ignored intent and doing nothing...");
		if (intent.getAction().equals(SDSmsManager.CHECK_SDSMS_STATUS) || 
				intent.getAction().endsWith(Intent.ACTION_BOOT_COMPLETED) ||
				intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
			SDSmsManager.isSdsmsIntstalled();
		}
	}
} 