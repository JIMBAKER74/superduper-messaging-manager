package com.messagingappsllc.superdupermms;

import android.content.Context;
import android.content.Intent;
import android.provider.Telephony.Sms.Intents;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * The main messaging receiver class. Note that this is not directly included in
 * AndroidManifest.xml, instead, subclassed versions of this are included which allows
 * them to be enabled/disabled independently as they will have a unique component name.
 */
public class MessagingReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
    }
}