package com.sdmmllc.superdupermm.xml;

import android.util.Log;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;


public class SpaXmlUpdateInfo {

	String TAG = "SpaXmlUpdateInfo";
	
	private String appVersionAvailable = "", appVersionSystem = "";
	private String spywareVersionAvailable = "", spywareVersionSystem = "";
	boolean appVersionResult = false, spywareVersionResult = false;
	
	public SpaXmlUpdateInfo(String appSysVersion, String spywareSysVersion) {
		appVersionSystem = appSysVersion;
		spywareVersionSystem = spywareSysVersion;
	}
	
	public String getAppListVersion() {
		if (spywareVersionSystem == null) return "0";
		if (spywareVersionSystem.length() < 1) return "0";
		return spywareVersionSystem;
	}
	
	public String getAppVersion() {
		if (appVersionSystem == null) return "1";
		if (appVersionSystem.length() < 1) return "1";
		return appVersionSystem;
	}
	
	public void setAppVersionAvailable (String newAppVersion) {
		appVersionAvailable = newAppVersion;
		if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "setAppVersionAvailable: appVersionAvailable:" + appVersionAvailable);
		appVersionResult = true;
	}
	
	public void setAppListVersionAvailable (String newSpyVersion) {
		spywareVersionAvailable = newSpyVersion;
		if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "setSpywareVersionAvailable: spywareVersionAvailable:" + spywareVersionAvailable);
		spywareVersionResult = true;
	}
	
	public boolean isNewAppVersionAvailable() {
		if (!appVersionResult) return false;
		return (!appVersionAvailable.equals(appVersionSystem));
	}
	
	public boolean isNewSpywareVersionAvailable() {
		if (!spywareVersionResult) return false;
		return (!spywareVersionAvailable.equals(spywareVersionSystem));
	}
	
	public boolean resultFound() {
		return (appVersionResult|spywareVersionResult);
	}
}
