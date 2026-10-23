package com.messagingappsllc.superdupermms.mms.model;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.messagingappsllc.superdupermms.mms.GoTxtmeApp;
import com.messagingappsllc.superdupermms.mms.R;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

public class AppThemeModel {
	
	public static final String TAG = "AppThemeModel";

	private Hashtable<String, String> themes = new Hashtable<String, String>();
	private String currentTheme = "";
	private PackageManager pkgMgr;
	private Context mContext;
	public static final String 
		THEME_KEY = "themes_key",
		THEME_SELECTED = "theme_selected";
	
	public AppThemeModel(Context context) {
		mContext = context;
		pkgMgr = context.getPackageManager();
		getThemes();
	}
	
	public boolean addTheme(String installPkgName) {
		ApplicationInfo pinfo = null;
		try {
			pinfo = pkgMgr.getApplicationInfo(installPkgName, PackageManager.GET_META_DATA);
			if (SDSmsConsts.DEBUG_INSTALL_RECEIVER) 
				Log.i(TAG, "package theme found:" + installPkgName + " META_DATA:" + (String)pinfo.metaData.get("theme_name"));
			themes.put(installPkgName, (String)pinfo.metaData.get("theme_name"));
			saveThemes();
		} catch(Exception e) {
		    e.printStackTrace();
		}
		return false;
	}
	
	public void removeTheme(String installPkgName) {
		themes.remove(installPkgName);
		if (installPkgName.equals(currentTheme)) currentTheme = GoTxtmeApp.getApplication().getPackageName();
		saveThemes();
	}
	
	public String getThemeName(String packageName) {
		return themes.get(packageName);
	}
	
	public int size() {
		return themes.size();
	}
	
	private void saveThemes() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefsEditor = appSharedPrefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(themes);
		prefsEditor.putString(THEME_KEY, json);
		prefsEditor.putString(THEME_SELECTED, currentTheme);
		prefsEditor.commit();
	}
	
	public Hashtable<String, String> getThemes() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Gson gson = new Gson();
		String json = appSharedPrefs.getString(THEME_KEY, "");
		themes = (Hashtable<String, String>)gson.fromJson(json, Hashtable.class);
		if (themes == null) themes = new Hashtable<String, String>();
		currentTheme = appSharedPrefs.getString(THEME_SELECTED, GoTxtmeApp.getApplication().getPackageName());

		final PackageManager pm = mContext.getPackageManager();
		//get a list of installed apps.
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
    	String prefix = mContext.getString(R.string.themePackagePrefix);

    	Set<String> themeNames = new HashSet<String>();
    	for (String name : themes.keySet()) {
    		themeNames.add(new String(name));
    	}
    	
    	boolean found = false;
		for (String themePkgName : themeNames) {
			found = false;
			for (ApplicationInfo packageInfo : packages) {
			    if (packageInfo.packageName.startsWith(themePkgName)) found = true;
			}
			if (!found) removeTheme(themePkgName);
		}
		saveThemes();

		return themes;
	}
}
