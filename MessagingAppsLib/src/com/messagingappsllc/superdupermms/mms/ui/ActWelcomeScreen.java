package com.messagingappsllc.superdupermms.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.messagingappsllc.superdupermms.mms.R;

public class ActWelcomeScreen extends Activity {

	private CheckBox termsChk;
	private Button acceptBtn;
	private SharedPreferences settings;
	private SharedPreferences.Editor editor;
    private LayoutInflater mInflater;
    private Context mContext;
    public static final String PrivacyNoticeStatus = "PrivacyNoticeStatus";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_welcome_screen);
        mContext = this;
        termsChk = (CheckBox)findViewById(R.id.welcomePrivacyAcceptChk);
        acceptBtn = (Button)findViewById(R.id.welcomePrivacyAcceptBtn);
        settings = PreferenceManager.getDefaultSharedPreferences(this);
		acceptBtn.setEnabled(true);
    	editor = settings.edit();
    }
	
	@Override
	public void onResume() {
		super.onResume();
    	termsChk.setChecked(true);
	}
	
	public void termsCheck(View v) {
		editor.putBoolean(PrivacyNoticeStatus, termsChk.isChecked());
		editor.commit();
		if (termsChk.isChecked()) {
    		acceptBtn.setEnabled(true);
    		termsChk.setChecked(true);
    	} else {
    		acceptBtn.setEnabled(false);
    		termsChk.setChecked(false);
    	}
	}
	
	public void termsAccept(View v) {
		if (termsChk.isChecked()) {
            Intent setupScreen = getIntent();
            setupScreen.setClass(mContext, ActSetupScreen.class);
            startActivity(setupScreen);
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
	                Intent setupScreen = getIntent();
	                setupScreen.setClass(mContext, ActSetupScreen.class);
	                startActivity(setupScreen);
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