package com.sdmmllc.superdupermm;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupermm.data.AppRegistrationModel;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.services.SDOrderedBroadcast;

public class MessagingServiceController {

	public static final String TAG = "MessagingServiceController";
    private Hashtable<String, SDSmsReceiveCallback> callbacks;
    private Hashtable<String, String> packages;
    private Hashtable<String, String> receivers;
    private final int mType;
    private MessagingServiceModel serviceModel;
	private AppRegistrationModel registrationModel;
    private SDOrderedBroadcast broadcaster;
    private SDOrderedBroadcast.SDOrderedBroadcastListener mSDOrderedBroadcastListener;
    private SharedPreferences settings;
    
    public MessagingServiceController(int type) {
    	mType = type;
    	serviceModel = SDSmsManagerApp.getMessagingServiceModel();
		registrationModel = new AppRegistrationModel(SDSmsManager.getContext());
        settings = SDSmsManager.getContext().getSharedPreferences(SDScanManager.SCAN_PREFS, 0);
    	init();
    }
    
    private void init() {
        callbacks = new Hashtable<String, SDSmsReceiveCallback>();
        packages = new Hashtable<String, String>();
        receivers = new Hashtable<String, String>();
        broadcaster = new SDOrderedBroadcast(SDSmsManagerApp.getContext());
    }
    
    public int size() {
    	return callbacks.size();
    }
    
    public void addCallback(String id, String pkgName, String receiver, SDSmsReceiveCallback newCallback) {
    	packages.put(id, pkgName);
    	receivers.put(id, receiver);
    	callbacks.put(id, newCallback);
		Log.i(TAG, "addCallback: new controller group id: " + id + " package: " + packages.get(id));
		Log.i(TAG, "addCallback: new for caller id: " + id + " receiver:" + receivers.get(id));
    }
    
    public Set<String> getIds() {
    	return packages.keySet();
    }
 
    public String getPkgName(String id) {
    	return packages.get(id);
    }
    
    public SDSmsReceiveCallback getCallback(String id) {
    	return callbacks.get(id);
    }
    
    public void removeBinder(String id) {
    	callbacks.remove(id);
    	packages.remove(id);
    	receivers.remove(id);
    }
    
    public void launchSMS(Context context) {
    	List<AppScanInfo> appInfo = SDSmsManagerApp.getCurrentScan().getMessagingCompatibleList();
    	if (appInfo.size() < 1) {
    		// if no compatible app is found, launch SDMM 
    		Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(
    			context.getPackageName());
        	context.startActivity(LaunchIntent);
    	} else {
    		Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(
    			appInfo.get(0).pkgName);
    		context.startActivity(LaunchIntent);
    	}
    }
    
    protected boolean sendSMS(Intent intent) {
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendSMS callbacks.size(): " + callbacks.size());
		for (String id : packages.keySet()) {
			if (!packages.get(id).equals(SDSmsConsts.SDSMS_INSTALL_PKG)) {
				if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendSMS checking id: " + id + " package: " + packages.get(id));
		    	intent.setClassName(packages.get(id), receivers.get(id));
		    	mSDOrderedBroadcastListener = broadcaster.getListener(packages.get(id), receivers.get(id));
		    	broadcaster.sendOrderedBroadcast(packages.get(id), intent, mSDOrderedBroadcastListener);
		    	synchronized (this) {
		    		while(!mSDOrderedBroadcastListener.completed()) {
		    			try {
							wait(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    		}
		    	}
		    	if (mSDOrderedBroadcastListener.aborted() || intent.getBooleanExtra(SDSmsConsts.SDSMS_ABORT, false)) return true;
			}
		}
		// sms was not aborted
		return false;
    }
    
    private int waitCycles = 5, waitedCycles = 0;

    protected boolean testSMS(Intent intent, MessagingServiceListener mMessagingServiceListener) {
		if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.i(TAG, "testSMS packages.size(): " + packages.size());
		for (String id : packages.keySet()) {
			waitedCycles = 0;
			if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.i(TAG, "testSMS packages.get(id): " + packages.get(id));
			if (!packages.get(id).equals(SDSmsConsts.SDSMS_INSTALL_PKG)) {
				if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.i(TAG, "testSMS checking id: " + id + " package: " + packages.get(id));
		    	intent.setClassName(packages.get(id), receivers.get(id));
		    	intent.putExtra(SDSmsManager.TEST_PACKAGE, packages.get(id));
		    	mSDOrderedBroadcastListener = broadcaster.getListener(packages.get(id), receivers.get(id));
		    	broadcaster.sendOrderedBroadcast(packages.get(id), intent, mSDOrderedBroadcastListener);
		    	mMessagingServiceListener.testSent(packages.get(id));
		    	synchronized (this) {
		    		while(!mSDOrderedBroadcastListener.completed() && (waitedCycles <= waitCycles)) {
		    			try {
							wait(500);
							if (SdmmsConsts.DEBUG_VERIFY_APPS) Log.i(TAG, "waiting....");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			waitedCycles++;
		    			if (waitedCycles > waitCycles) {
		    				mMessagingServiceListener.testResponse(packages.get(id), SDSmsManager.TEST_UPDATE_REQUIRED);
		    				//mSDOrderedBroadcastListener.onCompleted(false);
		    			}
		    		}
		    	}
		    	if (intent.hasExtra(SDSmsManager.TEST_RESPONSE)) {
		    		mMessagingServiceListener.testResponse(packages.get(id), intent.getStringExtra(SDSmsManager.TEST_RESPONSE));
			    	if (intent.hasExtra(SDSmsManager.TEST_VERSION)) {
			    		registrationModel.addRegistrationVersion(packages.get(id), intent.getIntExtra(SDSmsManager.TEST_VERSION, -1));
			    	}
		    	} else {
		    		mMessagingServiceListener.testResponse(packages.get(id), SDSmsManager.TEST_UPDATE_REQUIRED);
		    	}
			}
		}
		// sms was not aborted
		return true;
    }

    protected boolean sendSMSDeliver(Intent intent) {
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendSMS callbacks.size(): " + callbacks.size());
		for (String id : packages.keySet()) {
			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "package id: " + id 
					+ " settings.getString(SDScanManager.PRIMARY_MESSAGING_APP, ..):" 
					+ settings.getString(SDScanManager.PRIMARY_MESSAGING_APP, ""));
			if (packages.get(id).equals(settings.getString(SDScanManager.PRIMARY_MESSAGING_APP, ""))) {
				if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendSMS checking id: " + id + " package: " + packages.get(id));
		    	intent.setClassName(packages.get(id), receivers.get(id));
		    	intent.putExtra(SDSmsManager.TEST_PACKAGE, packages.get(id));
		    	mSDOrderedBroadcastListener = broadcaster.getListener(packages.get(id), receivers.get(id));
		    	broadcaster.sendOrderedBroadcast(packages.get(id), intent, mSDOrderedBroadcastListener);
		    	if (callbacks.size() > 0) {
					try {
						if (callbacks.get(id).onReceive(intent)) {
	        				Log.i(TAG, "onHandleIntent: sms aborted by controller group package: " + packages.get(id));
	        				Log.i(TAG, "onHandleIntent: sms aborted by id: " + id + " receiver:" + receivers.get(id));
	        				return true;
						}
					} catch (RemoteException e) {
		    			Log.e(TAG, "sendSMS failed for id: " + id + " - " + packages.get(id));
						e.printStackTrace();
					}
				}
			}
		}
		// sms was not aborted
		return false;
    }

    protected void sendMMS(Intent intent) {
    	if (mType != SDSmsManager.ACCESS_TYPE_MESSAGING) return;
		for (String caller : serviceModel.getControllers(SDSmsManager.ACCESS_TYPE_MESSAGING).packages.keySet()) {
	    	intent.setClassName(packages.get(caller), receivers.get(caller));
	    	if (callbacks.size() > 0) {
				try {
					callbacks.get(caller).onReceive(intent);
				} catch (RemoteException e) {
	    			Log.e(TAG, "sendMMS failed for caller: " + caller + " - " + packages.get(caller));
					e.printStackTrace();
				}
	    	}
		}
    }
}
