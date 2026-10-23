package com.sdmmllc.superdupermm;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.utils.FakeSMS;

public class MessagingServiceModel {

	public static final String TAG = "MessagingServiceModel";
	
	private Map<Integer, MessagingServiceController> controllers;
	private Map<String, String> types;
	private Map<Integer, Hashtable<String, String>> registrations;
	private ISDSmsReceiveService mBinder;
	
	public MessagingServiceModel() {
		init();
	}
	
	public ISDSmsReceiveService getBinder() {
		if (mBinder == null) return null;
		return mBinder;
	}
	
	public void setBinder(ISDSmsReceiveService binder) {
		mBinder = binder;
	}
	
	private void init() {
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "init initializing model");
		controllers = new Hashtable<Integer, MessagingServiceController>();
		types = new Hashtable<String, String>();
		registrations = new Hashtable<Integer, Hashtable<String, String>>();
		mBinder = null;
	}
	
	public boolean registerApp(int type, String id, String packageName) {
		if (type < SDSmsManager.ACCESS_TYPE_SYSTEM || type > SDSmsManager.ACCESS_TYPE_UNREGISTERED) return false;
		//TODO
		// add registration checking
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "registerApp type: " + type + " id: " + id + " packageName: "
				+ packageName);
		if (registrations.containsKey(type)) registrations.get(type).put(id, packageName);
		else {
			types = new Hashtable<String, String>();
			types.put(id, packageName);
			registrations.put(type, (Hashtable<String, String>) types);
		}
		return true;
	}
	
	public boolean isRegistered(int type, String id, String packageName) {
		return (registrations.containsKey(type) && registrations.get(type).containsKey(id) &&
				registrations.get(type).get(id).equals(packageName));
	}
	
	public Set<Integer> getTypes() {
		return registrations.keySet();
	}
	
	public Set<String> getRegistationIds(Integer type) {
		return registrations.get(type).keySet();
	}
	
	public List<String> getAllRegisteredApps() {
		List<String> apps = new ArrayList<String>();
		for (Hashtable<String, String> types : registrations.values()) {
			apps.addAll(types.values());
		}
		return apps;
	}
	
	public MessagingServiceController getControllers(int type) {
		if (type < SDSmsManager.ACCESS_TYPE_SYSTEM || type > SDSmsManager.ACCESS_TYPE_UNREGISTERED) 
			return null;
		if (!controllers.containsKey(type)) controllers.put(type, new MessagingServiceController(type));
		return controllers.get(type);
	}
	
	public void checkControllers() {
		if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.w(TAG, "reloading controllers -------------------------------- ");
		for (int i = SDSmsManager.ACCESS_TYPE_SYSTEM ; i <= SDSmsManager.ACCESS_TYPE_UNREGISTERED; i++) {
			MessagingServiceController controller = getControllers(i);
			for (String id : controller.getIds()) {
				if (!controller.getPkgName(id).equals(SDSmsConsts.SDSMS_INSTALL_PKG)) {
					if (controller.getCallback(id).reload()) {
						if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.w(TAG, "reloading controller for: " + controller.getPkgName(id));
						SDSmsManager.connect(controller.getPkgName(id));
						synchronized (this) {
							while (controller.getCallback(id).reload()) {
								try {
									if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.w(TAG, "waiting to reload controller for: " + controller.getPkgName(id));
									wait(100);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
	}

	public String[] testControllers(MessagingServiceListener listener) {
		//checkControllers();
    	SDSmsManagerApp.reconnectApps();

		if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.w(TAG, "testing controllers -------------------------------- ");
		Intent testBroadcastReceived = FakeSMS.createFakeSms(SdmmsConsts.SMS_RECEIVED, "8501235555", "SDMM Text Message");
		testBroadcastReceived.putExtra(SDSmsManager.TEST_BROADCAST, SDSmsManager.TEST_MSG);
		testBroadcastReceived.putExtra(SDSmsConsts.SDSMS_DUPLICATE, SDSmsConsts.SDSMS_DUPLICATE);
		Intent testBroadcastDeliver = FakeSMS.createFakeSms(SdmmsConsts.SMS_DELIVER, "8501235555", "SDMM Text Message");
		testBroadcastDeliver.putExtra(SDSmsManager.TEST_BROADCAST, SDSmsManager.TEST_MSG);
		testBroadcastDeliver.putExtra(SDSmsConsts.SDSMS_DUPLICATE, SDSmsConsts.SDSMS_DUPLICATE);
		for (int i = SDSmsManager.ACCESS_TYPE_SYSTEM ; i <= SDSmsManager.ACCESS_TYPE_UNREGISTERED; i++) {
			if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.w(TAG, "testing controller: " + i);
			MessagingServiceController controller = getControllers(i);
			if (i != SDSmsManager.ACCESS_TYPE_MESSAGING) controller.testSMS(testBroadcastReceived, listener);
			else controller.testSMS(testBroadcastDeliver, listener);
		}
		return new String[0];
	}
}
