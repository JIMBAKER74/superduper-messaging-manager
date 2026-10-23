package com.sdmmllc.superdupermm.ui;


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

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.Fields;
import com.placed.client.android.persistent.PlacedAgent;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class Act_Welcome extends Activity {

	public static final String TAG = "Act_Welcome";
	
    private SharedPreferences mPrefs;
	private Context spaContext;
	private CheckBox termsChk;
	private SharedPreferences.Editor editor;
    private LayoutInflater mInflater;
   
    private static final int ABOUT_PRIVACY = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spaContext = this;
        setContentView(R.layout.act_sdmm_welcome_screen);
        
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    	editor = mPrefs.edit();

    	termsChk = (CheckBox)findViewById(R.id.welcomePrivacyAcceptChk);
    	termsChk.setChecked(true);

    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
        PlacedAgent.registerApp(this.getApplicationContext(), getString(R.string.placedId));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
        //FlurryAgent.onPageView();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
    		Intent data) {
    	if (requestCode == ABOUT_PRIVACY) {
        	if (mPrefs.getBoolean(AboutPrivacy.PrivacyNoticeStatus, false)) {
        		termsChk.setChecked(true);
        	} else {
        		termsChk.setChecked(false);
        	}
    	}
    	if (requestCode == SdmmsConsts.INITIAL_SCAN_SCREEN && resultCode == RESULT_OK) {
    		setResult(RESULT_OK);
    		finish();
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    public void viewTerms(View v) {
	 	Intent privacyIntent = new Intent(this, AboutPrivacy.class);
		startActivityForResult(privacyIntent, ABOUT_PRIVACY);
        FlurryAgent.onEndSession(spaContext);
    }

	public void termsCheck(View v) {
		editor.putBoolean(AboutPrivacy.PrivacyNoticeStatus, termsChk.isChecked());
		editor.commit();
		if (termsChk.isChecked()) {
    		termsChk.setChecked(true);
    	} else {
    		termsChk.setChecked(false);
    	}
	}
	
	public void termsAccept(View v) {
		if (termsChk.isChecked()) {
			initialScan(v);
		} else {
	        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
			final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
			updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
			updatesText.setText(Html.fromHtml(getString(R.string.privacyAcceptConfirmTxt)));
			final View view = v;
			AlertDialog ad = new AlertDialog.Builder(this)
	        .setTitle(getString(R.string.privacyAcceptConfirmHeaderTxt))
	        .setView(helpTextView)
	        .setPositiveButton(getString(R.string.privacyAcceptTermsTxt), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	        		editor.putBoolean(AboutPrivacy.PrivacyNoticeStatus, true);
	        		editor.commit();
	            	dialog.cancel();
	    			initialScan(view);
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
	
	public void initialScan(View v) {
    	startActivityForResult(new Intent(this, Act_InitialAppScan.class), SdmmsConsts.INITIAL_SCAN_SCREEN);
    }
    
    @Override
    protected void onStop(){
        FlurryAgent.onEndSession(spaContext);
        super.onStop();
    }
}
