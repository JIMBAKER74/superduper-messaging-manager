package com.sdmmllc.superdupermm.data;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class AppRegistrationModel {
	
	public static final String TAG = "AppRegistrationModel";

	private Hashtable<String, String> registrations = new Hashtable<String, String>();
	private Hashtable<String, Integer> registrationVersions = new Hashtable<String, Integer>();
	private String currentTheme = "";
	private PackageManager pkgMgr;
	private Context mContext;
	public static final String 
		REGISTRATION_KEY = "registration_key",
		REGISTRATION_VERSION_KEY = "registration_version_key";
	
	public AppRegistrationModel(Context context) {
		mContext = context;
		pkgMgr = context.getPackageManager();
		getRegistrations();
		getRegistrationVersions();
	}
	
	public boolean addRegistration(String installPkgName) {
		Log.i(TAG, "registration of package:" + installPkgName);
		ApplicationInfo pinfo = null;
		try {
			pinfo = pkgMgr.getApplicationInfo(installPkgName, 0);
			if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
				Log.i(TAG, "package found:" + installPkgName + " package name:" + pinfo.name);
			if (pinfo.name == null) 
				registrations.put(installPkgName, installPkgName);
			else
				registrations.put(installPkgName, pinfo.name);
			saveRegistrations();
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return false;
	}
	
	public void removeRegistration(String installPkgName) {
		registrations.remove(installPkgName);
		if (installPkgName.equals(currentTheme)) currentTheme = mContext.getPackageName();
		saveRegistrations();
	}
	
	public String getAppName(String packageName) {
		return registrations.get(packageName);
	}
	
	public int size() {
		return registrations.size();
	}
	
	private void saveRegistrations() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefsEditor = appSharedPrefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(registrations);
		prefsEditor.putString(REGISTRATION_KEY, json);
		prefsEditor.commit();
	}
	
	public Hashtable<String, String> getRegistrations() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Gson gson = new Gson();
		String json = appSharedPrefs.getString(REGISTRATION_KEY, "");
		registrations = (Hashtable<String, String>)gson.fromJson(json, Hashtable.class);
		if (registrations == null) registrations = new Hashtable<String, String>();

		final PackageManager pm = mContext.getPackageManager();
		//get a list of installed apps.
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

    	Set<String> pkgNames = new HashSet<String>();
    	for (String name : registrations.keySet()) {
    		pkgNames.add(new String(name));
    	}
    	
    	boolean found = false;
		for (String pkgName : pkgNames) {
			found = false;
			for (ApplicationInfo packageInfo : packages) {
			    if (packageInfo.packageName.startsWith(pkgName)) found = true;
			}
			if (!found) removeRegistration(pkgName);
		}
		saveRegistrations();

		Intent connectionIntent = new Intent(SDSmsConsts.SDSMS_RELOAD);
		mContext.sendBroadcast(connectionIntent);
		Log.w(TAG, "sent reconnection intent...");
    	
		return registrations;
	}

	public boolean addRegistrationVersion(String installPkgName, Integer pkgVersion) {
		Log.i(TAG, "registration version of package:" + installPkgName + ": " + pkgVersion);
		registrationVersions.put(installPkgName, pkgVersion);
		saveRegistrationVersions();
		return false;
	}
	
	private void saveRegistrationVersions() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefsEditor = appSharedPrefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(registrationVersions);
		prefsEditor.putString(REGISTRATION_VERSION_KEY, json);
		prefsEditor.commit();
	}
	
	public Hashtable<String, Integer> getRegistrationVersions() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Gson gson = new Gson();
		String json = appSharedPrefs.getString(REGISTRATION_VERSION_KEY, "");
		registrationVersions = (Hashtable<String, Integer>)gson.fromJson(json, Hashtable.class);
		if (registrationVersions == null) registrationVersions = new Hashtable<String, Integer>();

		return registrationVersions;
	}

}
