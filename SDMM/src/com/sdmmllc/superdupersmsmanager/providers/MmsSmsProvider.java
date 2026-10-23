/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.services.SdmmsObserverService;

public class MmsSmsProvider extends ContentProvider {
    private static final String TAG = "MmsSmsProvider";
    private static final boolean DEBUG = false;

    private ContentProviderClient cpc;

    @Override
    public boolean onCreate() {
    	Intent sintent = new Intent(this.getContext(), SdmmsObserverService.class);
    	this.getContext().startService(sintent);        
        cpc = this.getContext().getContentResolver().acquireContentProviderClient(Uri.parse("content://mms-sms"));
        if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "Acquired CPC: " + (cpc !=null));
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "query Uri: " + uri.toString());
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "new query Uri: " + convert(uri).toString());
        try {
			return cpc.query(convert(uri), projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
	        return this.getContext()
	        		.getContentResolver().query(convert(uri), projection, selection, selectionArgs, sortOrder);
		}
    }

    @Override
    public String getType(Uri uri) {
        return VND_ANDROID_DIR_MMS_SMS;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "delete Uri: " + uri.toString());
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "new delete Uri: " + convert(uri).toString());
        try {
			return cpc.delete(convert(uri), selection, selectionArgs);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().delete(convert(uri), selection, selectionArgs);
		}
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "insert Uri: " + uri.toString());
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "new insert Uri: " + convert(uri).toString());
        try {
        	Uri retval = cpc.insert(convert(uri), values);
        	if (MmsProvider.uriToSdmmsMatch(retval)) return MmsProvider.uriToSdmms(retval);
        	if (MmsSmsProvider.uriInvertMatch(retval)) return MmsSmsProvider.invert(retval);
        	if (SmsProvider.uriInvertMatch(retval)) return SmsProvider.invert(retval);
        	return retval;
		} catch (Exception e) {
        	Uri retval = this.getContext().getContentResolver().insert(convert(uri), values);
        	if (MmsProvider.uriToSdmmsMatch(retval)) return MmsProvider.uriToSdmms(retval);
        	if (MmsSmsProvider.uriInvertMatch(retval)) return MmsSmsProvider.invert(retval);
        	if (SmsProvider.uriInvertMatch(retval)) return SmsProvider.invert(retval);
        	return retval;
		}
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "update Uri: " + uri.toString());
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "new update Uri: " + convert(uri).toString());
        try {
			return cpc.update(convert(uri), values, selection, selectionArgs);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().update(convert(uri), values, selection, selectionArgs);
		}
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, " observer.call - arg: " + arg);
    	if (method.equals("notify")) {
    		int match = URI_MATCHER.match(Uri.parse(arg));
    		if (match < 0) notifyChange(SdmmsConsts.SDCONTENT_URI);
    		else notifyChange(Uri.parse("content://" 
        					+ sdmmsURL[match] 
        	    			+ Uri.parse(arg).getQuery()));
    	}
    	return null;
    }

    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        if (!uri.equals(SdmmsConsts.SDCONTENT_URI)) cr.notifyChange(uri, null);
        cr.notifyChange(SdmmsConsts.SDCONTENT_URI, null);
        cr.notifyChange(Uri.parse("content://sdmms-sdsms/conversations/"), null);
    }

    @Override
    public void onLowMemory() {
    	cpc.release();
    }
    
    static Pattern hash = Pattern.compile("#");
    static Matcher matcher;
    
    static Pattern p = Pattern.compile("(\\d+)");
    static Matcher digitmatcher;
    
    public static boolean uriConvertMatch(Uri uri) {
    	return SDURI_MATCHER.match(uri) >= 0;
    }
    
    public static boolean uriInvertMatch(Uri uri) {
    	return URI_MATCHER.match(uri) >= 0;
    }
    
    static public Uri convert(Uri uri) {
        int match = SDURI_MATCHER.match(uri);
        String query = uri.getQuery();
        if (query != null) query = "?" + query;
        else query = "";
        String newUri = mmsURL[match];
        matcher = hash.matcher(newUri);
        digitmatcher = p.matcher(uri.toString());
        if (matcher.find() && digitmatcher.find()) {
        	newUri = matcher.replaceFirst(digitmatcher.group());
            matcher = hash.matcher(newUri);
            if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
        }
    	if (SdmmsConsts.DEBUG_MMS_SMS_PROVIDER) Log.i(TAG, "convert newUri: " + newUri + " match: " + mmsURL[match]);
        return Uri.parse("content://" + newUri + query);
    }
    
    static public Uri invert(Uri uri) {
        int match = URI_MATCHER.match(uri);
        String query = uri.getQuery();
        if (query != null) query = "?" + query;
        else query = "";
        String newUri = sdmmsURL[match];
        matcher = hash.matcher(newUri);
        digitmatcher = p.matcher(uri.toString());
        if (matcher.find() && digitmatcher.find()) {
        	newUri = matcher.replaceFirst(digitmatcher.group());
            matcher = hash.matcher(newUri);
            if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
        }
        return Uri.parse("content://" + newUri + query);
    }

    // These are the columns that appear in both the MMS ("pdu") and
    // SMS ("sms") message tables.
    private static final String[] MMS_SMS_COLUMNS =
            { BaseColumns._ID, Mms.DATE, Mms.DATE_SENT, Mms.READ, Mms.THREAD_ID, Mms.LOCKED };

    // These are the columns that appear only in the MMS message
    // table.
    private static final String[] MMS_ONLY_COLUMNS = {
        Mms.CONTENT_CLASS, 
        Mms.CONTENT_LOCATION, 
        Mms.CONTENT_TYPE,
        Mms.DELIVERY_REPORT, 
        Mms.EXPIRY, 
        Mms.MESSAGE_CLASS, 
        Mms.MESSAGE_ID,
        Mms.MESSAGE_SIZE, 
        Mms.MESSAGE_TYPE, 
        Mms.MESSAGE_BOX, 
        Mms.PRIORITY,
        Mms.READ_STATUS, 
        Mms.RESPONSE_STATUS, 
        Mms.RESPONSE_TEXT,
        Mms.RETRIEVE_STATUS, 
        Mms.RETRIEVE_TEXT_CHARSET, 
        Mms.REPORT_ALLOWED,
        Mms.READ_REPORT, 
        Mms.STATUS, 
        Mms.SUBJECT, 
        Mms.SUBJECT_CHARSET,
        Mms.TRANSACTION_ID, 
        Mms.MMS_VERSION, 
        Mms.TEXT_ONLY };

    // These are the columns that appear only in the SMS message
    // table.
    private static final String[] SMS_ONLY_COLUMNS = { 
    	"address", 
    	"body", 
    	"person", 
    	"reply_path_present",
        "service_center", 
        "status", 
        "subject", 
        "type", 
        "error_code" 
        };


    private static final int URI_CONVERSATIONS                     = 0;
    private static final int URI_CONVERSATIONS_MESSAGES            = 1;
    private static final int URI_CONVERSATIONS_RECIPIENTS          = 2;
    private static final int URI_MESSAGES_BY_PHONE                 = 3;
    private static final int URI_THREAD_ID                         = 4;
    private static final int URI_CANONICAL_ADDRESS                 = 5;
    private static final int URI_PENDING_MSG                       = 6;
    private static final int URI_COMPLETE_CONVERSATIONS            = 7;
    private static final int URI_UNDELIVERED_MSG                   = 8;
    private static final int URI_CONVERSATIONS_SUBJECT             = 9;
    private static final int URI_NOTIFICATIONS                     = 10;
    private static final int URI_OBSOLETE_THREADS                  = 11;
    private static final int URI_DRAFT                             = 12;
    private static final int URI_CANONICAL_ADDRESSES               = 13;
    private static final int URI_SEARCH                            = 14;
    private static final int URI_SEARCH_SUGGEST                    = 15;
    private static final int URI_FIRST_LOCKED_MESSAGE_ALL          = 16;
    private static final int URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID = 17;
    private static final int URI_MESSAGE_ID_TO_THREAD              = 18;

    private static final String VND_ANDROID_DIR_MMS_SMS =
            "vnd.android-dir/mms-sms";

    private static final String SDAUTHORITY = "sdmms-sdsms";

    private static final UriMatcher SDURI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        SDURI_MATCHER.addURI(SDAUTHORITY, "conversations", URI_CONVERSATIONS);
        SDURI_MATCHER.addURI(SDAUTHORITY, "complete-conversations", URI_COMPLETE_CONVERSATIONS);
        SDURI_MATCHER.addURI(SDAUTHORITY, "conversations/#", URI_CONVERSATIONS_MESSAGES);
        SDURI_MATCHER.addURI(SDAUTHORITY, "conversations/#/recipients",URI_CONVERSATIONS_RECIPIENTS);
        SDURI_MATCHER.addURI(SDAUTHORITY, "conversations/#/subject",URI_CONVERSATIONS_SUBJECT);
        SDURI_MATCHER.addURI(SDAUTHORITY, "conversations/obsolete", URI_OBSOLETE_THREADS);
        SDURI_MATCHER.addURI(SDAUTHORITY, "messages/byphone/*",URI_MESSAGES_BY_PHONE);
        SDURI_MATCHER.addURI(SDAUTHORITY, "threadID", URI_THREAD_ID);
        SDURI_MATCHER.addURI(SDAUTHORITY, "canonical-address/#", URI_CANONICAL_ADDRESS);
        SDURI_MATCHER.addURI(SDAUTHORITY, "canonical-addresses", URI_CANONICAL_ADDRESSES);
        SDURI_MATCHER.addURI(SDAUTHORITY, "search", URI_SEARCH);
        SDURI_MATCHER.addURI(SDAUTHORITY, "searchSuggest", URI_SEARCH_SUGGEST);
        SDURI_MATCHER.addURI(SDAUTHORITY, "pending", URI_PENDING_MSG);
        SDURI_MATCHER.addURI(SDAUTHORITY, "undelivered", URI_UNDELIVERED_MSG);
        SDURI_MATCHER.addURI(SDAUTHORITY, "notifications", URI_NOTIFICATIONS);
        SDURI_MATCHER.addURI(SDAUTHORITY, "draft", URI_DRAFT);
        SDURI_MATCHER.addURI(SDAUTHORITY, "locked", URI_FIRST_LOCKED_MESSAGE_ALL);
        SDURI_MATCHER.addURI(SDAUTHORITY, "locked/#", URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID);
        SDURI_MATCHER.addURI(SDAUTHORITY, "messageIdToThread", URI_MESSAGE_ID_TO_THREAD);
    }

    private static String sdmmsURL[] =  {
        SDAUTHORITY + "/conversations",
        SDAUTHORITY + "/conversations/#",
        SDAUTHORITY + "/conversations/#/recipients",
        SDAUTHORITY + "/messages/byphone/#",
        SDAUTHORITY + "/threadID",
        SDAUTHORITY + "/canonical-address/#",
        SDAUTHORITY + "/pending",
        SDAUTHORITY + "/complete-conversations",
        SDAUTHORITY + "/undelivered",
        SDAUTHORITY + "/conversations/#/subject",
        SDAUTHORITY + "/notifications",
        SDAUTHORITY + "/conversations/obsolete",
        SDAUTHORITY + "/draft",
        SDAUTHORITY + "/canonical-addresses",
        SDAUTHORITY + "/search",
        SDAUTHORITY + "/searchSuggest",
        SDAUTHORITY + "/locked",
        SDAUTHORITY + "/locked/#",
        SDAUTHORITY + "/messageIdToThread",
    };

    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);

    private static final String AUTHORITY = "mms-sms";

    static {
        URI_MATCHER.addURI(AUTHORITY, "conversations", URI_CONVERSATIONS);
        URI_MATCHER.addURI(AUTHORITY, "complete-conversations", URI_COMPLETE_CONVERSATIONS);
        URI_MATCHER.addURI(AUTHORITY, "conversations/#", URI_CONVERSATIONS_MESSAGES);
        URI_MATCHER.addURI(AUTHORITY, "conversations/#/recipients",URI_CONVERSATIONS_RECIPIENTS);
        URI_MATCHER.addURI(AUTHORITY, "conversations/#/subject",URI_CONVERSATIONS_SUBJECT);
        URI_MATCHER.addURI(AUTHORITY, "conversations/obsolete", URI_OBSOLETE_THREADS);
        URI_MATCHER.addURI(AUTHORITY, "messages/byphone/*",URI_MESSAGES_BY_PHONE);
        URI_MATCHER.addURI(AUTHORITY, "threadID", URI_THREAD_ID);
        URI_MATCHER.addURI(AUTHORITY, "canonical-address/#", URI_CANONICAL_ADDRESS);
        URI_MATCHER.addURI(AUTHORITY, "canonical-addresses", URI_CANONICAL_ADDRESSES);
        URI_MATCHER.addURI(AUTHORITY, "search", URI_SEARCH);
        URI_MATCHER.addURI(AUTHORITY, "searchSuggest", URI_SEARCH_SUGGEST);
        URI_MATCHER.addURI(AUTHORITY, "pending", URI_PENDING_MSG);
        URI_MATCHER.addURI(AUTHORITY, "undelivered", URI_UNDELIVERED_MSG);
        URI_MATCHER.addURI(AUTHORITY, "notifications", URI_NOTIFICATIONS);
        URI_MATCHER.addURI(AUTHORITY, "draft", URI_DRAFT);
        URI_MATCHER.addURI(AUTHORITY, "locked", URI_FIRST_LOCKED_MESSAGE_ALL);
        URI_MATCHER.addURI(AUTHORITY, "locked/#", URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID);
        URI_MATCHER.addURI(AUTHORITY, "messageIdToThread", URI_MESSAGE_ID_TO_THREAD);
    }

    private static String mmsURL[] =  {
        AUTHORITY + "/conversations",
        AUTHORITY + "/conversations/#",
        AUTHORITY + "/conversations/#/recipients",
        AUTHORITY + "/messages/byphone/#",
        AUTHORITY + "/threadID",
        AUTHORITY + "/canonical-address/#",
        AUTHORITY + "/pending",
        AUTHORITY + "/complete-conversations",
        AUTHORITY + "/undelivered",
        AUTHORITY + "/conversations/#/subject",
        AUTHORITY + "/notifications",
        AUTHORITY + "/conversations/obsolete",
        AUTHORITY + "/draft",
        AUTHORITY + "/canonical-addresses",
        AUTHORITY + "/search",
        AUTHORITY + "/searchSuggest",
        AUTHORITY + "/locked",
        AUTHORITY + "/locked/#",
        AUTHORITY + "/messageIdToThread",
    };
    
    public static final String TABLE_PENDING_MSG = "pending_msgs";

    private static final String TABLE_CANONICAL_ADDRESSES = "canonical_addresses";

    public static final String TABLE_THREADS = "threads";
    
    public static final class WordsTable {
        public static final String ID = "_id";
        public static final String SOURCE_ROW_ID = "source_id";
        public static final String TABLE_ID = "table_to_use";
        public static final String INDEXED_TEXT = "index_text";
    }

}
