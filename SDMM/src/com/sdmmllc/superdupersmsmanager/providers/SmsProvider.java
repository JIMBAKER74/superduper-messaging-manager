/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sdmmllc.superdupersmsmanager.providers;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.Sms;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.services.SdmmsObserverService;

public class SmsProvider extends ContentProvider {
    private final static String TAG = "SmsProvider";
    private static final Uri NOTIFICATION_URI = Uri.parse("content://sms");
    private static final Uri ICC_URI = Uri.parse("content://sms/icc");

    private ContentProviderClient cpc;

    @Override
    public boolean onCreate() {
    	Intent sintent = new Intent(this.getContext(), SdmmsObserverService.class);
    	this.getContext().startService(sintent);        
        cpc = this.getContext().getContentResolver().acquireContentProviderClient(Uri.parse("content://sms"));
        Log.i(TAG, "Acquired CPC: " + (cpc !=null));
        return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection, String[] selectionArgs, String sort) {
        Uri newUri = convert(url);
        if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "query Uri: " + url.toString());
        Log.i(TAG, "new query Uri: " + newUri.toString());
        try {
			return cpc.query(convert(url), projectionIn, selection, selectionArgs, sort);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().query(convert(url), projectionIn, selection, selectionArgs, sort);
		}
    }

    @Override
    public String getType(Uri url) {
        switch (url.getPathSegments().size()) {
        case 0:
            return VND_ANDROID_DIR_SDSMS;
            case 1:
                try {
                    Integer.parseInt(url.getPathSegments().get(0));
                    return VND_ANDROID_SDSMS;
                } catch (NumberFormatException ex) {
                    return VND_ANDROID_DIR_SDSMS;
                }
            case 2:
                // TODO: What about "threadID"?
                if (url.getPathSegments().get(0).equals("conversations")) {
                    return VND_ANDROID_SDSMSCHAT;
                } else {
                    return VND_ANDROID_SDSMS;
                }
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
        	Uri retval = cpc.insert(convert(uri), values);
        	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "cpc returned Uri: " + retval);
        	if (MmsProvider.uriToSdmmsMatch(retval)) return MmsProvider.uriToSdmms(retval);
        	if (MmsSmsProvider.uriInvertMatch(retval)) return MmsSmsProvider.invert(retval);
        	if (SmsProvider.uriInvertMatch(retval)) return SmsProvider.invert(retval);
        	return retval;
		} catch (Exception e) {
        	Uri retval = this.getContext().getContentResolver().insert(convert(uri), values);
        	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "resolver returned Uri: " + retval);
        	if (MmsProvider.uriToSdmmsMatch(retval)) return MmsProvider.uriToSdmms(retval);
        	if (MmsSmsProvider.uriInvertMatch(retval)) return MmsSmsProvider.invert(retval);
        	if (SmsProvider.uriInvertMatch(retval)) return SmsProvider.invert(retval);
        	return retval;
		}
    }

    @Override
    public int delete(Uri url, String where, String[] whereArgs) {
    	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "delete Uri: " + url.toString());
    	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "new delete Uri: " + convert(url).toString());
        try {
			return cpc.delete(convert(url), where, whereArgs);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().delete(convert(url), where, whereArgs);
		}
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
    	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "update Uri: " + url.toString());
    	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "new update Uri: " + convert(url).toString());
        try {
			return cpc.update(convert(url), values, where, whereArgs);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().update(convert(url), values, where, whereArgs);
		}
    }

    @Override
    public void onLowMemory() {
    	cpc.release();
    }
    
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        String query = Uri.parse(arg).getQuery();
        if (query != null) query = "?" + query;
        else query = "";
        if (method.equals("notify")) notifyChange(Uri.parse("content://" 
    			+ sdsmsURL[smsURLMatcher.match(Uri.parse(arg))]
    			+ query));
    	return null;
    }
    
	static Pattern hash = Pattern.compile("#");
	static Matcher matcher;
    
	static Pattern p = Pattern.compile("(\\d+)");
	static Matcher digitmatcher;
    
    public static boolean uriConvertMatch(Uri uri) {
    	return sURLMatcher.match(uri) >= 0;
    }
    
    public static boolean uriInvertMatch(Uri uri) {
    	return smsURLMatcher.match(uri) >= 0;
    }
    
	static public Uri convert(Uri uri) {
        int match = sURLMatcher.match(uri);
        String query = uri.getQuery();
        if (query != null) query = "?" + query;
        else query = "";
        String newUri = smsURL[match];
        matcher = hash.matcher(newUri);
        digitmatcher = p.matcher(uri.toString());
        if (matcher.find() && digitmatcher.find()) {
        	newUri = matcher.replaceFirst(digitmatcher.group());
            matcher = hash.matcher(newUri);
            if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
        }
    	if (SdmmsConsts.DEBUG_SMS_PROVIDER) Log.i(TAG, "convert newUri: " + newUri + " match: " + smsURL[match]);
        return Uri.parse("content://" + newUri + query);
    }
    
	static public Uri invert(Uri uri) {
        int match = smsURLMatcher.match(uri);
        String query = uri.getQuery();
        if (query != null) query = "?" + query;
        else query = "";
        String newUri = sdsmsURL[match];
        matcher = hash.matcher(newUri);
        digitmatcher = p.matcher(uri.toString());
        if (matcher.find() && digitmatcher.find()) {
        	newUri = matcher.replaceFirst(digitmatcher.group());
            matcher = hash.matcher(newUri);
            if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
        }
        return Uri.parse("content://" + newUri + query);
    }

    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        if (!uri.toString().equals(SdmmsConsts.SDCONTENT_URI)) cr.notifyChange(uri, null);
        cr.notifyChange(SdmmsConsts.SDCONTENT_URI, null);
        cr.notifyChange(Uri.parse("content://sdmms-sdsms/conversations/"), null);
    }

    private final static String[] ICC_COLUMNS = new String[] {
        // N.B.: These columns must appear in the same order as the
        // calls to add appear in convertIccToSms.
        "service_center_address",       // getServiceCenterAddress
        "address",                      // getDisplayOriginatingAddress
        "message_class",                // getMessageClass
        "body",                         // getDisplayMessageBody
        "date",                         // getTimestampMillis
        "status",                       // getStatusOnIcc
        "index_on_icc",                 // getIndexOnIcc
        "is_status_report",             // isStatusReportMessage
        "transport_type",               // Always "sms".
        "type",                         // Always MESSAGE_TYPE_ALL.
        "locked",                       // Always 0 (false).
        "error_code",                   // Always 0
        "_id"
    };

    private final static String VND_ANDROID_SMS = "vnd.android.cursor.item/sms";
    private final static String VND_ANDROID_SMSCHAT =
            "vnd.android.cursor.item/sms-chat";
    private final static String VND_ANDROID_DIR_SMS =
            "vnd.android.cursor.dir/sms";

    private final static String VND_ANDROID_SDSMS = "vnd.android.cursor.item/sdsms";
    private final static String VND_ANDROID_SDSMSCHAT =
            "vnd.android.cursor.item/sdsms-chat";
    private final static String VND_ANDROID_DIR_SDSMS =
            "vnd.android.cursor.dir/sdsms";

    private static final HashMap<String, String> sConversationProjectionMap =
            new HashMap<String, String>();
    private static final String[] sIDProjection = new String[] { "_id" };

    private static final int SDSMS_ALL = 0;
    private static final int SDSMS_ALL_ID = 1;
    private static final int SDSMS_INBOX = 2;
    private static final int SDSMS_INBOX_ID = 3;
    private static final int SDSMS_SENT = 4;
    private static final int SDSMS_SENT_ID = 5;
    private static final int SDSMS_DRAFT = 6;
    private static final int SDSMS_DRAFT_ID = 7;
    private static final int SDSMS_OUTBOX = 8;
    private static final int SDSMS_OUTBOX_ID = 9;
    private static final int SDSMS_CONVERSATIONS = 10;
    private static final int SDSMS_CONVERSATIONS_ID = 11;
    private static final int SDSMS_RAW_MESSAGE = 15;
    private static final int SDSMS_ATTACHMENT = 16;
    private static final int SDSMS_ATTACHMENT_ID = 17;
    private static final int SDSMS_NEW_THREAD_ID = 18;
    private static final int SDSMS_QUERY_THREAD_ID = 19;
    private static final int SDSMS_STATUS_ID = 20;
    private static final int SDSMS_STATUS_PENDING = 21;
    private static final int SDSMS_ALL_ICC = 22;
    private static final int SDSMS_ICC = 23;
    private static final int SDSMS_FAILED = 24;
    private static final int SDSMS_FAILED_ID = 25;
    private static final int SDSMS_QUEUED = 26;
    private static final int SDSMS_UNDELIVERED = 27;
    private static final int SDSIM_ALL_ICC = 28;
    private static final int SDSIM_ICC = 29;

    private static final UriMatcher sURLMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("sdsms", null, SDSMS_ALL);
        sURLMatcher.addURI("sdsms", "#", SDSMS_ALL_ID);
        sURLMatcher.addURI("sdsms", "inbox", SDSMS_INBOX);
        sURLMatcher.addURI("sdsms", "inbox/#", SDSMS_INBOX_ID);
        sURLMatcher.addURI("sdsms", "sent", SDSMS_SENT);
        sURLMatcher.addURI("sdsms", "sent/#", SDSMS_SENT_ID);
        sURLMatcher.addURI("sdsms", "draft", SDSMS_DRAFT);
        sURLMatcher.addURI("sdsms", "draft/#", SDSMS_DRAFT_ID);
        sURLMatcher.addURI("sdsms", "outbox", SDSMS_OUTBOX);
        sURLMatcher.addURI("sdsms", "outbox/#", SDSMS_OUTBOX_ID);
        sURLMatcher.addURI("sdsms", "undelivered", SDSMS_UNDELIVERED);
        sURLMatcher.addURI("sdsms", "failed", SDSMS_FAILED);
        sURLMatcher.addURI("sdsms", "failed/#", SDSMS_FAILED_ID);
        sURLMatcher.addURI("sdsms", "queued", SDSMS_QUEUED);
        sURLMatcher.addURI("sdsms", "conversations", SDSMS_CONVERSATIONS);
        sURLMatcher.addURI("sdsms", "conversations/*", SDSMS_CONVERSATIONS_ID);
        sURLMatcher.addURI("sdsms", "raw", SDSMS_RAW_MESSAGE);
        sURLMatcher.addURI("sdsms", "attachments", SDSMS_ATTACHMENT);
        sURLMatcher.addURI("sdsms", "attachments/#", SDSMS_ATTACHMENT_ID);
        sURLMatcher.addURI("sdsms", "threadID", SDSMS_NEW_THREAD_ID);
        sURLMatcher.addURI("sdsms", "threadID/*", SDSMS_QUERY_THREAD_ID);
        sURLMatcher.addURI("sdsms", "status/#", SDSMS_STATUS_ID);
        sURLMatcher.addURI("sdsms", "sr_pending", SDSMS_STATUS_PENDING);
        sURLMatcher.addURI("sdsms", "icc", SDSMS_ALL_ICC);
        sURLMatcher.addURI("sdsms", "icc/#", SDSMS_ICC);
        //we keep these for not breaking old applications
        sURLMatcher.addURI("sdsms", "sim", SDSIM_ALL_ICC);
        sURLMatcher.addURI("sdsms", "sim/#", SDSIM_ICC);

        sConversationProjectionMap.put(Sms.Conversations.SNIPPET,
            "sms.body AS snippet");
        sConversationProjectionMap.put(Sms.Conversations.THREAD_ID,
            "sms.thread_id AS thread_id");
        sConversationProjectionMap.put(Sms.Conversations.MESSAGE_COUNT,
            "groups.msg_count AS msg_count");
        sConversationProjectionMap.put("delta", null);    
    }
    private static final UriMatcher smsURLMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        smsURLMatcher.addURI("sms", null, SDSMS_ALL);
        smsURLMatcher.addURI("sms", "#", SDSMS_ALL_ID);
        smsURLMatcher.addURI("sms", "inbox", SDSMS_INBOX);
        smsURLMatcher.addURI("sms", "inbox/#", SDSMS_INBOX_ID);
        smsURLMatcher.addURI("sms", "sent", SDSMS_SENT);
        smsURLMatcher.addURI("sms", "sent/#", SDSMS_SENT_ID);
        smsURLMatcher.addURI("sms", "draft", SDSMS_DRAFT);
        smsURLMatcher.addURI("sms", "draft/#", SDSMS_DRAFT_ID);
        smsURLMatcher.addURI("sms", "outbox", SDSMS_OUTBOX);
        smsURLMatcher.addURI("sms", "outbox/#", SDSMS_OUTBOX_ID);
        smsURLMatcher.addURI("sms", "undelivered", SDSMS_UNDELIVERED);
        smsURLMatcher.addURI("sms", "failed", SDSMS_FAILED);
        smsURLMatcher.addURI("sms", "failed/#", SDSMS_FAILED_ID);
        smsURLMatcher.addURI("sms", "queued", SDSMS_QUEUED);
        smsURLMatcher.addURI("sms", "conversations", SDSMS_CONVERSATIONS);
        smsURLMatcher.addURI("sms", "conversations/*", SDSMS_CONVERSATIONS_ID);
        smsURLMatcher.addURI("sms", "raw", SDSMS_RAW_MESSAGE);
        smsURLMatcher.addURI("sms", "attachments", SDSMS_ATTACHMENT);
        smsURLMatcher.addURI("sms", "attachments/#", SDSMS_ATTACHMENT_ID);
        smsURLMatcher.addURI("sms", "threadID", SDSMS_NEW_THREAD_ID);
        smsURLMatcher.addURI("sms", "threadID/*", SDSMS_QUERY_THREAD_ID);
        smsURLMatcher.addURI("sms", "status/#", SDSMS_STATUS_ID);
        smsURLMatcher.addURI("sms", "sr_pending", SDSMS_STATUS_PENDING);
        smsURLMatcher.addURI("sms", "icc", SDSMS_ALL_ICC);
        smsURLMatcher.addURI("sms", "icc/#", SDSMS_ICC);
        //we keep these for not breaking old applications
        smsURLMatcher.addURI("sms", "sim", SDSIM_ALL_ICC);
        smsURLMatcher.addURI("sms", "sim/#", SDSIM_ICC);

        sConversationProjectionMap.put(Sms.Conversations.SNIPPET,
            "sms.body AS snippet");
        sConversationProjectionMap.put(Sms.Conversations.THREAD_ID,
            "sms.thread_id AS thread_id");
        sConversationProjectionMap.put(Sms.Conversations.MESSAGE_COUNT,
            "groups.msg_count AS msg_count");
        sConversationProjectionMap.put("delta", null);    
    }
        
    private static final int SMS_ALL = 0;
    private static final int SMS_ALL_ID = 1;
    private static final int SMS_INBOX = 2;
    private static final int SMS_INBOX_ID = 3;
    private static final int SMS_SENT = 4;
    private static final int SMS_SENT_ID = 5;
    private static final int SMS_DRAFT = 6;
    private static final int SMS_DRAFT_ID = 7;
    private static final int SMS_OUTBOX = 8;
    private static final int SMS_OUTBOX_ID = 9;
    private static final int SMS_CONVERSATIONS = 10;
    private static final int SMS_CONVERSATIONS_ID = 11;
    // missing 12
    // missing 13
    // missing 14
    private static final int SMS_RAW_MESSAGE = 15;
    private static final int SMS_ATTACHMENT = 16;
    private static final int SMS_ATTACHMENT_ID = 17;
    private static final int SMS_NEW_THREAD_ID = 18;
    private static final int SMS_QUERY_THREAD_ID = 19;
    private static final int SMS_STATUS_ID = 20;
    private static final int SMS_STATUS_PENDING = 21;
    private static final int SMS_ALL_ICC = 22;
    private static final int SMS_ICC = 23;
    private static final int SMS_FAILED = 24;
    private static final int SMS_FAILED_ID = 25;
    private static final int SMS_QUEUED = 26;
    private static final int SMS_UNDELIVERED = 27;
    private static final int SMS_SIM_ALL_ICC = 28;
    private static final int SMS_SIM_ICC = 29;

    private static String smsURL[] = {
        "sms",
        "sms/#",
        "sms/inbox",
        "sms/inbox/#",
        "sms/sent",
        "sms/sent/#",
        "sms/draft",
        "sms/draft/#",
        "sms/outbox",
        "sms/outbox/#",
        "sms/conversations",
        "sms/conversations/#",
        "sms",
        "sms",
        "sms",
        "sms/raw",
        "sms/attachments",
        "sms/attachments/#",
        "sms/threadID",
        "sms/threadID/*",
        "sms/status/#",
        "sms/sr_pending",
        "sms/icc",
        "sms/icc/#",
        "sms/failed",
        "sms/failed/#",
        "sms/queued",
        "sms/undelivered",
        //we keep these for not breaking old applications
        "sms/sim",
        "sms/sim/#",
    };

    private static String sdsmsURL[] = {
        "sdsms",
        "sdsms/#",
        "sdsms/inbox",
        "sdsms/inbox/#",
        "sdsms/sent",
        "sdsms/sent/#",
        "sdsms/draft",
        "sdsms/draft/#",
        "sdsms/outbox",
        "sdsms/outbox/#",
        "sdsms/conversations",
        "sdsms/conversations/#",
        "sdsms",
        "sdsms",
        "sdsms",
        "sdsms/raw",
        "sdsms/attachments",
        "sdsms/attachments/#",
        "sdsms/threadID",
        "sdsms/threadID/*",
        "sdsms/status/#",
        "sdsms/sr_pending",
        "sdsms/icc",
        "sdsms/icc/#",
        "sdsms/failed",
        "sdsms/failed/#",
        "sdsms/queued",
        "sdsms/undelivered",
        //we keep these for not breaking old applications
        "sdsms/sim",
        "sdsms/sim/#",
    };
}
