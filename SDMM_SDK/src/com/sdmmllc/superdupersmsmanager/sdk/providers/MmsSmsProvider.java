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

package com.sdmmllc.superdupersmsmanager.sdk.providers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.UriMatcher;
import android.net.Uri;

public class MmsSmsProvider {
    
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
        if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
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
        if (matcher.find() && digitmatcher.find()) newUri = matcher.replaceFirst(digitmatcher.group());
        return Uri.parse("content://" + newUri + query);
    }

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
}
