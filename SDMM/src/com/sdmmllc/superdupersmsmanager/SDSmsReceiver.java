package com.sdmmllc.superdupersmsmanager;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.sdk.utils.SdmmsUtil;

public class SDSmsReceiver extends BroadcastReceiver {

	private String TAG = "SDSmsReceiver";
	
	/* this receiver checks for incoming send text message requests
	 *  
	 * (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().endsWith(SDSmsManager.SDSMS_SEND_TEXT)) {
			if (SdmmsConsts.DEBUG_UTILS) Log.i(TAG, "Sending text message");
			SmsManager sManager = SDSmsManagerApp.getSmsManager();
			String mDest = intent.getStringExtra(SDSmsManager.DEST);
			String mServiceCenter = intent.getStringExtra(SDSmsManager.SVC_CENTER);
			String message = (String) intent.getExtras().get(SDSmsManager.MESSAGE);
			PendingIntent sentIntent = (PendingIntent) intent.getExtras().get(SDSmsManager.SENT_INTENT);
			PendingIntent deliveryIntent = (PendingIntent) intent.getExtras().get(SDSmsManager.DELIVERY_INTENT);
			sManager.sendTextMessage(mDest, mServiceCenter, message, sentIntent, deliveryIntent);
		} else if (intent.getAction().endsWith(SDSmsManager.SDSMS_SEND_MULTIPART_TEXT)) {
			if (SdmmsConsts.DEBUG_UTILS) Log.i(TAG, "Sending multipart message");
			SmsManager sManager = SDSmsManagerApp.getSmsManager();
			String mDest = intent.getStringExtra(SDSmsManager.DEST);
			String mServiceCenter = intent.getStringExtra(SDSmsManager.SVC_CENTER);
			ArrayList<String> messages = (ArrayList<String>) intent.getExtras().get(SDSmsManager.MESSAGES);
			ArrayList<PendingIntent> sentIntents = (ArrayList<PendingIntent>) intent.getExtras().get(SDSmsManager.SENT_INTENTS);
			ArrayList<PendingIntent> deliveryIntents = (ArrayList<PendingIntent>) intent.getExtras().get(SDSmsManager.DELIVERY_INTENTS);
			sManager.sendMultipartTextMessage(mDest, mServiceCenter, messages, sentIntents, deliveryIntents);
		} else if (intent.getAction().endsWith(SDSmsManager.SDSMS_SEND_DATA)) {
			if (SdmmsConsts.DEBUG_UTILS) Log.i(TAG, "Sending data message");
			SmsManager sManager = SDSmsManagerApp.getSmsManager();
			String mDest = intent.getStringExtra(SDSmsManager.DEST);
			String mServiceCenter = intent.getStringExtra(SDSmsManager.SVC_CENTER);
			short destinationPort = intent.getShortExtra(SDSmsManager.PORT, SDSmsManager.DEF_DATA_PORT);
			byte[] data = (byte[]) intent.getByteArrayExtra(SDSmsManager.MSG_DATA);
			PendingIntent sentIntent = (PendingIntent) intent.getExtras().get(SDSmsManager.SENT_INTENT);
			PendingIntent deliveryIntent = (PendingIntent) intent.getExtras().get(SDSmsManager.DELIVERY_INTENT);
			sManager.sendDataMessage(mDest, mServiceCenter, destinationPort, data, sentIntent, deliveryIntent);
		} else if (intent.getAction().endsWith(SdmmsUtil.SMS_ADDMESSAGETOURI)) {
			if (SdmmsConsts.DEBUG_UTILS) Log.i(TAG, ".onReceive broadcast intent received: " + intent.getAction());
			ContentResolver resolver = context.getContentResolver();
			Uri uri = Uri.parse(intent.getStringExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_URI_STRING));
			String address = intent.getStringExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_ADDRESS);
			String body = intent.getStringExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_BODY);
			String subject = intent.getStringExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_SUBJECT);
			Long date = intent.getLongExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_DATE, 0);
			boolean read = intent.getBooleanExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_READ, false);
			boolean deliveryReport = intent.getBooleanExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_DELIVERY_REPORT, false);
			Long threadId = intent.getLongExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_THREADID, 0);
			if (intent.hasExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_SUB_ID)) {
				int sub_id = intent.getIntExtra(SdmmsUtil.SMS_ADDMESSAGETOURI_SUB_ID, 0);
				SdmmsUtil.smsAddMessageToUri(resolver, uri, address, body, subject, 
						date, read, deliveryReport, threadId, sub_id);
			} else {
				SdmmsUtil.smsAddMessageToUri(resolver, uri, address, body, subject, 
						date, read, deliveryReport, threadId);
			}
		} else if (intent.getAction().endsWith(SdmmsUtil.SMS_INBOX_ADDMESSAGE)) {
			if (SdmmsConsts.DEBUG_UTILS) Log.i(TAG, ".onReceive broadcast intent received: " + intent.getAction());
			ContentResolver resolver = context.getContentResolver();
			String address = intent.getStringExtra(SdmmsUtil.SMS_INBOX_ADDMESSAGE_ADDRESS);
			String body = intent.getStringExtra(SdmmsUtil.SMS_INBOX_ADDMESSAGE_BODY);
			String subject = intent.getStringExtra(SdmmsUtil.SMS_INBOX_ADDMESSAGE_SUBJECT);
			Long date = intent.getLongExtra(SdmmsUtil.SMS_INBOX_ADDMESSAGE_DATE, 0);
			if (intent.hasExtra(SdmmsUtil.SMS_INBOX_ADDMESSAGE_READ)) {
				boolean read = intent.getBooleanExtra(SdmmsUtil.SMS_INBOX_ADDMESSAGE_READ, false);
				SdmmsUtil.smsInboxAddMessage(resolver, address, body, subject, date, read);
			} else {
				SdmmsUtil.smsInboxAddMessage(resolver, address, body, subject, date);
			}
			
		}
	}
	
} 