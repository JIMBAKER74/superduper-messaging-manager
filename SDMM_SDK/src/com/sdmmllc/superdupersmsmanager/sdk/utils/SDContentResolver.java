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

package com.sdmmllc.superdupersmsmanager.sdk.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.PeriodicSync;
import android.content.SyncAdapterType;
import android.content.SyncInfo;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.content.SyncStatusObserver;
import android.content.UriPermission;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.sdk.providers.MmsProvider;
import com.sdmmllc.superdupersmsmanager.sdk.providers.MmsSmsProvider;
import com.sdmmllc.superdupersmsmanager.sdk.providers.SmsProvider;

public class SDContentResolver {
	private ContentResolver mContentResolver;
	private static String TAG = "SDContentResolver";
	
    public static final String SYNC_EXTRAS_ACCOUNT = "account";
    public static final String SYNC_EXTRAS_EXPEDITED = "expedited";
    public static final String SYNC_EXTRAS_FORCE = "force";
    public static final String SYNC_EXTRAS_IGNORE_SETTINGS = "ignore_settings";
    public static final String SYNC_EXTRAS_IGNORE_BACKOFF = "ignore_backoff";
    public static final String SYNC_EXTRAS_DO_NOT_RETRY = "do_not_retry";
    public static final String SYNC_EXTRAS_MANUAL = "force";
    public static final String SYNC_EXTRAS_UPLOAD = "upload";
    public static final String SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS = "deletions_override";
    public static final String SYNC_EXTRAS_DISCARD_LOCAL_DELETIONS = "discard_deletions";
    public static final String SYNC_EXTRAS_INITIALIZE = "initialize";
    public static final String SCHEME_CONTENT = "content";
    public static final String SCHEME_ANDROID_RESOURCE = "android.resource";
    public static final String SCHEME_FILE = "file";
    public static final String CURSOR_ITEM_BASE_TYPE = "vnd.android.cursor.item";
    public static final String CURSOR_DIR_BASE_TYPE = "vnd.android.cursor.dir";

    private static final String[] SYNC_ERROR_NAMES = new String[] {
          "already-in-progress",
          "authentication-error",
          "io-error",
          "parse-error",
          "conflict",
          "too-many-deletions",
          "too-many-retries",
          "internal-error",
    };

    public static final int SYNC_OBSERVER_TYPE_SETTINGS = 1<<0;
    public static final int SYNC_OBSERVER_TYPE_PENDING = 1<<1;
    public static final int SYNC_OBSERVER_TYPE_ACTIVE = 1<<2;    
    
    public SDContentResolver(Context context) {
        mContentResolver = context.getContentResolver();
    }

    public final String getType(Uri url) {
    	return mContentResolver.getType(url);
    }

    public String[] getStreamTypes(Uri url, String mimeTypeFilter) {
    	return mContentResolver.getStreamTypes(url, mimeTypeFilter);
    }

    public final Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        //return mContentResolver.query(SdmmsUtil.uriToSdmms(uri), projection, selection, selectionArgs, sortOrder, null);
        return mContentResolver.query(SdmmsUtil.uriToSdmms(uri), projection, selection, selectionArgs, sortOrder);
    }

    public final Cursor query(final Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder,
            CancellationSignal cancellationSignal) {
    	return mContentResolver.query(SdmmsUtil.uriToSdmms(uri), projection, selection, selectionArgs, sortOrder, cancellationSignal);
    }

    public final Uri canonicalize(Uri url) {
        return mContentResolver.canonicalize(url);
    }

    public final Uri uncanonicalize(Uri url) {
    	return mContentResolver.uncanonicalize(url);
    }

    public final InputStream openInputStream(Uri uri)
            throws FileNotFoundException {
    	return mContentResolver.openInputStream(SdmmsUtil.uriToSdmms(uri));
    }

    public final OutputStream openOutputStream(Uri uri)
            throws FileNotFoundException {
        return mContentResolver.openOutputStream(SdmmsUtil.uriToSdmms(uri));
    }

    public final OutputStream openOutputStream(Uri uri, String mode)
            throws FileNotFoundException {
    	return mContentResolver.openOutputStream(SdmmsUtil.uriToSdmms(uri), mode);
    }

    public final ParcelFileDescriptor openFileDescriptor(Uri uri, String mode)
            throws FileNotFoundException {
        return mContentResolver.openFileDescriptor(SdmmsUtil.uriToSdmms(uri), mode);
    }

    public final ParcelFileDescriptor openFileDescriptor(Uri uri,
            String mode, CancellationSignal cancellationSignal) throws FileNotFoundException {
    	return mContentResolver.openFileDescriptor(SdmmsUtil.uriToSdmms(uri), mode, cancellationSignal);
    }

    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri, String mode)
            throws FileNotFoundException {
        return mContentResolver.openAssetFileDescriptor(SdmmsUtil.uriToSdmms(uri), mode);
    }

    public final AssetFileDescriptor openAssetFileDescriptor(Uri uri,
            String mode, CancellationSignal cancellationSignal) throws FileNotFoundException {
    	return mContentResolver.openAssetFileDescriptor(SdmmsUtil.uriToSdmms(uri), mode, cancellationSignal);
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(
            Uri uri, String mimeType, Bundle opts) throws FileNotFoundException {
        return mContentResolver.openTypedAssetFileDescriptor(SdmmsUtil.uriToSdmms(uri), mimeType, opts);
    }

    public final AssetFileDescriptor openTypedAssetFileDescriptor(Uri uri,
            String mimeType, Bundle opts, CancellationSignal cancellationSignal)
            throws FileNotFoundException {
    	return mContentResolver.openTypedAssetFileDescriptor(SdmmsUtil.uriToSdmms(uri), mimeType, opts, cancellationSignal);
    }

    public final Uri insert(Uri uri, ContentValues values) {
    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "insert uri: " + uri);
    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "insert uriConvert(uri): " + SdmmsUtil.uriToSdmms(uri));
    	return SdmmsUtil.uriToMms(mContentResolver.insert(SdmmsUtil.uriToSdmms(uri), values));
    }

    public ContentProviderResult[] applyBatch(String authority,
            ArrayList<ContentProviderOperation> operations)
            throws RemoteException, OperationApplicationException {
    	return mContentResolver.applyBatch(authority, operations);
    }

    public final int bulkInsert(Uri uri, ContentValues[] values) {
    	return mContentResolver.bulkInsert(SdmmsUtil.uriToSdmms(uri), values);
    }

    public final int delete(Uri uri, String where, String[] selectionArgs) {
    	return mContentResolver.delete(SdmmsUtil.uriToSdmms(uri), where, selectionArgs);
    }

    public final int update(Uri uri, ContentValues values, String where,
            String[] selectionArgs) {
    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "update uri: " + uri);
    	if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, "update uriConvert(uri): " + SdmmsUtil.uriToSdmms(uri));
    	return mContentResolver.update(SdmmsUtil.uriToSdmms(uri), values, where, selectionArgs);
    }

    public final Bundle call(Uri uri, String method, String arg, Bundle extras) {
    	return mContentResolver.call(SdmmsUtil.uriToSdmms(uri), method, arg, extras);
    }

    public final ContentProviderClient acquireContentProviderClient(Uri uri) {
    	return mContentResolver.acquireContentProviderClient(SdmmsUtil.uriToSdmms(uri));
    }

    public final ContentProviderClient acquireContentProviderClient(String name) {
    	return mContentResolver.acquireContentProviderClient(name);
    }

    public final ContentProviderClient acquireUnstableContentProviderClient(Uri uri) {
    	return mContentResolver.acquireUnstableContentProviderClient(SdmmsUtil.uriToSdmms(uri));
    }

    public final ContentProviderClient acquireUnstableContentProviderClient(String name) {
    	return mContentResolver.acquireUnstableContentProviderClient(name);
    }

    public final void registerContentObserver(Uri uri, boolean notifyForDescendents,
            ContentObserver observer) {
        mContentResolver.registerContentObserver(SdmmsUtil.uriToSdmms(uri), notifyForDescendents, observer);
    }

    public final void unregisterContentObserver(ContentObserver observer) {
    	mContentResolver.unregisterContentObserver(observer);
    }

    public void notifyChange(Uri uri, ContentObserver observer) {
    	mContentResolver.notifyChange(SdmmsUtil.uriToSdmms(uri), observer);
    }

    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
    	mContentResolver.notifyChange(SdmmsUtil.uriToSdmms(uri), observer, syncToNetwork);
    }

    public void takePersistableUriPermission(Uri uri, int modeFlags) {
    	mContentResolver.takePersistableUriPermission(SdmmsUtil.uriToSdmms(uri), modeFlags);
    }

    public void releasePersistableUriPermission(Uri uri, int modeFlags) {
    	mContentResolver.releasePersistableUriPermission(SdmmsUtil.uriToSdmms(uri), modeFlags);
    }

    public List<UriPermission> getPersistedUriPermissions() {
    	return mContentResolver.getPersistedUriPermissions();
    }

    public List<UriPermission> getOutgoingPersistedUriPermissions() {
    	return mContentResolver.getOutgoingPersistedUriPermissions();
    }

    public void startSync(Uri uri, Bundle extras) {
    	mContentResolver.startSync(SdmmsUtil.uriToSdmms(uri), extras);
    }

    public static void requestSync(Account account, String authority, Bundle extras) {
    	ContentResolver.requestSync(account, authority, extras);
    }

    public static void requestSync(SyncRequest request) {
    	ContentResolver.requestSync(request);
    }

    public static void validateSyncExtrasBundle(Bundle extras) {
    	ContentResolver.validateSyncExtrasBundle(extras);
    }

    @Deprecated
    public void cancelSync(Uri uri) {
    	mContentResolver.cancelSync(SdmmsUtil.uriToSdmms(uri));
    }

    public static void cancelSync(Account account, String authority) {
    	ContentResolver.cancelSync(account, authority);
    }

    public static SyncAdapterType[] getSyncAdapterTypes() {
    	return ContentResolver.getSyncAdapterTypes();
    }

    public static boolean getSyncAutomatically(Account account, String authority) {
    	return ContentResolver.getSyncAutomatically(account, authority);
    }

    public static void setSyncAutomatically(Account account, String authority, boolean sync) {
    	ContentResolver.setSyncAutomatically(account, authority, sync);
    }

    public static void addPeriodicSync(Account account, String authority, Bundle extras,
            long pollFrequency) {
    	ContentResolver.addPeriodicSync(account, authority, extras, pollFrequency);
    }

    public static void removePeriodicSync(Account account, String authority, Bundle extras) {
    	ContentResolver.removePeriodicSync(account, authority, extras);
    }

    public static List<PeriodicSync> getPeriodicSyncs(Account account, String authority) {
    	return ContentResolver.getPeriodicSyncs(account, authority);
    }

    public static int getIsSyncable(Account account, String authority) {
    	return ContentResolver.getIsSyncable(account, authority);
    }

    public static void setIsSyncable(Account account, String authority, int syncable) {
    	ContentResolver.setIsSyncable(account, authority, syncable);
    }

    public static boolean getMasterSyncAutomatically() {
    	return ContentResolver.getMasterSyncAutomatically();
    }

    public static void setMasterSyncAutomatically(boolean sync) {
    	ContentResolver.setMasterSyncAutomatically(sync);
    }

    public static boolean isSyncActive(Account account, String authority) {
    	return ContentResolver.isSyncActive(account, authority);
    }

    public static SyncInfo getCurrentSync() {
    	return ContentResolver.getCurrentSync();
    }

    public static List<SyncInfo> getCurrentSyncs() {
    	return ContentResolver.getCurrentSyncs();
    }

    public static boolean isSyncPending(Account account, String authority) {
    	return ContentResolver.isSyncPending(account, authority);
    }

    public static Object addStatusChangeListener(int mask, final SyncStatusObserver callback) {
    	return ContentResolver.addStatusChangeListener(mask, callback);
    }

    public static void removeStatusChangeListener(Object handle) {
    	ContentResolver.removeStatusChangeListener(handle);
    }
}