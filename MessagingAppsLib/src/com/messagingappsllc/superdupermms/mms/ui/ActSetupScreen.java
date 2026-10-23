package com.messagingappsllc.superdupermms.mms.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.Telephony;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.messagingappsllc.superdupermms.mms.R;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class ActSetupScreen extends PreferenceActivity {

    private LayoutInflater mInflater;
    private CheckBoxPreference mVibratePref;
    private CheckBoxPreference mEnableNotificationsPref;
    private CheckBoxPreference mEnablePrivacyModePref;
    private RingtonePreference mRingtonePref;
    // QuickMessage
    private CheckBoxPreference mEnableQuickMessagePref;
    private CheckBoxPreference mEnableQmLockscreenPref;
    private CheckBoxPreference mEnableQmCloseAllPref;
    private CheckBoxPreference mEnableQmDarkThemePref;

    public static final String PrivacyNoticeStatus = "PrivacyNoticeStatus";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_setup_screen);
        addPreferencesFromResource(R.xml.setup_preferences);
        this.setTheme(R.style.Prefs);

        mEnableNotificationsPref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.NOTIFICATION_ENABLED);
        mEnablePrivacyModePref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.PRIVACY_MODE_ENABLED);
        mVibratePref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.NOTIFICATION_VIBRATE);
        mRingtonePref = (RingtonePreference) findPreference(MessagingPreferenceActivity.NOTIFICATION_RINGTONE);

        // QuickMessage
        mEnableQuickMessagePref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.QUICKMESSAGE_ENABLED);
        mEnableQmLockscreenPref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.QM_LOCKSCREEN_ENABLED);
        mEnableQmCloseAllPref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.QM_CLOSE_ALL_ENABLED);
        mEnableQmDarkThemePref = (CheckBoxPreference) findPreference(MessagingPreferenceActivity.QM_DARK_THEME_ENABLED);

        setMessagePreferences();
    }
	
    private void setMessagePreferences() {

        setEnabledNotificationsPref();

        // Privacy mode
        setEnabledPrivacyModePref();

        // QuickMessage
        setEnabledQuickMessagePref();
        setEnabledQmLockscreenPref();
        setEnabledQmCloseAllPref();
        setEnabledQmDarkThemePref();

        // If needed, migrate vibration setting from the previous tri-state setting stored in
        // NOTIFICATION_VIBRATE_WHEN to the boolean setting stored in NOTIFICATION_VIBRATE.
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.contains(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_WHEN)) {
            String vibrateWhen = sharedPreferences.
                    getString(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_WHEN, null);
            boolean vibrate = "always".equals(vibrateWhen);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            prefsEditor.putBoolean(MessagingPreferenceActivity.NOTIFICATION_VIBRATE, vibrate);
            prefsEditor.remove(MessagingPreferenceActivity.NOTIFICATION_VIBRATE_WHEN);  // remove obsolete setting
            prefsEditor.apply();
            mVibratePref.setChecked(vibrate);
        }

        String soundValue = sharedPreferences.getString(MessagingPreferenceActivity.NOTIFICATION_RINGTONE, null);
        setRingtoneSummary(soundValue);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {

    	if (preference == mEnableNotificationsPref) {
            // Update the actual "enable notifications" value that is stored in secure settings.
    		MessagingPreferenceActivity.enableNotifications(mEnableNotificationsPref.isChecked(), this);

        } else if (preference == mEnablePrivacyModePref) {
            // Update the actual "enable private mode" value that is stored in secure settings.
        	MessagingPreferenceActivity.enablePrivacyMode(mEnablePrivacyModePref.isChecked(), this);

            // Update "enable quickmessage" checkbox state
            mEnableQuickMessagePref.setEnabled(!mEnablePrivacyModePref.isChecked());

            // Update "enable dark theme" checkbox state
            mEnableQmDarkThemePref.setEnabled(!mEnablePrivacyModePref.isChecked());

        } else if (preference == mEnableQuickMessagePref) {
            // Update the actual "enable quickmessage" value that is stored in secure settings.
        	MessagingPreferenceActivity.enableQuickMessage(mEnableQuickMessagePref.isChecked(), this);

        } else if (preference == mEnableQmLockscreenPref) {
            // Update the actual "enable quickmessage on lockscreen" value that is stored in secure settings.
        	MessagingPreferenceActivity.enableQmLockscreen(mEnableQmLockscreenPref.isChecked(), this);

        } else if (preference == mEnableQmCloseAllPref) {
            // Update the actual "enable close all" value that is stored in secure settings.
        	MessagingPreferenceActivity.enableQmCloseAll(mEnableQmCloseAllPref.isChecked(), this);

        } else if (preference == mEnableQmDarkThemePref) {
            // Update the actual "enable dark theme" value that is stored in secure settings.
        	MessagingPreferenceActivity.enableQmDarkTheme(mEnableQmDarkThemePref.isChecked(), this);

        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void setRingtoneSummary(String soundValue) {
        Uri soundUri = TextUtils.isEmpty(soundValue) ? null : Uri.parse(soundValue);
        Ringtone tone = soundUri != null ? RingtoneManager.getRingtone(this, soundUri) : null;
        mRingtonePref.setSummary(tone != null ? tone.getTitle(this)
                : getResources().getString(R.string.silent_ringtone));
    }

    private void setEnabledNotificationsPref() {
        // The "enable notifications" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableNotificationsPref.setChecked(MessagingPreferenceActivity.getNotificationEnabled(this));
    }

    private void setEnabledPrivacyModePref() {
        // The "enable privacy mode" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        boolean isPrivacyModeEnabled = MessagingPreferenceActivity.getPrivacyModeEnabled(this);
        mEnablePrivacyModePref.setChecked(isPrivacyModeEnabled);

        // Enable/Disable the "enable quickmessage" setting according to
        // the "enable privacy mode" setting state
        mEnableQuickMessagePref.setEnabled(!isPrivacyModeEnabled);

        // Enable/Disable the "enable dark theme" setting according to
        // the "enable privacy mode" setting state
        mEnableQmDarkThemePref.setEnabled(!isPrivacyModeEnabled);
    }

    private void setEnabledQuickMessagePref() {
        // The "enable quickmessage" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQuickMessagePref.setChecked(MessagingPreferenceActivity.getQuickMessageEnabled(this));
    }

    private void setEnabledQmLockscreenPref() {
        // The "enable quickmessage on lock screen " setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQmLockscreenPref.setChecked(MessagingPreferenceActivity.getQmLockscreenEnabled(this));
    }

    private void setEnabledQmCloseAllPref() {
        // The "enable close all" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQmCloseAllPref.setChecked(MessagingPreferenceActivity.getQmCloseAllEnabled(this));
    }

    private void setEnabledQmDarkThemePref() {
        // The "Use dark theme" setting is really stored in our own prefs. Read the
        // current value and set the checkbox to match.
        mEnableQmDarkThemePref.setChecked(MessagingPreferenceActivity.getQmDarkThemeEnabled(this));
    }

	public void setupContinue(View v) {
        SharedPreferences.Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefsEditor.putBoolean(PrivacyNoticeStatus, true);
		prefsEditor.commit();
		finish();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		launchDefaultSmsSelector();
	}
	
    private void launchDefaultSmsSelector() {	
    	if (SDSmsManager.isSdsmsDefaultSmsApp() || !SDSmsManager.hasKitKat()) return;
    	if (SDSmsManager.isSdsmsIntstalled()) {
    		SDSmsManager.notifySetDefaultSms(this);
    	} else {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView title = new TextView(this);
            title.setText(getString(R.string.gotxtmeChangeDefSmsTitleTxt));
            //title.setBackgroundColor(Color.DKGRAY);
            title.setPadding(10, 10, 10, 10);
            title.setGravity(Gravity.CENTER);
            title.setTextColor(Color.WHITE);
            title.setBackgroundColor(Color.BLACK);
            title.setTextSize(20);
    		final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
    		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
    		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
			updatesText.setText(Html.fromHtml(getString(R.string.gotxtmeChangeDefSmsDescrTxt)));
			AlertDialog ad = new AlertDialog.Builder(this)
	        .setCustomTitle(title)
	        .setView(helpTextView)
	        .setNeutralButton(getString(R.string.gotxtmeChangeDefSmsChangeTxt), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	dialog.cancel();
	            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
	
	    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
	
	    			//startActivity(intent);
	    			startActivity(intent);
	            }
	        })
	        .setNegativeButton(getString(R.string.gotxtmeChangeDefSmsNoChangeTxt), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	dialog.cancel();
	            }
	        })
	        .create();
			ad.show();
    	}

    }


}