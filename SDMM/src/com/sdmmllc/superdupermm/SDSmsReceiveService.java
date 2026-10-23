package com.sdmmllc.superdupermm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

/**
 * This service handles the system intent ACTION_RESPOND_VIA_MESSAGE when we are the default SMS
 * app.
 */
public class SDSmsReceiveService extends Service {
    private static final String TAG = "SDSmsReceiveService";
    @Override
    public void onCreate() {
      super.onCreate();
    }

    @Override
    public IBinder onBind(Intent ignore) {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "onBind sending IBinder - ignored intent");
		return null;
    }
    
}