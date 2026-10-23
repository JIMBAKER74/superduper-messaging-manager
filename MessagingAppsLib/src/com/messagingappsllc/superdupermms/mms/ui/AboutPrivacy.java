package com.messagingappsllc.superdupermms.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.messagingappsllc.superdupermms.mms.R;

public class AboutPrivacy extends Activity {

	private CheckBox termsChk;
	private Button rejectBtn, acceptBtn;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
    private LayoutInflater mInflater;
    public static final String PrivacyNoticeStatus = "PrivacyNoticeStatus";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_privacy);
        TextView info = (TextView)findViewById(R.id.aboutPrivacy3Lbl);
        Linkify.addLinks(info, Linkify.WEB_URLS);
        TextView info2 = (TextView)findViewById(R.id.aboutPrivacy4Lbl);
        Linkify.addLinks(info2, Linkify.EMAIL_ADDRESSES|Linkify.WEB_URLS);
        termsChk = (CheckBox)findViewById(R.id.aboutPrivacyAcceptChk);
        acceptBtn = (Button)findViewById(R.id.aboutPrivacyAcceptBtn);
        rejectBtn = (Button)findViewById(R.id.aboutPrivacyRejectBtn);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
		rejectBtn.setEnabled(true);
		acceptBtn.setEnabled(true);
    	editor = settings.edit();
    	if (settings.getBoolean(PrivacyNoticeStatus, false)) {
    		termsChk.setChecked(true);
    	} else {
    		termsChk.setChecked(false);
    	}
    }
	
	public void termsCheck(View v) {
		editor.putBoolean(PrivacyNoticeStatus, termsChk.isChecked());
		editor.commit();
		if (termsChk.isChecked()) {
    		rejectBtn.setEnabled(false);
    		acceptBtn.setEnabled(true);
    		termsChk.setChecked(true);
    	} else {
    		rejectBtn.setEnabled(true);
    		acceptBtn.setEnabled(false);
    		termsChk.setChecked(false);
    	}
	}
	
	public void termsReject(View v) {
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(getString(R.string.privacyRejectConfirmTxt)));
		AlertDialog ad = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.privacyRejectConfirmHeaderTxt))
        .setView(helpTextView)
        .setNeutralButton(getString(R.string.sysYes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	finish();
            }
        })
        .setNegativeButton(getString(R.string.sysNo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();
	}
	
	public void termsAccept(View v) {
		if (termsChk.isChecked()) {
			finish();
		} else {
	        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
			final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
			updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
			updatesText.setText(Html.fromHtml(getString(R.string.privacyAcceptConfirmTxt)));
			AlertDialog ad = new AlertDialog.Builder(this)
	        .setTitle(getString(R.string.privacyAcceptConfirmHeaderTxt))
	        .setView(helpTextView)
	        .setPositiveButton(getString(R.string.privacyAcceptTermsTxt), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	        		editor.putBoolean(PrivacyNoticeStatus, true);
	        		editor.commit();
	            	dialog.cancel();
	            	finish();
	            }
	        })
	        .setNegativeButton(getString(R.string.sysNo), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            	dialog.cancel();
	            }
	        })
	        .create();
			ad.show();
		}
	}
}