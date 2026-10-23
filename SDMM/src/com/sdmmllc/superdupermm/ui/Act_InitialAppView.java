package com.sdmmllc.superdupermm.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
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
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDScanAdapter;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupermm.scan.SDScanManager.AppListComparator;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;
import com.sdmmllc.superdupersmsmanager.utils.Utils;

public class Act_InitialAppView extends Activity {

    private static final String TAG = "Act_InitialAppView";
    private Context spaContext;
    private SDAppList scanList;
    private ArrayList<AppScanInfo> pkgList;
    private LayoutInflater mInflater;
    
    // display option is either app CATEGORY or app TYPE
    private int listType, displayOption;
    private int scanListId;
    private SDScanManager scanManager;
    private Button nextBtn;
    private TextView 
    	scanTitleTxt, scanDescrTxt;
    private List<CheckBox> checkBoxes = new ArrayList<CheckBox>();
    private LinearLayout resultsLayout;
    private String[] appPackages, appNames, appURL;
    private TypedArray appIcons;
    private AlertDialog warnNotSupportedApp;
    private Hashtable<String, LinearLayout> scanItems = new Hashtable<String, LinearLayout>();

	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, 
			java.text.DateFormat.SHORT);

    private ScanListener scanListener;
    
    public class ScanListener extends SDScanAdapter {
        @Override
		public void scanComplete(String pkgName, String action) {
			if (SdmmsConsts.DEBUG_VIEW_ACT) 
				Log.i(TAG, " scanListener complete on package: " + pkgName);
    		runOnUiThread(new UpdateAppStatus(pkgName, action, true));
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
			if (SdmmsConsts.DEBUG_VIEW_ACT) 
    			Log.i(TAG, "updateUI: listener called");
	    	scanList = scanManager.getScanList(scanListId);
	        pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, listType));
	        Collections.sort(pkgList, AppListComparator.getComparator(AppListComparator.APP_NAME));
	    	if (SdmmsConsts.DEBUG_VIEW_ACT) 
				Log.i(TAG, "updateUI: listener done");
		}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_initial_app_view);
        spaContext = this;
        mInflater = this.getLayoutInflater();
        
        //userAccount = SpaUserAccount.getUserAccount(spaContext);
        scanManager = SDScanManager.getScanManager(this);
        scanList = SDSmsManagerApp.getInstalledPackages();
        scanListId = scanList.scanID;

        appPackages = getResources().getStringArray(R.array.appPackages);
        appNames = getResources().getStringArray(R.array.appNames);
        appURL = getResources().getStringArray(R.array.appURL);
        appIcons = getResources().obtainTypedArray(R.array.appIcons);

        scanTitleTxt = (TextView)findViewById(R.id.initialAppViewTitle);
        scanDescrTxt = (TextView)findViewById(R.id.initialAppViewDescr);
        resultsLayout = (LinearLayout)findViewById(R.id.initialAppViewViewResultsAppsLayout);
        
        nextBtn = (Button)findViewById(R.id.initialAppViewViewNextBtn);
        nextBtn.setEnabled(false);
        
        resultsLayout.setVisibility(View.GONE);

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
        checkForInstalls();
    }
    
    public void viewInitialAppSelector(View v) {
    	startActivityForResult(new Intent(this, Act_InitialAppSelector.class), 
    			SdmmsConsts.INITIAL_SELECT_SCREEN);
    }

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
        	scanTitleTxt.setText(getString(R.string.initialAppViewTitleTxt));
            scanDescrTxt.setText(getString(R.string.initialAppViewDescrTxt));
            resultsLayout.setVisibility(View.VISIBLE);
            resultsLayout.setBackgroundColor(getResources().getColor(R.color.sdmmRed));
        	nextBtn.setVisibility(View.VISIBLE);
        	setupAppsDisplay();
        }
    };

	private void setupAppsDisplay() {
		if (scanList.getMessagingCompatibleCount() < 1) setupMarket();

		List<AppScanInfo> displayList = scanList.getMessaging();
		
		for(AppScanInfo nextApp : displayList) {
			if (SdmmsConsts.DEBUG_VIEW_ACT) 
				Log.i(TAG, "setting line for nextApp:" + nextApp.pkgName);
			PackageInfo p;
			try {
				LinearLayout item = (LinearLayout)mInflater.inflate(R.layout.sdmm_scanitem_chk, null);
				ImageView img = (ImageView)item.findViewById(R.id.scanItemIconImg);
				TextView titleTxt = (TextView)item.findViewById(R.id.scanItemTitle);
				TextView descrTxt = (TextView)item.findViewById(R.id.scanItemDescr);
				Button installBtn = (Button)item.findViewById(R.id.scanItemInstallBtn);
				final CheckBox selectChk = (CheckBox)item.findViewById(R.id.scanItemChkBox);
				item.setBackgroundColor(getResources().getColor(R.color.sdmmBgGray));
				
				p = spaContext.getPackageManager().getPackageInfo(nextApp.pkgName, 0);
				img.setImageDrawable(p.applicationInfo.loadIcon(spaContext.getPackageManager()));
				titleTxt.setText(nextApp.appName);
	
	            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
	            		LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	            llp.setMargins(10, 15, 10, 15);

	            if (nextApp.isTypeRegistered()) {
	            	titleTxt.setTextColor(getResources().getColor(R.color.sdmmWhite));
		            if (scanList.hasPackage(nextApp.pkgName)) {
						selectChk.setButtonDrawable(R.drawable.checkbox_download_selector);
		            	selectChk.setEnabled(true);
		            	selectChk.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								
								if (selectChk.isChecked()) {
									for (CheckBox check : checkBoxes){
										if (check != v && check.isChecked()) check.setChecked(false);
									}
									nextBtn.setEnabled(true);
								} else {
									nextBtn.setEnabled(false);
								}
							}
			            });
			            checkBoxes.add(selectChk);
		            	installBtn.setVisibility(View.GONE);
						descrTxt.setText(getString(R.string.initialAppViewAppSelectDescrTxt));
						descrTxt.setTextColor(getResources().getColor(R.color.sdmmGreen));
		            } else {
		            	selectChk.setVisibility(View.GONE);
		            	final String pkgName = nextApp.pkgName;
		            	item.setClickable(true);
			            item.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
										Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
								startActivity(browserIntent);				
							}
			            });
		            	installBtn.setBackground(getResources().getDrawable(R.drawable.btn_play));
						descrTxt.setText(getString(R.string.initialAppViewAppDownloadDescrTxt));
						descrTxt.setTextColor(getResources().getColor(R.color.sdmmBlue));
		            }

					if (nextApp.isTypeSecure()) {
						TextView secureTxt = (TextView)item.findViewById(R.id.scanItemSecureDescr);
						secureTxt.setVisibility(View.VISIBLE);
						secureTxt.setTextColor(getResources().getColor(R.color.sdmmRed));
						if (nextApp.pkgName.startsWith("com.secondphoneapps.SecondPhone"))
							secureTxt.setText(getString(R.string.verifyAppsSecureDescrHMTMsg));
						else secureTxt.setText(getString(R.string.verifyAppsSecureDescrMsg));
					}
	            } else {
	            	titleTxt.setTextColor(getResources().getColor(R.color.sdmmWhite));
	            	item.setClickable(true);
		            item.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							warnNotSupported();
						}
		            });
		            installBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							warnNotSupported();
						}
		            });
					installBtn.setVisibility(View.VISIBLE);
					installBtn.setText(getString(R.string.initialAppViewInstallBtnDetailsTxt));
					selectChk.setButtonDrawable(R.drawable.checkbox_selector);
	            	selectChk.setEnabled(false);
					descrTxt.setText(getString(R.string.initialAppViewAppNotSupportedDescrTxt));
					descrTxt.setTextColor(getResources().getColor(R.color.sdmmYellow));
	            }

				if (SdmmsConsts.DEBUG_VIEW_ACT) 
					Log.i(TAG, "adding line for nextApp:" + nextApp.pkgName);
	            resultsLayout.addView(item, llp);
	            scanItems.put(nextApp.pkgName, item);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
 
		if (scanList.getMessagingCompatibleCount() > 0) setupMarket();
		
		resultsLayout.setVisibility(View.VISIBLE);
	}
	
	private void setupMarket() {
        for(int ii = 0; ii < appPackages.length; ii++) {
        	if (!scanList.hasPackage(appPackages[ii])) {
				LinearLayout item = (LinearLayout)mInflater.inflate(R.layout.sdmm_scanitem_chk, null);
				ImageView img = (ImageView)item.findViewById(R.id.scanItemIconImg);
				TextView text = (TextView)item.findViewById(R.id.scanItemTitle);
				TextView descrTxt = (TextView)item.findViewById(R.id.scanItemDescr);
				Button googlePlay = (Button)item.findViewById(R.id.scanItemInstallBtn);
				CheckBox selectChk = (CheckBox)item.findViewById(R.id.scanItemChkBox);
	        	googlePlay.setBackgroundResource(R.drawable.img_play_badge);
	        	googlePlay.setVisibility(View.VISIBLE);
				
	        	img.setImageDrawable(appIcons.getDrawable(ii));
	            text.setText(appNames[ii]);
	            text.setTextColor(getResources().getColor(R.color.sdmmWhite));
	            descrTxt.setText(getString(R.string.initialAppViewAppDownloadDescrTxt));
	            descrTxt.setTextColor(getResources().getColor(R.color.sdmmBlue2));

	            item.setBackgroundColor(getResources().getColor(R.color.sdmmBgGray));
	            selectChk.setVisibility(View.GONE);
	
	            final String url = appURL[ii];
	            
	            googlePlay.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
								Uri.parse(url));
						startActivity(browserIntent);				
					}
	            });
	            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
	            		LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	            llp.setMargins(10, 10, 10, 10);
	            
	            resultsLayout.addView(item, llp);
	            scanItems.put(appPackages[ii], item);
        	}
        }
	}

	private void warnNotSupported() {	
		if (Utils.isDefaultSmsApp(this)) return;
		if (warnNotSupportedApp != null && warnNotSupportedApp.isShowing()) return;
		mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(getString(R.string.viewAppsNotSupportedDescrTxt)));
		warnNotSupportedApp = new AlertDialog.Builder(this)
		.setTitle(getString(R.string.viewAppsNotSupportedTitleTxt))
		.setView(helpTextView)
		.setNeutralButton(getString(R.string.sysDone), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		})
		.create();
		warnNotSupportedApp.show();
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
		if (SdmmsConsts.DEBUG_VIEW_ACT) 
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
    
    private void checkForInstalls() {
        scanList = SDSmsManagerApp.getInstalledPackages();
        scanListId = scanList.scanID;
    	for (String pkgName : appPackages) {
    		if (scanList.hasPackage(pkgName)) {
    			if (SdmmsConsts.DEBUG_VIEW_ACT) 
    				Log.i(TAG, "checkForInstalls: checking for installed app: " + pkgName);
    			runOnUiThread(new UpdateAppStatus(pkgName, SDScanManager.ACTION_PACKAGE_ADDED, true));
    		}
    	}
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
			if (SdmmsConsts.DEBUG_VIEW_ACT) 
				Log.i(TAG, "updating installed app info");
			if (!pkgResponseUpdate.equals(SDScanManager.ACTION_PACKAGE_ADDED)) return;
			LinearLayout item = scanItems.get(pkgNameUpdate);
			TextView titleTxt = (TextView)item.findViewById(R.id.scanItemTitle);
			TextView descrTxt = (TextView)item.findViewById(R.id.scanItemDescr);
			Button installBtn = (Button)item.findViewById(R.id.scanItemInstallBtn);
			final CheckBox selectChk = (CheckBox)item.findViewById(R.id.scanItemChkBox);
        	titleTxt.setTextColor(getResources().getColor(R.color.sdmmWhite));
			selectChk.setButtonDrawable(R.drawable.checkbox_download_selector);
        	selectChk.setEnabled(true);
        	selectChk.setVisibility(View.VISIBLE);
        	selectChk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					
					if (selectChk.isChecked()) {
						for (CheckBox check : checkBoxes){
							if (check != v && check.isChecked()) check.setChecked(false);
						}
						nextBtn.setEnabled(true);
					} else {
						nextBtn.setEnabled(false);
					}
				}
            });
            checkBoxes.add(selectChk);
        	installBtn.setVisibility(View.GONE);
			descrTxt.setText(getString(R.string.initialAppViewAppSelectDescrTxt));
			descrTxt.setTextColor(getResources().getColor(R.color.sdmmGreen));
			item.invalidate();
		}    	
    	
    }
}
