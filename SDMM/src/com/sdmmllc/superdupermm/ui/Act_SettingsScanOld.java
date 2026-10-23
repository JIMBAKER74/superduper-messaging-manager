package com.sdmmllc.superdupermm.ui;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.Fields;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDB.ScanListListener;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDAppListUpdateAdapter;
import com.sdmmllc.superdupermm.scan.SDScanAdapter;
import com.sdmmllc.superdupermm.scan.SDScanListener;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupermm.scan.SDScanManager.AppListComparator;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class Act_SettingsScanOld extends Activity {

    private static final String TAG = "Act_AppList";
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
    private Button startScanBtn, nextBtn, nonMessagingCountBtn, messagingCountBtn, fullFeaturedMessagingBtn;
    private TextView 
    	scanTitleTxt, scanDescrTxt, progressBarLbl, scanDisplayTitle, scanDisplayMsg;
    private int scanCountTotal = 0, scanCountUpdate = 0;
    private ProgressBar progressBar, progressSpinner;
    private int waittime = 0, totalwait = 3;
    private RelativeLayout startMsgLayout, resultsLayout;

	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);

    private ScanUpdateListener mScanUpdateListener;
    private ScanListener scanListener;
    
    public class ScanListener extends SDScanAdapter {
        @Override
		public void scanComplete(String pkgName, String action) {
            runOnUiThread(updateScanInfo);
        }

        @Override
        public void compilingResults() {
            runOnUiThread(updateScanInfo);
        }

		@Override
		public void updateScanProgress(int scanItemsTotal,
				int scanItemsCompleted) {
        	if (SdmmsConsts.DEBUG_SCAN) 
				Log.i(TAG, "ScanUpdateListener: scanItemsTotal:" + scanItemsTotal
					+ " - scanItemsCompleted: " + scanItemsCompleted);
    		if (scanCountTotal != scanItemsTotal) {
    			scanCountTotal = scanItemsTotal;
   				progressBar.setMax(scanCountTotal);
    		}
    		if (scanCountUpdate != scanItemsCompleted) {
    			scanCountUpdate = scanItemsCompleted;
   				runOnUiThread(updateScanProgress);
    		}
		}
    }
    
    public class ScanUpdateListener extends SDAppListUpdateAdapter {
    	@Override
    	public void updateScanProgress(int scanItemsTotal, int scanItemsCompleted) {
//        	if (SpaTextConsts.debugLevel > 7) 
				Log.i(TAG, "ScanUpdateListener: scanItemsTotal:" + scanItemsTotal
					+ " - scanItemsCompleted: " + scanItemsCompleted);
    		if (scanCountTotal != scanItemsTotal) {
    			scanCountTotal = scanItemsTotal;
   				progressBar.setMax(scanCountTotal);
    		}
    		if (scanCountUpdate != scanItemsCompleted) {
    			scanCountUpdate = scanItemsCompleted;
   				runOnUiThread(updateScanProgress);
    		}
    	}
    }

	private ScanListListener scanListListener = new ScanListListener() {

		@Override
		public void onScanUpdate() {
			runOnUiThread(updateUI);
		}
    	
    };
    
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
        setContentView(R.layout.act_settings_scan_old);
        spaContext = this;
        //userAccount = SpaUserAccount.getUserAccount(spaContext);
        scanManager = SDScanManager.getScanManager(this);
        spaDB = SDSmsManagerApp.getSDDB();
        scanList = SDSmsManagerApp.getInstalledPackages();
        scanListId = scanList.scanID;
        
        startScanBtn = (Button)findViewById(R.id.initialScanBtn);
        scanTitleTxt = (TextView)findViewById(R.id.initialScanTitle);
        scanDescrTxt = (TextView)findViewById(R.id.initialScanDescr);
        scanDisplayTitle = (TextView)findViewById(R.id.initialScanViewStartTitle);
        scanDisplayMsg = (TextView)findViewById(R.id.initialScanViewStartDescr);
        progressBar = (ProgressBar)findViewById(R.id.initialScanProgress);
        progressBarLbl = (TextView)findViewById(R.id.initialScanProgressDescr);
        progressSpinner = (ProgressBar)findViewById(R.id.initialScanProgressSpinner);
        
        nonMessagingCountBtn = (Button)findViewById(R.id.initialScanViewNonMessagingAppsBtn);
        messagingCountBtn = (Button)findViewById(R.id.initialScanViewMessagingAppsBtn);
        fullFeaturedMessagingBtn = (Button)findViewById(R.id.initialScanViewDefaultMessagingAppsBtn);
        
        startMsgLayout = (RelativeLayout)findViewById(R.id.initialScanViewStartLayout);
        resultsLayout = (RelativeLayout)findViewById(R.id.initialScanViewResultsAppsLayout);
        
        nextBtn = (Button)findViewById(R.id.initialScanViewNextBtn);
        
        resultsLayout.setVisibility(View.GONE);

        mInflater = (LayoutInflater) spaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        scanListener = new ScanListener();
        if (!scanManager.scanInProgress()) {
        	progressBar.setVisibility(View.GONE);
        	progressBarLbl.setVisibility(View.GONE);
        	progressSpinner.setVisibility(View.GONE);
        	nextBtn.setVisibility(View.GONE);
        }
    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
    }

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
    	spaDB.setScanListListener(scanListListener);
        scanManager.addScanListener(scanListener);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		runOnUiThread(updateUI);
    }
    
    public void viewInitialAppSelector(View v) {
    	//startActivityForResult(new Intent(this, Act_InitialAppSelector.class), SdmmsConsts.INITIAL_SELECT_SCREEN);
    	finish();
    }

    @Override
    protected void onStop(){
        FlurryAgent.onEndSession(spaContext);
    	spaDB.removeScanListListener();
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

    public void scanNow(View v) {
        FlurryAgent.logEvent("SCAN STARTED");
        startScanBtn.setEnabled(false);
        startScanBtn.setText(getString(R.string.initialScanBtnTxtScanning));
        scanDescrTxt.setText(getString(R.string.initialScanDescrTxt2));
//        waitDialog = ProgressDialog.show(spaContext, "Scanning Apps",
//                "Please wait...", true);
    	scanCountTotal = 0;
    	scanCountUpdate = 0;
		progressBar.setMax(scanCountTotal);
        showScanProgress();
        Thread thread = new Thread(null, scanApps, "ScanningApps");
        thread.start();
    }

	public void showScanProgress() {
		startScanBtn.setVisibility(View.GONE);
		progressSpinner.setVisibility(View.VISIBLE);
		if (scanCountTotal > 0) {
			progressBar.setMax(scanCountTotal);
			progressBar.setVisibility(View.VISIBLE);
			if (scanCountUpdate > 0) progressBar.setProgress(scanCountUpdate);
		}
	}

    private Runnable updateScanInfo = new Runnable() {
        @Override
        public void run() {
        	startScanBtn.setEnabled(false);
        	startScanBtn.setText(getString(R.string.initialScanBtnDoneTxt));
        	scanTitleTxt.setText(getString(R.string.initialScanTitleTxt2));
            scanDescrTxt.setText(getString(R.string.initialScanDescrTxt3));
            scanList = SDSmsManagerApp.getInstalledPackages();
//        	if (SpaTextConsts.debugLevel > 7) 
				Log.i(TAG, "updateScanInfo: current scan ID:" + scanList.scanID);
//    		if (SpaTextConsts.debugLevel > 7) 
				Log.i(TAG, "updateScanInfo: current scan size:" + scanList.size());
	        if (scanList.getNonSMSCount() > 0) {
	        	nonMessagingCountBtn.setEnabled(true);
	        	nonMessagingCountBtn.setText(scanList.getNonSMSCount() + "");
            } else {
            	nonMessagingCountBtn.setEnabled(false);
            	nonMessagingCountBtn.setText(getString(R.string.initialScanBtnNoneTxt));
            }
	        if (scanList.size() - scanList.getNonSMSCount() > 0) {
	        	messagingCountBtn.setEnabled(true);
	        	messagingCountBtn.setText("" + (scanList.size() - scanList.getNonSMSCount()));
            } else {
            	messagingCountBtn.setEnabled(false);
	        	messagingCountBtn.setText(getString(R.string.initialScanBtnNoneTxt));
            }
	        if (scanList.getMessagingCount() > 0) {
	        	fullFeaturedMessagingBtn.setEnabled(true);
	        	fullFeaturedMessagingBtn.setText("" + scanList.getMessagingCount());
	        }
            if(progressBar != null) {
            	progressBarLbl.setVisibility(View.GONE);
            	progressBar.setVisibility(View.GONE);
            }
            if(progressSpinner != null) progressSpinner.setVisibility(View.GONE);
            if (scanManager != null && mScanUpdateListener != null)
            	scanManager.removeSDAppListUpdateListener(mScanUpdateListener);
            resultsLayout.setVisibility(View.VISIBLE);
            startMsgLayout.setVisibility(View.GONE);
        	nextBtn.setVisibility(View.VISIBLE);
        }
    };

    private Runnable scanInProgressInfo = new Runnable() {
        @Override
        public void run() {
        	startScanBtn.setEnabled(false);
            startScanBtn.setText(getString(R.string.initialScanScanBtnProgressTxt));
            scanTitleTxt.setText(getString(R.string.initialScanScanTitleProgressTxt));
            scanDescrTxt.setText(getString(R.string.initialScanScanDescrProgressTxt));
            //waitDialog is null if you start a scan, hit the back button, and then re-enter the activity before the scan completes.
            if(progressBar != null) {
            	scanCountTotal = 0;
            	scanCountUpdate = 0;
    			progressBar.setMax(scanCountTotal);
    			progressBarLbl.setVisibility(View.VISIBLE);
            	progressBar.setVisibility(View.VISIBLE);
            }
        }
    };

    Runnable scanApps = new Runnable() {
        @Override
        public void run() {
            mScanUpdateListener = new ScanUpdateListener();
            scanManager.setSDAppListUpdateListener(mScanUpdateListener);
            scanManager.scanNow();
            waittime = 0;
            totalwait = 300;
            while (!scanManager.isScanComplete() && (waittime < totalwait)) {
                try {
                    wait(1000);
                } catch (Exception e) {
                    // I don't care
                }
                if (scanManager.isScanComplete()) runOnUiThread(updateScanInfo);
                else if (progressBar != null) 
                	runOnUiThread(updateScanProgress);
                waittime++;
            }
            if (!scanManager.isScanComplete()) runOnUiThread(scanInProgressInfo);
        }
    };

	private Runnable updateScanProgress = new Runnable() {
		@Override
		public void run () {
			if (!progressBar.isShown()) progressBar.setVisibility(View.VISIBLE);
			if (scanCountUpdate > 0) {
				if (scanCountUpdate >= scanCountTotal) progressBarLbl.setText(getString(R.string.initialScanScanProgressDescrTxt3));
				else {
					scanDisplayTitle.setText(getString(R.string.initialScanViewStartTitle2Txt));
					scanDisplayMsg.setText(getString(R.string.initialScanViewStartDescr2Txt));
					progressBarLbl.setText(getString(R.string.initialScanScanProgressDescrTxt2));
				}
			}
			progressBar.setProgress(scanCountUpdate);
		}
	};

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

    private void showHelp(){

    }

    private void showAbout() {
        Intent aboutUsIntent = new Intent(spaContext, Act_AboutUs.class);
        startActivity(aboutUsIntent);
    }
}
