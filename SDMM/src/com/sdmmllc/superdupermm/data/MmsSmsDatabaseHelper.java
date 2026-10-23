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

package com.sdmmllc.superdupermm.data;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Addr;
import android.provider.Telephony.Mms.Part;
import android.provider.Telephony.Mms.Rate;
import android.provider.Telephony.MmsSms.PendingMessages;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Threads;
import android.util.Log;

import com.sdmmllc.superdupersmsmanager.providers.MmsProvider;
import com.sdmmllc.superdupersmsmanager.providers.MmsSmsProvider;


@TargetApi(19)
public class MmsSmsDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MmsSmsDatabaseHelper";

    private static MmsSmsDatabaseHelper sInstance = null;
    private static boolean sTriedAutoIncrement = false;
    private static boolean sFakeLowStorageTest = false;     // for testing only

    static final String DATABASE_NAME = "mmssms.db";
    static final int DATABASE_VERSION = 57;
    private final Context mContext;
    private LowStorageMonitor mLowStorageMonitor;


    private MmsSmsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        mContext = context;
    }

    /**
     * Return a singleton helper for the combined MMS and SMS
     * database.
     */
    /* package */ static synchronized MmsSmsDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MmsSmsDatabaseHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createMmsTables(db);
        createSmsTables(db);
        createCommonTables(db);
    }

    private void createMmsTables(SQLiteDatabase db) {
        // N.B.: Whenever the columns here are changed, the columns in
        // {@ref MmsSmsProvider} must be changed to match.
        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_PDU + " (" +
                   Mms._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                   Mms.THREAD_ID + " INTEGER," +
                   Mms.DATE + " INTEGER," +
                   Mms.DATE_SENT + " INTEGER DEFAULT 0," +
                   Mms.MESSAGE_BOX + " INTEGER," +
                   Mms.READ + " INTEGER DEFAULT 0," +
                   Mms.MESSAGE_ID + " TEXT," +
                   Mms.SUBJECT + " TEXT," +
                   Mms.SUBJECT_CHARSET + " INTEGER," +
                   Mms.CONTENT_TYPE + " TEXT," +
                   Mms.CONTENT_LOCATION + " TEXT," +
                   Mms.EXPIRY + " INTEGER," +
                   Mms.MESSAGE_CLASS + " TEXT," +
                   Mms.MESSAGE_TYPE + " INTEGER," +
                   Mms.MMS_VERSION + " INTEGER," +
                   Mms.MESSAGE_SIZE + " INTEGER," +
                   Mms.PRIORITY + " INTEGER," +
                   Mms.READ_REPORT + " INTEGER," +
                   Mms.REPORT_ALLOWED + " INTEGER," +
                   Mms.RESPONSE_STATUS + " INTEGER," +
                   Mms.STATUS + " INTEGER," +
                   Mms.TRANSACTION_ID + " TEXT," +
                   Mms.RETRIEVE_STATUS + " INTEGER," +
                   Mms.RETRIEVE_TEXT + " TEXT," +
                   Mms.RETRIEVE_TEXT_CHARSET + " INTEGER," +
                   Mms.READ_STATUS + " INTEGER," +
                   Mms.CONTENT_CLASS + " INTEGER," +
                   Mms.RESPONSE_TEXT + " TEXT," +
                   Mms.DELIVERY_TIME + " INTEGER," +
                   Mms.DELIVERY_REPORT + " INTEGER," +
                   Mms.LOCKED + " INTEGER DEFAULT 0," +
                   Mms.SEEN + " INTEGER DEFAULT 0," +
                   Mms.TEXT_ONLY + " INTEGER DEFAULT 0" +
                   ");");

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_ADDR + " (" +
                   Addr._ID + " INTEGER PRIMARY KEY," +
                   Addr.MSG_ID + " INTEGER," +
                   Addr.CONTACT_ID + " INTEGER," +
                   Addr.ADDRESS + " TEXT," +
                   Addr.TYPE + " INTEGER," +
                   Addr.CHARSET + " INTEGER);");

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_PART + " (" +
                   Part._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                   Part.MSG_ID + " INTEGER," +
                   Part.SEQ + " INTEGER DEFAULT 0," +
                   Part.CONTENT_TYPE + " TEXT," +
                   Part.NAME + " TEXT," +
                   Part.CHARSET + " INTEGER," +
                   Part.CONTENT_DISPOSITION + " TEXT," +
                   Part.FILENAME + " TEXT," +
                   Part.CONTENT_ID + " TEXT," +
                   Part.CONTENT_LOCATION + " TEXT," +
                   Part.CT_START + " INTEGER," +
                   Part.CT_TYPE + " TEXT," +
                   Part._DATA + " TEXT," +
                   Part.TEXT + " TEXT);");

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_RATE + " (" +
                   Rate.SENT_TIME + " INTEGER);");

        db.execSQL("CREATE TABLE " + MmsProvider.TABLE_DRM + " (" +
                   BaseColumns._ID + " INTEGER PRIMARY KEY," +
                   "_data TEXT);");
    }

    private void createSmsTables(SQLiteDatabase db) {
        // N.B.: Whenever the columns here are changed, the columns in
        // {@ref MmsSmsProvider} must be changed to match.
        db.execSQL("CREATE TABLE sms (" +
                   "_id INTEGER PRIMARY KEY," +
                   "thread_id INTEGER," +
                   "address TEXT," +
                   "person INTEGER," +
                   "date INTEGER," +
                   "date_sent INTEGER DEFAULT 0," +
                   "protocol INTEGER," +
                   "read INTEGER DEFAULT 0," +
                   "status INTEGER DEFAULT -1," + // a TP-Status value
                                                  // or -1 if it
                                                  // status hasn't
                                                  // been received
                   "type INTEGER," +
                   "reply_path_present INTEGER," +
                   "subject TEXT," +
                   "body TEXT," +
                   "service_center TEXT," +
                   "locked INTEGER DEFAULT 0," +
                   "error_code INTEGER DEFAULT 0," +
                   "seen INTEGER DEFAULT 0" +
                   ");");

        /**
         * This table is used by the SMS dispatcher to hold
         * incomplete partial messages until all the parts arrive.
         */
        db.execSQL("CREATE TABLE raw (" +
                   "_id INTEGER PRIMARY KEY," +
                   "date INTEGER," +
                   "reference_number INTEGER," + // one per full message
                   "count INTEGER," + // the number of parts
                   "sequence INTEGER," + // the part number of this message
                   "destination_port INTEGER," +
                   "address TEXT," +
                   "pdu TEXT);"); // the raw PDU for this part

        db.execSQL("CREATE TABLE attachments (" +
                   "sms_id INTEGER," +
                   "content_url TEXT," +
                   "offset INTEGER);");

        /**
         * This table is used by the SMS dispatcher to hold pending
         * delivery status report intents.
         */
        db.execSQL("CREATE TABLE sr_pending (" +
                   "reference_number INTEGER," +
                   "action TEXT," +
                   "data TEXT);");
    }

    private void createCommonTables(SQLiteDatabase db) {
        // TODO Ensure that each entry is removed when the last use of
        // any address equivalent to its address is removed.

        /**
         * This table maps the first instance seen of any particular
         * MMS/SMS address to an ID, which is then used as its
         * canonical representation.  If the same address or an
         * equivalent address (as determined by our Sqlite
         * PHONE_NUMBERS_EQUAL extension) is seen later, this same ID
         * will be used. The _id is created with AUTOINCREMENT so it
         * will never be reused again if a recipient is deleted.
         */
        db.execSQL("CREATE TABLE canonical_addresses (" +
                   "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                   "address TEXT);");

        /**
         * This table maps the subject and an ordered set of recipient
         * IDs, separated by spaces, to a unique thread ID.  The IDs
         * come from the canonical_addresses table.  This works
         * because messages are considered to be part of the same
         * thread if they have the same subject (or a null subject)
         * and the same set of recipients.
         */
        db.execSQL("CREATE TABLE threads (" +
                   Threads._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                   Threads.DATE + " INTEGER DEFAULT 0," +
                   Threads.MESSAGE_COUNT + " INTEGER DEFAULT 0," +
                   Threads.RECIPIENT_IDS + " TEXT," +
                   Threads.SNIPPET + " TEXT," +
                   Threads.SNIPPET_CHARSET + " INTEGER DEFAULT 0," +
                   Threads.READ + " INTEGER DEFAULT 1," +
                   Threads.TYPE + " INTEGER DEFAULT 0," +
                   Threads.ERROR + " INTEGER DEFAULT 0," +
                   Threads.HAS_ATTACHMENT + " INTEGER DEFAULT 0);");

        /**
         * This table stores the queue of messages to be sent/downloaded.
         */
        db.execSQL("CREATE TABLE " + MmsSmsProvider.TABLE_PENDING_MSG +" (" +
                   PendingMessages._ID + " INTEGER PRIMARY KEY," +
                   PendingMessages.PROTO_TYPE + " INTEGER," +
                   PendingMessages.MSG_ID + " INTEGER," +
                   PendingMessages.MSG_TYPE + " INTEGER," +
                   PendingMessages.ERROR_TYPE + " INTEGER," +
                   PendingMessages.ERROR_CODE + " INTEGER," +
                   PendingMessages.RETRY_INDEX + " INTEGER NOT NULL DEFAULT 0," +
                   PendingMessages.DUE_TIME + " INTEGER," +
                   PendingMessages.LAST_TRY + " INTEGER);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion
                + " to " + currentVersion + ".");

        switch (oldVersion) {
        case 55:
            if (currentVersion <= 55) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion56(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            // fall through
        case 56:
            if (currentVersion <= 56) {
                return;
            }

            db.beginTransaction();
            try {
                upgradeDatabaseToVersion57(db);
                db.setTransactionSuccessful();
            } catch (Throwable ex) {
                Log.e(TAG, ex.getMessage(), ex);
                break;
            } finally {
                db.endTransaction();
            }
            return;
        }

        Log.e(TAG, "Destroying all old data.");
        dropAll(db);
        onCreate(db);
    }

    private void dropAll(SQLiteDatabase db) {
        // Clean the database out in order to start over from scratch.
        // We don't need to drop our triggers here because SQLite automatically
        // drops a trigger when its attached database is dropped.
        db.execSQL("DROP TABLE IF EXISTS canonical_addresses");
        db.execSQL("DROP TABLE IF EXISTS threads");
        db.execSQL("DROP TABLE IF EXISTS " + MmsSmsProvider.TABLE_PENDING_MSG);
        db.execSQL("DROP TABLE IF EXISTS sms");
        db.execSQL("DROP TABLE IF EXISTS raw");
        db.execSQL("DROP TABLE IF EXISTS attachments");
        db.execSQL("DROP TABLE IF EXISTS thread_ids");
        db.execSQL("DROP TABLE IF EXISTS sr_pending");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_PDU + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_ADDR + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_PART + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_RATE + ";");
        db.execSQL("DROP TABLE IF EXISTS " + MmsProvider.TABLE_DRM + ";");
    }

     private void upgradeDatabaseToVersion56(SQLiteDatabase db) {
        // Add 'text_only' column to pdu table.
        db.execSQL("ALTER TABLE " + MmsProvider.TABLE_PDU + " ADD COLUMN " + Mms.TEXT_ONLY +
                " INTEGER DEFAULT 0");
    }

    private void upgradeDatabaseToVersion57(SQLiteDatabase db) {
        // Clear out bad rows, those with empty threadIds, from the pdu table.
        db.execSQL("DELETE FROM " + MmsProvider.TABLE_PDU + " WHERE " + Mms.THREAD_ID + " IS NULL");
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();

        return db;
    }

    private class LowStorageMonitor extends BroadcastReceiver {

        public LowStorageMonitor() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "[LowStorageMonitor] onReceive intent " + action);

            if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                sTriedAutoIncrement = false;    // try to upgrade on the next getWriteableDatabase
            }
        }
    }

}