package com.sdmmllc.superdupermm.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.Fields;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.MessagingServiceAdapter;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDScanAdapter;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupermm.scan.SDScanManager.AppListComparator;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class Act_InitialAppVerify extends Activity {

    private static final String TAG = "Act_InitialAppVerify";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Context spaContext;
    private SDAppList scanList;
    private SDDB spaDB;
    private ArrayList<AppScanInfo> pkgList;
    private LayoutInflater mInflater;
    
    // display option is either app CATEGORY or app TYPE
    private int listType, displayOption;
    private int scanListId;
    private SDScanManager scanManager;
    private Button nextBtn, verifyBtn;
    private TextView 
    	scanTitleTxt, scanDescrTxt;
    private LinearLayout resultsLayout;
    private String[] appPackages, appNames, appURL;
    private TypedArray appIcons;
    private Hashtable<String, LinearLayout> scanItems = new Hashtable<String, LinearLayout>();

	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);

    private ScanListener scanListener;
    
    public class ScanListener extends SDScanAdapter {
        @Override
		public void scanComplete(String pkgName, String action) {
           runOnUiThread(updateScanInfo);
        }

        @Override
        public void compilingResults() {
        	// do nothing
        }

		@Override
		public void updateScanProgress(int scanItemsTotal,
				int scanItemsCompleted) {
			// do nothing
		}
    }
    
    private Runnable updateUI = new Runnable() {
		@Override
		public void run() {
			if (SdmmsConsts.DEBUG_SCAN_ACT) 
    			Log.i(TAG, "updateUI: listener called");
	    	scanList = scanManager.getScanList(scanListId);
	        pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, listType));
	        Collections.sort(pkgList, AppListComparator.getComparator(AppListComparator.APP_NAME));
	    	if (SdmmsConsts.DEBUG_SCAN_ACT) 
				Log.i(TAG, "updateUI: listener done");
		}
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_verify_apps);
        spaContext = this;

        appPackages = getResources().getStringArray(R.array.appPackages);
        appNames = getResources().getStringArray(R.array.appNames);
        appURL = getResources().getStringArray(R.array.appURL);
        appIcons = getResources().obtainTypedArray(R.array.appIcons);

        //userAccount = SpaUserAccount.getUserAccount(spaContext);
        scanManager = SDScanManager.getScanManager(this);
        spaDB = SDSmsManagerApp.getSDDB();
        scanList = SDSmsManagerApp.getInstalledPackages();
        scanListId = scanList.scanID;
        
        scanTitleTxt = (TextView)findViewById(R.id.verifyAppsTitle);
        scanDescrTxt = (TextView)findViewById(R.id.verifyAppsDescr);
        resultsLayout = (LinearLayout)findViewById(R.id.verifyAppsViewResultsAppsLayout);
        
        verifyBtn = (Button)findViewById(R.id.verifyAppsBtn);
        nextBtn = (Button)findViewById(R.id.verifyAppsViewNextBtn);
        nextBtn.setEnabled(false);
        
        resultsLayout.setVisibility(View.GONE);
        resultsLayout.setBackgroundColor(getResources().getColor(R.color.sdmmWhite));

        mInflater = (LayoutInflater) spaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setupAppsDisplay();
    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
        scanManager.addScanListener(scanListener);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		runOnUiThread(updateUI);
    }
    
    public void nextBtn(View v) {
    	setResult(RESULT_OK);
    	finish();
    }
    
    public void verifyBtn(View v) {
        Thread thread = new Thread(null, testApps, "testingApps");
        thread.start();
    }
    
    private Runnable testApps = new Runnable() {
		@Override
		public void run() {
	    	SDSmsManagerApp.testRegisteredApps(verifyConnection);
	    	runOnUiThread(finishAppsTest);
		}
    };

    private Runnable finishAppsTest = new Runnable() {
		@Override
		public void run() {
			for (String app: scanItems.keySet()) {
				LinearLayout item = scanItems.get(app);
				if (!((CheckBox)item.findViewById(R.id.scanItemChkBox)).isChecked()) {
					Button btn = (Button)item.findViewById(R.id.scanItemInstallBtn);
					btn.setEnabled(true);
					btn.setVisibility(View.VISIBLE);
				}
			}
	        nextBtn.setEnabled(true);
	        verifyBtn.setEnabled(false);
		}
    };

    @Override
    protected void onStop(){
        FlurryAgent.onEndSession(spaContext);
        scanManager.removeScanListener(scanListener);
        super.onStop();
    }

    public void scanSafeView(View v) {
        FlurryAgent.logEvent("View scan list: Non-SMS Apps");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.SMS_NONE);
    }

    public void viewScanList(int display, int type) {
        Intent viewScanListIntent = new Intent(spaContext, Act_AppList.class);
        viewScanListIntent.putExtra(SDAppList.DISPLAY_OPTION, display);
        viewScanListIntent.putExtra(SDAppList.SCAN_OPTION, type);
        startActivityForResult(viewScanListIntent, SdmmsConsts.SCANLIST_SCREEN);
    }

    private Runnable updateScanInfo = new Runnable() {
        @Override
        public void run() {
        	scanTitleTxt.setText(getString(R.string.verifyAppsTitleTxt));
            scanDescrTxt.setText(getString(R.string.verifyAppsDescrTxt));
            resultsLayout.setVisibility(View.VISIBLE);
            resultsLayout.setBackgroundColor(getResources().getColor(R.color.sdmmWhite));
        	nextBtn.setVisibility(View.VISIBLE);
        	setupAppsDisplay();
        }
    };
    
	private void setupAppsDisplay() {
		List<AppScanInfo> displayList = scanList.getRegisteredList();

		for(AppScanInfo nextApp : displayList) {
			PackageInfo p;
			try {
				LinearLayout item = (LinearLayout)this.getLayoutInflater().inflate(R.layout.sdmm_scanitem_chk, null);
				ImageView img = (ImageView)item.findViewById(R.id.scanItemIconImg);
				TextView titleTxt = (TextView)item.findViewById(R.id.scanItemTitle);
				TextView descrTxt = (TextView)item.findViewById(R.id.scanItemDescr);
				CheckBox itemChk = (CheckBox)item.findViewById(R.id.scanItemChkBox);
				Button itemBtn = (Button)item.findViewById(R.id.scanItemInstallBtn);
				
				item.setBackgroundColor(getResources().getColor(R.color.sdmmRed));
				itemChk.setEnabled(false);
				
				itemBtn.setEnabled(true);
				itemBtn.setText(getString(R.string.verifyAppsAppUpdateBtnTxt));
				itemBtn.setVisibility(View.GONE);
				final String pkgName = nextApp.pkgName;
	            itemBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
								Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
						startActivity(browserIntent);				
					}
	            });

	            p = spaContext.getPackageManager().getPackageInfo(nextApp.pkgName, 0);
				img.setImageDrawable(p.applicationInfo.loadIcon(spaContext.getPackageManager()));
				String title = nextApp.appName;
				if (nextApp.isTypeSecure()) {
					TextView secureTxt = (TextView)item.findViewById(R.id.scanItemSecureDescr);
					secureTxt.setVisibility(View.VISIBLE);
					secureTxt.setTextColor(getResources().getColor(R.color.sdmmRed));
					if (nextApp.pkgName.startsWith("com.secondphoneapps.SecondPhone"))
						secureTxt.setText(getString(R.string.verifyAppsSecureDescrHMTMsg));
					else secureTxt.setText(getString(R.string.verifyAppsSecureDescrMsg));
				}
				titleTxt.setText(nextApp.appName);
				descrTxt.setText(nextApp.pkgName);
	
	            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
	            		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	            llp.setMargins(10, 10, 10, 10);
	            
	            resultsLayout.addView(item, llp);
	            
	            scanItems.put(nextApp.pkgName, item);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        resultsLayout.setVisibility(View.VISIBLE);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.as_default_menu, menu);
        menu.findItem(R.id.home).setVisible(true);
        menu.findItem(R.id.help).setVisible(false);
        menu.findItem(R.id.about).setVisible(true);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                return true;
            case R.id.logout:
                return true;
            case R.id.home:
                Intent homeIntent = new Intent(spaContext, Act_Home.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                return true;
            case R.id.help:
                showHelp();
                return true;
            case R.id.about:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("requestCode = %s resultCode = %s",
                requestCode, resultCode));
        if (requestCode == SdmmsConsts.SCANLIST_SCREEN) runOnUiThread(updateScanInfo);
        if (requestCode == SdmmsConsts.INITIAL_SELECT_SCREEN &&
        		resultCode == RESULT_OK) {
        	setResult(RESULT_OK);
        	finish();
        }
    }

    private void showHelp() {

    }

    private void showAbout() {
        Intent aboutUsIntent = new Intent(spaContext, Act_AboutUs.class);
        startActivity(aboutUsIntent);
    }
    
    private class UpdateAppStatus implements Runnable {
    	String pkgNameUpdate, pkgResponseUpdate;
    	boolean pkgStatus = false;
    	
    	public UpdateAppStatus (String pkg, String resp, boolean stat) {
    		pkgNameUpdate = pkg;
    		pkgResponseUpdate = resp;
    		pkgStatus = stat;
    	}
    	
		@Override
		public void run() {
	    	if (!scanItems.containsKey(pkgNameUpdate)) return;
	    	LinearLayout item = scanItems.get(pkgNameUpdate);
			((CheckBox)item.findViewById(R.id.scanItemChkBox)).setChecked(pkgResponseUpdate.length() > 0);
			if (pkgStatus) {
				item.setBackgroundColor(getResources().getColor(R.color.sdmmBgGray));
				if (pkgResponseUpdate.length() < 1) ((TextView)item.findViewById(R.id.scanItemDescr)).setText(spaContext.getString(R.string.verifyAppsVerificationSent));
				else ((TextView)item.findViewById(R.id.scanItemDescr)).setText(pkgResponseUpdate);
			}
			if (pkgResponseUpdate.equals(SDSmsManager.TEST_UPDATE_REQUIRED)) {
				//((Button)item.findViewById(R.id.scanItemInstallBtn)).setVisibility(View.VISIBLE);
				//((TextView)item.findViewById(R.id.scanItemDescr)).setText(spaContext.getString(R.string.verifyAppsUpdateApp));
			}
			item.invalidate();
		}    	
    	
    }
    
    private MessagingServiceAdapter verifyConnection = new MessagingServiceAdapter() {
    	@Override
    	public void testSent(String pkgName) {
    		Log.i(TAG, " test sent to package: " + pkgName);
    		runOnUiThread(new UpdateAppStatus(pkgName, "", true));
    	}

    	@Override
    	public void testResponse(String pkgName, String response) {
    		Log.i(TAG, " test response from package: " + pkgName);
    		runOnUiThread(new UpdateAppStatus(pkgName, response, true));
    	}
    };
}
