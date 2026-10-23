package com.sdmmllc.superdupersmsmanager.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupermm.ISDSmsReceiveService;
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;

public class SDSmsReceiveCallbackConnection implements ServiceConnection {
	
	private ISDSmsReceiveService mService;
	private String TAG = "SDSmsReceiveCallbackConnection";
	private String smsReceiver, packageName;
	private Context mContext;
	private SDSmsReceiveCallback mCallback;

	public SDSmsReceiveCallbackConnection(Context context, String receiver, SDSmsReceiveCallback callback) {
		mContext = context;
		packageName = mContext.getPackageName();
		smsReceiver = receiver;
		mCallback = callback;
	}
	
	public void onReceive(Intent intent) throws RemoteException {
		Log.i(TAG, "onReceive");
		mCallback.onReceive(intent);
	}
	
	public String testConnection(String text) throws RemoteException {
		Log.i(TAG, "testing connection:" + text);
		return mService.testConnection(text);
	}
	
	public boolean registerApp(Intent intent) throws RemoteException {
		Log.i(TAG, "registerApp (mCallback == null): " + (mCallback == null));
	    return mService.registerApp(intent);
	}
	
	public String registerReceiver(String id, int type, Intent intent) throws RemoteException {
		Log.i(TAG, "registerReceiver (mCallback == null): " + (mCallback == null));
	    return mService.registerReceiver(id, type, intent, mCallback);
	}
	
	public SDSmsReceiveCallback getCallBack() {
		if (mCallback == null) { 
			mCallback = new SDSmsReceiveCallback() {
				@Override
				public boolean onReceive(Intent intent) throws RemoteException {
					if (!packageName.endsWith(SDSmsConsts.SDSMS_INSTALL_PKG))
						Log.w(TAG, "No message sent - you must implement a custom callback to receive SDSmsManager messages");
					return false;
				}
			};
		}
		Log.i(TAG, "getCallBack");
		return mCallback;
	}
	
	public String setCallBack(String id, int type, Intent intent, SDSmsReceiveCallback callback) throws RemoteException {
		Log.i(TAG, "setCallBack");
		if (callback == null) { 
			if (mCallback == null)
			mCallback = new SDSmsReceiveCallback() {
				@Override
				public boolean onReceive(Intent intent) throws RemoteException {
					if (!packageName.endsWith(SDSmsConsts.SDSMS_INSTALL_PKG))
						Log.w(TAG, "No message sent - you must implement a custom callback to receive SDSmsManager messages");
					return false;
				}
			};
		} else mCallback = callback;
	    return mService.registerReceiver(id, type, intent, mCallback);
	}
	
	public boolean connected() {
		return mService != null;
	}
	
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
	    mService = ISDSmsReceiveService.Stub.asInterface((IBinder)service);
		Log.i(TAG, "onServiceConnected mService== null: " + (mService == null));
		Log.i(TAG, "onServiceConnected name: " + name.getClassName());
		Log.i(TAG, "onServiceConnected pkgName: " + name.getPackageName());
		try {
			mService.testConnection(TAG + ".onServiceConnected testing service connection");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "Service remote exception");
			e.printStackTrace();
		}
	}
	
	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
	}
}
