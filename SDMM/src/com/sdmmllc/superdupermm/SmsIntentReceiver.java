package com.sdmmllc.superdupermm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsIntentReceiver extends BroadcastReceiver {

	String TAG = "SmsIntentReceiver";
    public static final int MSGNOTIFICATION = 110;

	@Override
	public void onReceive(Context context, Intent intent) {
		// start messaging service on Boot, if subscriber

		if((!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))) {
			return;
		}
	}
}
