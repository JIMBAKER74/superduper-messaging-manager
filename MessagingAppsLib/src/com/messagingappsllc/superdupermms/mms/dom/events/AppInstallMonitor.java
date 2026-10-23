package com.messagingappsllc.superdupermms.mms.dom.events;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.messagingappsllc.superdupermms.mms.R;
import com.messagingappsllc.superdupermms.mms.model.AppThemeModel;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class AppInstallMonitor extends BroadcastReceiver {
	String TAG = "AppInstallMonitor";
	private String installPkgName = "";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) return;
		
	    installPkgName = intent.getDataString();
	    installPkgName = installPkgName.substring(installPkgName.indexOf(":") + 1, installPkgName.length());
		// check to see if an app was added
    	if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
    		Log.i(TAG, "package installed or updated:" + installPkgName + " checking for theme app");
    	
    	if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
    		Log.i(TAG, "getString(R.string.themePackagePrefix): " + context.getString(R.string.themePackagePrefix));
    	
    	String prefix = context.getString(R.string.themePackagePrefix);
    	
    	if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) && 
    			installPkgName.equals(context.getPackageName())) {
    		AppThemeModel appThemes = new AppThemeModel(context);
    		appThemes.addTheme(installPkgName);
    	}
    	if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) && installPkgName.startsWith(prefix)) {
    		AppThemeModel appThemes = new AppThemeModel(context);
    		appThemes.addTheme(installPkgName);
    	}
    	if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) && installPkgName.startsWith(prefix)) {
    		AppThemeModel appThemes = new AppThemeModel(context);
    		appThemes.removeTheme(installPkgName);
    	}
    	if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) && installPkgName.startsWith(context.getPackageName())) {
    		AppThemeModel appThemes = new AppThemeModel(context);
    		
    		final PackageManager pm = context.getPackageManager();
    		//get a list of installed apps.
    		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

    		for (ApplicationInfo packageInfo : packages) {
    		    if (packageInfo.packageName.startsWith(prefix))
    	    		appThemes.addTheme(packageInfo.packageName);
    		}
    	}
	}
}
