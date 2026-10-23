/*
 * Copyright (C) 2008 Esmertec AG.
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

package com.messagingappsllc.superdupermms.mms;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.drm.DrmManagerClient;
import android.net.Uri;
import android.os.RemoteException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.SearchRecentSuggestions;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Logger.LogLevel;
import com.google.analytics.tracking.android.Tracker;
import com.messagingappsllc.superdupermms.mms.data.Contact;
import com.messagingappsllc.superdupermms.mms.data.Conversation;
import com.messagingappsllc.superdupermms.mms.layout.LayoutManager;
import com.messagingappsllc.superdupermms.mms.templates.TemplatesProvider;
import com.messagingappsllc.superdupermms.mms.transaction.MessagingNotification;
import com.messagingappsllc.superdupermms.mms.transaction.MmsSystemEventReceiver;
import com.messagingappsllc.superdupermms.mms.transaction.SmsReceiver;
import com.messagingappsllc.superdupermms.mms.transaction.SmsReceiverService;
import com.messagingappsllc.superdupermms.mms.util.DownloadManager;
import com.messagingappsllc.superdupermms.mms.util.DraftCache;
import com.messagingappsllc.superdupermms.mms.util.EmojiParser;
import com.messagingappsllc.superdupermms.mms.util.PduLoaderManager;
import com.messagingappsllc.superdupermms.mms.util.RateController;
import com.messagingappsllc.superdupermms.mms.util.SmileyParser;
import com.messagingappsllc.superdupermms.mms.util.ThumbnailManager;
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class GoTxtmeApp extends Application {
    public static final String LOG_TAG = "GoTxtmeApp";

    private SearchRecentSuggestions mRecentSuggestions;
    private TelephonyManager mTelephonyManager;
    private String mCountryIso;
    private static GoTxtmeApp sMmsApp = null;
    private PduLoaderManager mPduLoaderManager;
    private ThumbnailManager mThumbnailManager;
    private DrmManagerClient mDrmManagerClient;
    private static SDSmsManager mSdmmsManager;
    private static Context mContext;
    private static GoTxtmeApp mInstance = null;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Log.isLoggable(LogTag.STRICT_MODE_TAG, Log.DEBUG)) {
            // Log tag for enabling/disabling StrictMode violation log. This will dump a stack
            // in the log that shows the StrictMode violator.
            // To enable: adb shell setprop log.tag.Mms:strictmode DEBUG
            StrictMode.setThreadPolicy(
                    new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        }

        sMmsApp = this;

        // Load the default preference values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        mCountryIso = getTelephonyManager().getSimCountryIso();

        Context context = getApplicationContext();
        mSdmmsManager = SDSmsManager.getDefault(context.getApplicationContext(), 
        		context.getString(R.string.SDSMS_ID),
        		SDSmsManager.ACCESS_TYPE_MESSAGING,
        		getString(R.string.smsSendReceiver), 
        		getString(R.string.smsReceiveReceiver));
        mSdmmsManager.setReceiverCallback(mCallback);
        mPduLoaderManager = new PduLoaderManager(context);
        mThumbnailManager = new ThumbnailManager(context);
        mInstance = this;
        mContext = this.getApplicationContext();

        MmsConfig.init(this);
        Contact.init(this);
        DraftCache.init(this);
        Conversation.init(this);
        DownloadManager.init(this);
        RateController.init(this);
        LayoutManager.init(this);
        SmileyParser.init(this);
        EmojiParser.init(this);
        MessagingNotification.init(this);
        initializeGa();
        
        createTemplatesProvider();
        activePendingMessages();
    }
	/*
	 * Google Analytics
	 */
	private static GoogleAnalytics mGa;
	private static Tracker mTracker;
	// Dispatch period in seconds.
	private static final int GA_DISPATCH_PERIOD = 30;
	// Prevent hits from being sent to reports, i.e. during testing.
	private static final boolean GA_IS_DRY_RUN = false;
	// GA Logger verbosity.
	private static final LogLevel GA_LOG_VERBOSITY = LogLevel.INFO;
	// Key used to store a user's tracking preferences in SharedPreferences.
	private static final String TRACKING_PREF_KEY = "trackingPreference";
	private void initializeGa() {
		mGa = GoogleAnalytics.getInstance(this);
		mTracker = mGa.getTracker(getString(R.string.googleAnalyticsID));
		// Set dispatch period.
		GAServiceManager.getInstance().setLocalDispatchPeriod(GA_DISPATCH_PERIOD);
		// Set dryRun flag.
		mGa.setDryRun(GA_IS_DRY_RUN);
		// Set Logger verbosity.
		mGa.getLogger().setLogLevel(GA_LOG_VERBOSITY);
	}

	public static Tracker getGaTracker() {
		return mTracker;
	}

	public static GoogleAnalytics getGaInstance() {
		return mGa;
	}
	
	public static void setContext(Context context) {
		mContext = context;
	}

    public static Context getContext() {
    	if (mContext == null) {
    		if (mInstance == null) mInstance = new GoTxtmeApp();
    		//mContext = mInstance.getApplicationContext();
    	}
		Log.i(LOG_TAG, "getContext mContext == null: " + (mContext == null));
    	return mContext;
    }
    
    private static SDSmsReceiveCallback mCallback = new SDSmsReceiveCallback() {

    	@Override
		public boolean onReceive(Intent intent) throws RemoteException {
    		SmsReceiver receiver = new SmsReceiver();
    		receiver.onReceive(sMmsApp, intent);
			Log.i(LOG_TAG, "KitKat test success! message aborted: ");
			return true;
		}

		@Override
		public String testCallbackConnection(String text) {
			Log.i(LOG_TAG, "custom callback on MmsApp successful; testConnection text:" + text);
			return "Connection successful for " + LOG_TAG;
		}
		
		@Override
		public boolean reload() {
			return false;
		}
	};

	public static SDSmsManager getSDSmsManager() {
		if (mSdmmsManager == null) {
	        mSdmmsManager = SDSmsManager.getDefault(GoTxtmeApp.getApplication(), 
	        		GoTxtmeApp.getApplication().getString(R.string.SDSMS_ID),
	        		SDSmsManager.ACCESS_TYPE_MESSAGING,
	        		"com.messagingappsllc.superdupermms.mms.transaction.SmsReceiver", 
	        		"com.messagingappsllc.superdupermms.SmsReceiver");
	        mSdmmsManager.setReceiverCallback(mCallback);
		}
		return mSdmmsManager;
	}


    /**
     * Try to process all pending messages(which were interrupted by user, OOM, Mms crashing,
     * etc...) when Mms app is (re)launched.
     */
    private void activePendingMessages() {
        // For Mms: try to process all pending transactions if possible
        MmsSystemEventReceiver.wakeUpService(this);

        // For Sms: retry to send smses in outbox and queued box
        sendBroadcast(new Intent(SmsReceiverService.ACTION_SEND_INACTIVE_MESSAGE,
                null,
                this,
                SmsReceiver.class));
    }

    synchronized public static GoTxtmeApp getApplication() {
        return sMmsApp;
    }

    @Override
    public void onTerminate() {
    	// nothing to do
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mPduLoaderManager.onLowMemory();
        mThumbnailManager.onLowMemory();
    }

    public PduLoaderManager getPduLoaderManager() {
        return mPduLoaderManager;
    }

    public ThumbnailManager getThumbnailManager() {
        return mThumbnailManager;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        LayoutManager.getInstance().onConfigurationChanged(newConfig);
    }

    /**
     * @return Returns the TelephonyManager.
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }

    /**
     * Returns the content provider wrapper that allows access to recent searches.
     * @return Returns the content provider wrapper that allows access to recent searches.
     */
    public SearchRecentSuggestions getRecentSuggestions() {
        /*
        if (mRecentSuggestions == null) {
            mRecentSuggestions = new SearchRecentSuggestions(this,
                    SuggestionsProvider.AUTHORITY, SuggestionsProvider.MODE);
        }
        */
        return mRecentSuggestions;
    }

    // This function CAN return null.
    public String getCurrentCountryIso() {
        if (mCountryIso == null) {
        	mCountryIso = getTelephonyManager().getSimCountryIso();
        }
        return mCountryIso;
    }

    public DrmManagerClient getDrmManagerClient() {
        if (mDrmManagerClient == null) {
            mDrmManagerClient = new DrmManagerClient(getApplicationContext());
        }
        return mDrmManagerClient;
    }
    
    private void createTemplatesProvider() {
    	Template template = new Template();
        Cursor cur = getContentResolver().query(template.CONTENT_URI, null, null, null, null);
        if (cur == null) {
        	TemplatesProvider tp = new TemplatesProvider();
            cur = getContentResolver().query(template.CONTENT_URI, null, null, null, null);
        	Log.i(LOG_TAG, "Created templates provider");
        }
    }

    private class Template implements BaseColumns {
        public final Uri CONTENT_URI = Uri.parse(
        		getApplication().getApplicationContext().getResources().getString(R.string.templateUri));
        public final String TEXT = "text";
    }


}
