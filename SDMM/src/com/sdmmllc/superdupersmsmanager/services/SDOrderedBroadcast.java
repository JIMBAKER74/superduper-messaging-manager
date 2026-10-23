package com.sdmmllc.superdupersmsmanager.services;

import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class SDOrderedBroadcast {
	private static final String TAG = "SDOrderedBroadcast";

	private Context mContext;
	private Hashtable<String, BroadcastReceiver> receivers = new Hashtable<String, BroadcastReceiver>();
	
	public SDOrderedBroadcast (Context context) {
		mContext = context;
	}

    private void registerReceiver(final String pkgName, String action, 
    		IntentFilter filter, final SDOrderedBroadcastListener listener) {
    	receivers.put(pkgName, new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	listener.onCompleted(true);
	            Log.i(TAG, "orderedBroadcast completed and aborted");
	        	mContext.unregisterReceiver(receivers.get(pkgName));
	        }
	    });
	    
	    mContext.registerReceiver(receivers.get(pkgName), filter);
    }
    
    public SDOrderedBroadcastListener getListener(final String pkgName, final String receiverName) {
    	return new SDOrderedBroadcastListener() {
        	boolean aborted = false;
        	boolean completed = false;
        	String name = receiverName;
        	String packageName = pkgName;
        	
        	@Override
        	public String receiverName() {
        		return name;
        	}
        	@Override
			public void onCompleted(boolean abort) {
        		Log.i(TAG, "listener completed; aborted: " + abort + " on receiver: " + receiverName());
				aborted = abort;
				completed = true;
			}
    		@Override
    		public boolean completed() {
    			return completed;
    		}
    		@Override 
    		public boolean aborted() {
        		Log.i(TAG, "listener checking aborted: " + aborted + " on receiver: " + receiverName());
    			return aborted;
    		}
    	};
    }

    public void sendOrderedBroadcast(final String pkgName, Intent intent, final SDOrderedBroadcastListener listener) {
    	if (listener == null) {
    		Log.w(TAG, "No broadcast listener was registered");
    		return;
    	}
    	String action = intent.getAction();
    	IntentFilter filter = new IntentFilter(action);
    	filter.setPriority(1000);
    	registerReceiver(pkgName, action, filter, listener);
    	BroadcastReceiver finalReceiver = new BroadcastReceiver() {
	        @SuppressLint("NewApi")
			@Override
	        public void onReceive(Context context, Intent intent) {
	            Log.i(TAG, "orderedBroadcast completed without aborting on receiver: " + listener.receiverName());
	            Log.i(TAG, "orderedBroadcast getResultCode(): " + getResultCode());
	            Log.i(TAG, "orderedBroadcast getAbortBroadcast(): " + getAbortBroadcast());
	        	listener.onCompleted(intent.getBooleanExtra(SDSmsConsts.SDSMS_ABORT, false) ||
	        			getResultCode() > Activity.RESULT_OK);
	        	if (intent.hasExtra(SDSmsManager.TEST_PACKAGE))
	        		Log.i(TAG, "orderedBroadcast finalReceiver.onReceive() unregistering for package: " + 
	        				intent.getStringExtra(SDSmsManager.TEST_PACKAGE));
        		Log.i(TAG, "orderedBroadcast finalReceiver.onReceive() package: " + pkgName);
	        	mContext.unregisterReceiver(receivers.get(pkgName));
	        }
	    };
	    mContext.sendOrderedBroadcast(intent, null, finalReceiver, null, Activity.RESULT_OK, SDSmsConsts.SDSMS_DUPLICATE, null);
    }
    
    public interface SDOrderedBroadcastListener {
    	String receiverName();
    	void onCompleted(boolean abort);
    	boolean completed();
    	boolean aborted();
    }
}