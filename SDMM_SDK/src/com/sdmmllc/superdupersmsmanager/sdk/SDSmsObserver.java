package com.sdmmllc.superdupersmsmanager.sdk;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SDSmsObserver extends ContentObserver {

	private Handler mHandler;
	
	public SDSmsObserver(Handler handler) {
		super(handler);
		mHandler = handler;
	}

	public void onChange(boolean selfChange, Uri uri) {
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("uri", uri.toString());
		msg.setData(bundle);
		mHandler.dispatchMessage(msg);
	}
	
	public interface ContentChanged {
		public abstract void contentChanged(Uri uri);
	}

}
