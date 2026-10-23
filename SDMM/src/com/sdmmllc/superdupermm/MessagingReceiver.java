package com.sdmmllc.superdupermm;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.Sms.Intents;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * The main messaging receiver class. Note that this is not directly included in
 * AndroidManifest.xml, instead, subclassed versions of this are included which allows
 * them to be enabled/disabled independently as they will have a unique component name.
 */
public class MessagingReceiver extends WakefulBroadcastReceiver {

	public static final String TAG = "MessagingReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();

        // If on KitKat+ and default messaging app then look for new deliver actions actions.
        if (Utils.hasKitKat() && Utils.isDefaultSmsApp(context)) {
            if (Intents.SMS_DELIVER_ACTION.equals(action)) {
            	Log.i(TAG, "SMS_DELIVER_ACTION *************************");
                handleIncomingSms(context, intent);
            } else if (Intents.WAP_PUSH_DELIVER_ACTION.equals(action)) {
                handleIncomingMms(context, intent);
            }
        } else if (!Utils.hasKitKat()){ // Otherwise look for old pre-KitKat actions
            if (Intents.SMS_RECEIVED_ACTION.equals(action)) {
            	Log.i(TAG, "SMS_RECEIVED_ACTION *************************");
                handleIncomingSms(context, intent);
            } else if (Intents.WAP_PUSH_RECEIVED_ACTION.equals(action)) {
                handleIncomingMms(context, intent);
            }
        }
    }

    private void handleIncomingSms(Context context, Intent intent) {
        // TODO: Handle SMS here
        // As an example, we&#39;ll start a wakeful service to handle the SMS
        intent.setAction(MessagingService.ACTION_MY_RECEIVE_SMS);
        intent.setClass(context, MessagingService.class);
        startWakefulService(context, intent);
    }

    private void handleIncomingMms(Context context, Intent intent) {
        // TODO: Handle MMS here
        // As an example, we&#39;ll start a wakeful service to handle the MMS
        intent.setAction(MessagingService.ACTION_MY_RECEIVE_MMS);
        intent.setClass(context, MessagingService.class);
        startWakefulService(context, intent);
    }
}