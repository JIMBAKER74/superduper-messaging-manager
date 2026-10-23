/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.sdmmllc.superdupersmsmanager.sdk.providers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;

import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;

public class MmsProvider {

    static Pattern hash = Pattern.compile("#");
    static Matcher matcher;
    
    static Pattern p = Pattern.compile("(\\d+)");
    static Matcher digitmatcher;
    
    public static Uri convert(Uri uri) {
        int match = sURLMatcher.match(uri);
    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "convert uri: " + uri + " match: " + mmsURL[match]);
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
    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "convert newUri: " + newUri + " match: " + mmsURL[match]);
        return Uri.parse("content://" + newUri + query);
    }
    
    public static boolean uriConvertMatch(Uri uri) {
    	return sURLMatcher.match(uri) >= 0;
    }
    
    public static boolean uriInvertMatch(Uri uri) {
    	return mmsURLMatcher.match(uri) >= 0;
    }
    
    public static Uri invert(Uri uri) {
        int match = mmsURLMatcher.match(uri);
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
    
    private final static String TAG = "MmsProvider";

    private static final int SDMMS_ALL                      = 0;
    private static final int SDMMS_ALL_ID                   = 1;
    private static final int SDMMS_INBOX                    = 2;
    private static final int SDMMS_INBOX_ID                 = 3;
    private static final int SDMMS_SENT                     = 4;
    private static final int SDMMS_SENT_ID                  = 5;
    private static final int SDMMS_DRAFTS                   = 6;
    private static final int SDMMS_DRAFTS_ID                = 7;
    private static final int SDMMS_OUTBOX                   = 8;
    private static final int SDMMS_OUTBOX_ID                = 9;
    private static final int SDMMS_ALL_PART                 = 10;
    private static final int SDMMS_MSG_PART                 = 11;
    private static final int SDMMS_PART_ID                  = 12;
    private static final int SDMMS_MSG_ADDR                 = 13;
    private static final int SDMMS_SENDING_RATE             = 14;
    private static final int SDMMS_REPORT_STATUS            = 15;
    private static final int SDMMS_REPORT_REQUEST           = 16;
    private static final int SDMMS_DRM_STORAGE              = 17;
    private static final int SDMMS_DRM_STORAGE_ID           = 18;
    private static final int SDMMS_THREADS                  = 19;
    private static final int SDMMS_PART_RESET_FILE_PERMISSION = 20;
    private static final int SDMMS_TEMP_MSG_PART                 = 21;
    private static final int SDMMS_MSG_ADDR_PART                 = 22;

    private static final UriMatcher
            sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURLMatcher.addURI("sdmms", null,         SDMMS_ALL);
        sURLMatcher.addURI("sdmms", "#",          SDMMS_ALL_ID);
        sURLMatcher.addURI("sdmms", "inbox",      SDMMS_INBOX);
        sURLMatcher.addURI("sdmms", "inbox/#",    SDMMS_INBOX_ID);
        sURLMatcher.addURI("sdmms", "sent",       SDMMS_SENT);
        sURLMatcher.addURI("sdmms", "sent/#",     SDMMS_SENT_ID);
        sURLMatcher.addURI("sdmms", "drafts",     SDMMS_DRAFTS);
        sURLMatcher.addURI("sdmms", "drafts/#",   SDMMS_DRAFTS_ID);
        sURLMatcher.addURI("sdmms", "outbox",     SDMMS_OUTBOX);
        sURLMatcher.addURI("sdmms", "outbox/#",   SDMMS_OUTBOX_ID);
        sURLMatcher.addURI("sdmms", "part",       SDMMS_ALL_PART);
        sURLMatcher.addURI("sdmms", "#/part",     SDMMS_MSG_PART);
        sURLMatcher.addURI("sdmms", "part/#",     SDMMS_PART_ID);
        sURLMatcher.addURI("sdmms", "#/addr",     SDMMS_MSG_ADDR);
        sURLMatcher.addURI("sdmms", "rate",       SDMMS_SENDING_RATE);
        sURLMatcher.addURI("sdmms", "report-status/#",  SDMMS_REPORT_STATUS);
        sURLMatcher.addURI("sdmms", "report-request/#", SDMMS_REPORT_REQUEST);
        sURLMatcher.addURI("sdmms", "drm",        SDMMS_DRM_STORAGE);
        sURLMatcher.addURI("sdmms", "drm/#",      SDMMS_DRM_STORAGE_ID);
        sURLMatcher.addURI("sdmms", "threads",    SDMMS_THREADS);
        sURLMatcher.addURI("sdmms", "resetFilePerm/*",    SDMMS_PART_RESET_FILE_PERMISSION);
        sURLMatcher.addURI("sdmms", "#/part/#",     SDMMS_TEMP_MSG_PART);
        sURLMatcher.addURI("sdmms", "addr/#",     SDMMS_MSG_ADDR_PART);
    }

    private static String sdmmsURL[] = {
        "sdmms",
        "sdmms/#",
        "sdmms/inbox",
        "sdmms/inbox/#",
        "sdmms/sent",
        "sdmms/sent/#",
        "sdmms/drafts",
        "sdmms/drafts/#",
        "sdmms/outbox",
        "sdmms/outbox/#",
        "sdmms/part",
        "sdmms/#/part",
        "sdmms/part/#",
        "sdmms/#/addr",
        "sdmms/rate",
        "sdmms/report-status/#",
        "sdmms/report-request/#",
        "sdmms/drm",
        "sdmms/drm/#",
        "sdmms/threads",
        "sdmms/resetFilePerm/*",
        "sdmms/#/part/#",
        "sdmms/#/addr",
    };

    private static final int MMS_ALL                      = 0;
    private static final int MMS_ALL_ID                   = 1;
    private static final int MMS_INBOX                    = 2;
    private static final int MMS_INBOX_ID                 = 3;
    private static final int MMS_SENT                     = 4;
    private static final int MMS_SENT_ID                  = 5;
    private static final int MMS_DRAFTS                   = 6;
    private static final int MMS_DRAFTS_ID                = 7;
    private static final int MMS_OUTBOX                   = 8;
    private static final int MMS_OUTBOX_ID                = 9;
    private static final int MMS_ALL_PART                 = 10;
    private static final int MMS_MSG_PART                 = 11;
    private static final int MMS_PART_ID                  = 12;
    private static final int MMS_MSG_ADDR                 = 13;
    private static final int MMS_SENDING_RATE             = 14;
    private static final int MMS_REPORT_STATUS            = 15;
    private static final int MMS_REPORT_REQUEST           = 16;
    private static final int MMS_DRM_STORAGE              = 17;
    private static final int MMS_DRM_STORAGE_ID           = 18;
    private static final int MMS_THREADS                  = 19;
    private static final int MMS_PART_RESET_FILE_PERMISSION = 20;
    private static final int MMS_TEMP_MSG_PART                 = 21;
    private static final int MMS_MSG_ADDR_PART                 = 22;

    private static final UriMatcher
    	mmsURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        mmsURLMatcher.addURI("mms", null,         MMS_ALL);
        mmsURLMatcher.addURI("mms", "#",    	  MMS_ALL_ID);
        mmsURLMatcher.addURI("mms", "inbox",      MMS_INBOX);
        mmsURLMatcher.addURI("mms", "inbox/#",    MMS_INBOX_ID);
        mmsURLMatcher.addURI("mms", "sent",       MMS_SENT);
        mmsURLMatcher.addURI("mms", "sent/#",     MMS_SENT_ID);
        mmsURLMatcher.addURI("mms", "drafts",     MMS_DRAFTS);
        mmsURLMatcher.addURI("mms", "drafts/#",   MMS_DRAFTS_ID);
        mmsURLMatcher.addURI("mms", "outbox",     MMS_OUTBOX);
        mmsURLMatcher.addURI("mms", "outbox/#",   MMS_OUTBOX_ID);
        mmsURLMatcher.addURI("mms", "part",       MMS_ALL_PART);
        mmsURLMatcher.addURI("mms", "#/part",     MMS_MSG_PART);
        mmsURLMatcher.addURI("mms", "part/#",     MMS_PART_ID);
        mmsURLMatcher.addURI("mms", "#/addr",     MMS_MSG_ADDR);
        mmsURLMatcher.addURI("mms", "rate",       MMS_SENDING_RATE);
        mmsURLMatcher.addURI("mms", "report-status/#",  MMS_REPORT_STATUS);
        mmsURLMatcher.addURI("mms", "report-request/#", MMS_REPORT_REQUEST);
        mmsURLMatcher.addURI("mms", "drm",        MMS_DRM_STORAGE);
        mmsURLMatcher.addURI("mms", "drm/#",      MMS_DRM_STORAGE_ID);
        mmsURLMatcher.addURI("mms", "threads",    MMS_THREADS);
        mmsURLMatcher.addURI("mms", "resetFilePerm/*",    MMS_PART_RESET_FILE_PERMISSION);
        mmsURLMatcher.addURI("mms", "#/part/#",     MMS_TEMP_MSG_PART);
        mmsURLMatcher.addURI("mms", "addr/#",     MMS_MSG_ADDR_PART);
    }
    
    private static String mmsURL[] = {
        "mms",
        "mms/#",
        "mms/inbox",
        "mms/inbox/#",
        "mms/sent",
        "mms/sent/#",
        "mms/drafts",
        "mms/drafts/#",
        "mms/outbox",
        "mms/outbox/#",
        "mms/part",
        "mms/#/part",
        "mms/part/#",
        "mms/#/addr",
        "mms/rate",
        "mms/report-status/#",
        "mms/report-request/#",
        "mms/drm",
        "mms/drm/#",
        "mms/threads",
        "mms/resetFilePerm/*",
        "mms/#/part/#",
        "mms/addr/#",
    };

}

