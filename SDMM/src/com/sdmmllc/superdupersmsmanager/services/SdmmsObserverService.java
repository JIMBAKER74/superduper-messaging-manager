package com.sdmmllc.superdupersmsmanager.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class SdmmsObserverService extends Service {
	private WakeLock mWakeLock;
	private String TAG = "SdmmsObserver";
	private Context mContext;
	private SdmmsCO sdmmsCO;
	private SdsmsCO sdsmsCO;
	private SdsmsmmsCO sdsmsmmsCO;
	
	private class SdmmsCO extends ContentObserver {     

		private Context mContext; 
		
		public SdmmsCO(Handler handler) {
			super(handler);         
		}

		@Override
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}     

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			mContext.getContentResolver().call(Uri.parse("content://sdmms/"), "notify", uri.toString(), null);
		}     
		
		public void setContext(Context context) {
			mContext = context;
		}
	}
	
	private class SdsmsCO extends ContentObserver {     

		private Context mContext; 
		
		public SdsmsCO(Handler handler) {
			super(handler);         
		}

		@Override
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}     

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			mContext.getContentResolver().call(Uri.parse("content://sdsms/"), "notify", uri.toString(), null);
		}     

		public void setContext(Context context) {
			mContext = context;
		}
	}
	
	private class SdsmsmmsCO extends ContentObserver {     

		private Context mContext; 
		
		public SdsmsmmsCO(Handler handler) {
			super(handler);         
		}

		@Override
		public void onChange(boolean selfChange) {
			this.onChange(selfChange, null);
		}     

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			mContext.getContentResolver().call(Uri.parse("content://sdmms-sdsms/"), "notify", uri.toString(), null);
		}     

		public void setContext(Context context) {
			mContext = context;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = this;
		sdmmsCO = new SdmmsCO(new Handler());
		sdmmsCO.setContext(this.getApplicationContext());
		getContentResolver().
		      registerContentObserver(
		            SdmmsConsts.MMS_URI, 
		            true, 
		            sdmmsCO);
		sdsmsCO = new SdsmsCO(new Handler());
		sdsmsCO.setContext(this.getApplicationContext());
		getContentResolver().
		      registerContentObserver(
		            SdmmsConsts.SMS_URI, 
		            true, 
		            sdsmsCO);
		sdsmsmmsCO = new SdsmsmmsCO(new Handler());
		sdsmsmmsCO.setContext(this.getApplicationContext());
		getContentResolver().
		      registerContentObserver(
		            SdmmsConsts.CONTENT_URI, 
		            true, 
		            sdsmsmmsCO);
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		//TODO for communication return IBinder implementation
		return null;
	}
	
	@Override
	public void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
        getContentResolver().unregisterContentObserver(sdmmsCO);
        getContentResolver().unregisterContentObserver(sdsmsCO);
        getContentResolver().unregisterContentObserver(sdsmsmmsCO);
    }
}
