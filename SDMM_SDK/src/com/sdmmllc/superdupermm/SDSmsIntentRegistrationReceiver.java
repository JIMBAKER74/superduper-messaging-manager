package com.sdmmllc.superdupermm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class SDSmsIntentRegistrationReceiver extends BroadcastReceiver {

	String TAG = "SDSmsIntentRegistrationReceiver";
	PackageManager pkgMgr;
	String installPkgName = "";
	
	@Override
	public void onReceive(Context context, Intent intent) {
    	if (SDSmsConsts.DEBUG_SERVICE) 
			Log.i(TAG, "received intent for SDSmsConsts.SDSMS_RELOAD:" + SDSmsConsts.SDSMS_RELOAD);
	    pkgMgr = context.getPackageManager();
	    installPkgName = context.getPackageName();
		// SDMM must have crashed and is sending a reconnect request
		if (SDSmsConsts.SDSMS_RELOAD.equals(intent.getAction())) {
	    	//if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
	    		Log.i(TAG, "package installed or updated:" + installPkgName + " connecting / reconnecting / diconnecting services");
			SDSmsManager.reconnect();
			intent.putExtra(SDSmsManager.TEST_RESPONSE, SDSmsManager.TEST_MSG);
			intent.putExtra(SDSmsManager.TEST_VERSION, SDSmsManager.getVersion());
		}
	}
}
