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

package com.sdmmllc.superdupersmsmanager.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.services.SdmmsObserverService;
import com.sdmmllc.superdupersmsmanager.utils.ParcelFileDescriptorUtil;

public class MmsProvider extends ContentProvider {
    private ContentProviderClient cpc;

    @Override
    public boolean onCreate() {
    	Intent sintent = new Intent(this.getContext(), SdmmsObserverService.class);
    	this.getContext().startService(sintent);        
        cpc = this.getContext().getContentResolver().acquireContentProviderClient(Uri.parse("content://mms"));
        if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "Acquired CPC: " + (cpc !=null));
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Generate the body of the query.
        Uri newUri = convert(uri);
        if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "query Uri: " + uri.toString());
        if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "new query Uri: " + newUri.toString());
        try {
			return cpc.query(convert(uri), projection, selection, selectionArgs, sortOrder);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().query(convert(uri), projection, selection, selectionArgs, sortOrder);
		}
    }

    @Override
    public String getType(Uri uri) {
        if (SdmmsConsts.DEBUG_MMS_PROVIDER) {
            Log.v(TAG, "getType problem? uri request: " + uri.toString());
        }

		String type;
    	try {
            if (SdmmsConsts.DEBUG_MMS_PROVIDER) {
                Log.v(TAG, "getType problem? cpc.getType(convert(uri)): " + cpc.getType(convert(uri)));
            }
    		type = cpc.getType(convert(uri));
            if (SdmmsConsts.DEBUG_MMS_PROVIDER) {
                Log.v(TAG, "getType problem? type returned: " + type);
            }
			return type;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			type = uriToSdmms(Uri.parse(this.getContext().getContentResolver().getType(convert(uri)))).toString();
            if (SdmmsConsts.DEBUG_MMS_PROVIDER) {
                Log.v(TAG, "getType problem? type returned: " + type);
            }
			return type;
		}
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
    	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "insert uri: " + uri.toString());
    	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "insert convert(uri): " + convert(uri).toString());
    	try {
        	Uri retval = cpc.insert(convert(uri), values);
        	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "insert cpc retval: " + retval.toString());
        	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "insert cpc invert(retval): " + uriToSdmms(retval).toString());
        	if (MmsProvider.uriToSdmmsMatch(retval)) return MmsProvider.uriToSdmms(retval);
        	if (MmsSmsProvider.uriInvertMatch(retval)) return MmsSmsProvider.invert(retval);
        	if (SmsProvider.uriInvertMatch(retval)) return SmsProvider.invert(retval);
        	return retval;
		} catch (Exception e) {
        	Uri retval = this.getContext().getContentResolver().insert(convert(uri), values);
        	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "insert non-cpc retval: " + retval.toString());
        	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "insert non-cpc invert(retval): " + uriToSdmms(retval).toString());
        	if (MmsProvider.uriToSdmmsMatch(retval)) return MmsProvider.uriToSdmms(retval);
        	if (MmsSmsProvider.uriInvertMatch(retval)) return MmsSmsProvider.invert(retval);
        	if (SmsProvider.uriInvertMatch(retval)) return SmsProvider.invert(retval);
        	return retval;
		}
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
    	try {
			return cpc.delete(convert(uri), selection, selectionArgs);
		} catch (Exception e) {
	    	return this.getContext().getContentResolver().delete(convert(uri), selection, selectionArgs);
		}
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "update uri: " + uri);
    	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "update uriConvert(uri): " + convert(uri));
    	try {
			return cpc.update(convert(uri), values, selection, selectionArgs);
		} catch (Exception e) {
        	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "update uri is null: " + (uri == null));
        	if (SdmmsConsts.DEBUG_MMS_PROVIDER && uri != null) Log.i(TAG, "update uri: " + uri.toString());
			if (SdmmsConsts.DEBUG_MMS_PROVIDER) e.printStackTrace();
	    	return this.getContext().getContentResolver().update(convert(uri), values, selection, selectionArgs);
		}
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Log.v(TAG, "openFile trying uri: " + uri + " convert(uri): " + convert(uri) + " mode: " + mode);
        try {
        	if (ParcelFileDescriptorUtil.writeMode(mode))
				return ParcelFileDescriptorUtil.pipeTo(this.getContext(), 
						new ParcelFileDescriptor.AutoCloseOutputStream(cpc.openFile(convert(uri), mode)),
						uri);
        	else 
				return cpc.openFile(convert(uri), mode);
//				return ParcelFileDescriptorUtil.pipeFrom(this.getContext(),
//						new ParcelFileDescriptor.AutoCloseInputStream(cpc.openFile(convert(uri), mode)),
//						uri);
		} catch (Exception e) {
            Log.v(TAG, "openFile Exception uri: " + uri + " convert(uri): " + convert(uri) + " mode: " + mode 
            		+ " error: " + e);
			e.printStackTrace();
			return null;
		}
    }

    public ParcelFileDescriptor openSDFile(Uri uri, String mode) throws FileNotFoundException {
        if (SdmmsConsts.DEBUG_MMS_PROVIDER) {
            Log.v(TAG, "openFile problem? uri: " + uri + " convert(uri): " + convert(uri) + " mode: " + mode);
        }
        int match = sURLMatcher.match(uri);

        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.d(TAG, "openFile: uri=" + uri + ", mode=" + mode + ", match=" + match);
        }

        if (match != MMS_PART_ID) {
            return null;
        }

        // Verify that the _data path points to mms data
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            // If there is not exactly one result, throw an appropriate
            // exception.
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }

        c.moveToFirst();
        int i = c.getColumnIndex("_data");
        String path = (i >= 0 ? c.getString(i) : null);
        c.close();

        if (path == null) {
            return null;
        }
        try {
            File filePath = new File(path);
            if (!filePath.getCanonicalPath()
                    .startsWith(getContext().getApplicationInfo().dataDir + "/app_parts/")) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return openSDFileHelper(uri, mode);
    }
    
    protected final ParcelFileDescriptor openSDFileHelper(Uri uri,
            String mode) throws FileNotFoundException {
        Cursor c = query(uri, new String[]{"_data"}, null, null, null);
        int count = (c != null) ? c.getCount() : 0;
        if (count != 1) {
            // If there is not exactly one result, throw an appropriate
            // exception.
            if (c != null) {
                c.close();
            }
            if (count == 0) {
                throw new FileNotFoundException("No entry for " + uri);
            }
            throw new FileNotFoundException("Multiple items at " + uri);
        }

        c.moveToFirst();
        int i = c.getColumnIndex("_data");
        String path = (i >= 0 ? c.getString(i) : null);
        c.close();
        if (path == null) {
            throw new FileNotFoundException("Column _data not found.");
        }

        int modeBits = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(new File(path), modeBits);
    }

    
    @Override
    public void onLowMemory() {
    	cpc.release();
    }
    
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
    	if (method.equals("notify")) notifyChange();
    	return null;
    }

    static Pattern hash = Pattern.compile("#");
    static Matcher matcher;
    
    static Pattern p = Pattern.compile("(\\d+)");
    static Matcher digitmatcher;
    
    public static Uri convert(Uri uri) {
        int match = sURLMatcher.match(uri);
    	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "convert uri: " + uri + " match: " + mmsURL[match]);
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
    	if (SdmmsConsts.DEBUG_MMS_PROVIDER) Log.i(TAG, "convert newUri: " + newUri + " match: " + mmsURL[match]);
        return Uri.parse("content://" + newUri + query);
    }
    
    public static boolean uriConvertMatch(Uri uri) {
    	return sURLMatcher.match(uri) >= 0;
    }
    
    public static boolean uriToSdmmsMatch(Uri uri) {
    	return mmsURLMatcher.match(uri) >= 0;
    }
    
    public static Uri uriToSdmms(Uri uri) {
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
    
    private void notifyChange() {
        getContext().getContentResolver().notifyChange(
                SdmmsConsts.SDMMS_URI, null);
    }

    private final static String TAG = "MmsProvider";
    private final static String VND_ANDROID_SDMMS = "vnd.android/mms";
    private final static String VND_ANDROID_DIR_SDMMS = "vnd.android-dir/mms";
    private final static boolean DEBUG = false;
    private final static boolean LOCAL_LOGV = false;

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
        "sdmms/addr/#",
    };
    
    private final static String VND_ANDROID_MMS = "vnd.android/mms";
    private final static String VND_ANDROID_DIR_MMS = "vnd.android-dir/mms";

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
    private static final int MMS_TEMP_MSG_PART            = 21;
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

    public static final String TABLE_PDU  = "pdu";
    public static final String TABLE_ADDR = "addr";
    public static final String TABLE_PART = "part";
    public static final String TABLE_RATE = "rate";
    public static final String TABLE_DRM  = "drm";
    public static final String TABLE_WORDS = "words";

}

