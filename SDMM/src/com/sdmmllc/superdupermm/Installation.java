package com.sdmmllc.superdupermm;

import java.util.Calendar;
import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings.Secure;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class Installation {

	public String TAG = "Installation";
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    long installTS = 0;
    String android_ID = "", serial_ID = "", uuid = "";

	public Installation(Context context) {
		settings = context.getSharedPreferences(SdmmsConsts.INSTALL_FILE, 0);
		if (!settings.contains(SdmmsConsts.installTS)) {
			installTS = Calendar.getInstance().getTimeInMillis();
			int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
				android_ID = Secure.ANDROID_ID;
			if (sdkVersion >= Build.VERSION_CODES.GINGERBREAD) {
				serial_ID = android.os.Build.SERIAL;
			} else {
				serial_ID = "pre-Gingerbread";
			}
			uuid = UUID.randomUUID().toString();
			editor = settings.edit();
			editor.putLong(SdmmsConsts.installTS, installTS);
			editor.putString(SdmmsConsts.deviceID, android_ID);
			editor.putString(SdmmsConsts.serialID, serial_ID);
			editor.putString(SdmmsConsts.uuid, uuid);
			editor.putString(SdmmsConsts.newInstall, "Y");
			editor.commit();
		} else {
			installTS = settings.getLong(SdmmsConsts.installTS, 0);
			android_ID = settings.getString(SdmmsConsts.deviceID, "");
			serial_ID = settings.getString(SdmmsConsts.serialID, "");
			uuid = settings.getString(SdmmsConsts.uuid, "");
		}
	}
	
	public void finishInstall(Context context) {
		settings = context.getSharedPreferences(SdmmsConsts.INSTALL_FILE, 0);
		editor = settings.edit();
		editor.putString(SdmmsConsts.newInstall, "N");
		editor.commit();
	}
	
	public boolean isNewInstall(Context context) {
		settings = context.getSharedPreferences(SdmmsConsts.INSTALL_FILE, 0);
		return (settings.getString(SdmmsConsts.newInstall, "Y").equals("Y"));
		
	}
}
