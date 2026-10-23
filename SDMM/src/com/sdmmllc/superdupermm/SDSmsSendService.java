package com.sdmmllc.superdupermm;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.util.Log;

import com.sdmmllc.superdupermm.ISDSmsSendService;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

/**
 * This service handles the system intent ACTION_RESPOND_VIA_MESSAGE when we are the default SMS
 * app.
 */
public class SDSmsSendService extends Service {
    private static final String TAG = "SDSmsSendService";
    
    private final ISDSmsSendService.Stub mBinder = new ISDSmsSendService.Stub() {
        @Override
        public void sendTextMessage(
    			String destinationAddress, String scAddress, String text,
    			PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
        	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendTextMessage");
			SDSmsSendService.this.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
        }

		@Override
		public void sendMultipartTextMessage(String destinationAddress, String scAddress,
				List<String> parts, List<PendingIntent> sentIntents,
				List<PendingIntent> deliveryIntents) throws RemoteException {
			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendMultipartTextMessage");
			SDSmsSendService.this.sendMultipartTextMessage(destinationAddress, scAddress, new ArrayList<String>(parts), 
					new ArrayList<PendingIntent>(sentIntents), new ArrayList<PendingIntent>(deliveryIntents));
		}

		@Override
		public String testConnection(String str) throws RemoteException {
	    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "testConnection: " + str);
			return SDSmsSendService.this.testConn(str);
		}
    };

    @Override
    public void onCreate() {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "onCreate");
    	super.onCreate();
    }

    @Override
    public IBinder onBind(Intent ignore) {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "onBind sending IBinder");
		return mBinder;
    }
    
    private void sendTextMessage(
			String destinationAddress, String scAddress, String text,
			PendingIntent sentIntent, PendingIntent deliveryIntent) throws RemoteException {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendTextMessage");
		SmsManager sManager = SDSmsManagerApp.getSmsManager();
		sManager.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
    }

    private void sendMultipartTextMessage(String destinationAddress, String scAddress,
			List<String> parts, List<PendingIntent> sentIntents,
			List<PendingIntent> deliveryIntents) throws RemoteException {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "sendMultipartTextMessage");
		SmsManager sManager = SDSmsManagerApp.getSmsManager();
		sManager.sendMultipartTextMessage(destinationAddress, scAddress, new ArrayList<String>(parts), 
				new ArrayList<PendingIntent>(sentIntents), new ArrayList<PendingIntent>(deliveryIntents));
	}

    private String testConn(String str) {
    	Log.i(TAG, "string sent: " + str);
    	return TAG + ":textConn success";
    }
}