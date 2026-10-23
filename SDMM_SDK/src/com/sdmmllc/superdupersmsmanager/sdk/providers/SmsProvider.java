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

package com.sdmmllc.superdupersmsmanager.sdk.providers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.UriMatcher;
import android.net.Uri;

public class SmsProvider {
    
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
        if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
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
        if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
        return Uri.parse("content://" + newUri + query);
    }

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
