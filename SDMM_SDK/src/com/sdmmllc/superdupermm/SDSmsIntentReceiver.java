package com.sdmmllc.superdupermm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class SDSmsIntentReceiver extends BroadcastReceiver {

	String TAG = "SDSmsIntentReceiver";
	PackageManager pkgMgr;
	String installPkgName = "";
	
	@Override
	public void onReceive(Context context, Intent intent) {
    	if (SDSmsConsts.DEBUG_SERVICE) 
			Log.i(TAG, "received intent for SDSmsConsts.SDSMS_RELOAD:" + SDSmsConsts.SDSMS_RELOAD);
	    pkgMgr = context.getPackageManager();
	    installPkgName = intent.getDataString();
	    installPkgName = installPkgName.substring(installPkgName.indexOf(":") + 1, installPkgName.length());
		// add the app - returns true if added (non-system packages are not added
		if (SDSmsConsts.SDSMS_INSTALL_PKG.equals(installPkgName)) {
	    	if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
	    		Log.i(TAG, "package installed or updated:" + installPkgName + " connecting / reconnecting / diconnecting services");
	    	if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
		    	SDSmsManager.disconnect();
	    	else SDSmsManager.reconnect();
		}
		if (intent.getAction().equals(SDSmsConsts.SDSMS_RELOAD)) {
			SDSmsManager.reconnect();
			intent.putExtra(SDSmsManager.TEST_RESPONSE, SDSmsManager.RESPONSE_MSG);
			intent.putExtra(SDSmsManager.TEST_PACKAGE, context.getPackageName());
			intent.putExtra(SDSmsManager.TEST_VERSION, SDSmsManager.getVersion());
		}
	}
}
