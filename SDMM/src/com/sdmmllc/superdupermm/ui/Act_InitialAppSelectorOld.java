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
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.Fields;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDB.ScanListListener;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupermm.scan.SDScanManager.AppListComparator;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;
import com.sdmmllc.superdupersmsmanager.utils.Utils;

public class Act_InitialAppSelectorOld extends Activity {

    String TAG = "Act_AppList";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Context spaContext;
    private ListView scanListView;
    private SDAppList scanList;
    private SDDB spaDB;
    private ArrayList<AppScanInfo> pkgList;
    private Hashtable<String, RelativeLayout> messagingItems = new Hashtable<String, RelativeLayout>();
    private LayoutInflater mInflater;
    private int listType, displayType;
    private int scanListId;
    private SDScanManager scanManager;
    
    private Button otherSMSApps, compatibleMessagingApps, nonCompatibleMessagingApps;
    private RelativeLayout doneBtnLayout;
    
	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);

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
	    	scanList = scanManager.getInstalledPackages();
	        pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayType, listType));
	        Collections.sort(pkgList, AppListComparator.getComparator(AppListComparator.APP_NAME));
	    	if (SdmmsConsts.DEBUG_SCAN_ACT) 
				Log.i(TAG, "updateUI: listener done");
	    	setupMessagingSelector();
	    	setupMarketAppsSelector();
		}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_initial_app_selector);
        spaContext = this;
        scanManager = SDScanManager.getScanManager(this);
        spaDB = SDSmsManagerApp.getSDDB();
        scanList = SDSmsManagerApp.getInstalledPackages();
        scanListId = scanList.scanID;
        settings = getSharedPreferences(SDScanManager.SCAN_PREFS, 0);
		editor = settings.edit();
       
        otherSMSApps = (Button)findViewById(R.id.initialScanAppSelectorFeatureAppsBtn);
        compatibleMessagingApps = (Button)findViewById(R.id.initialScanAppSelectorCompatibleAppsBtn);
        nonCompatibleMessagingApps = (Button)findViewById(R.id.initialScanAppSelectorFeatureAppsBtn);
        doneBtnLayout = (RelativeLayout)findViewById(R.id.initialScanAppSelectorDoneBtnLayout);
        doneBtnLayout.setVisibility(View.GONE);
        
        otherSMSApps.setText(scanList.getSMSOtherCount() + "");
        compatibleMessagingApps.setText(scanList.getMessagingCompatibleCount() + "");
        nonCompatibleMessagingApps.setText(scanList.getMessagingNonCompatibleCount() + "");
        
        if (scanList.getSMSOtherCount() < 1) otherSMSApps.setEnabled(false);
        if (scanList.getMessagingCompatibleCount() < 1) compatibleMessagingApps.setEnabled(false);
        if (scanList.getMessagingNonCompatibleCount() < 1) nonCompatibleMessagingApps.setEnabled(false);
        
        mInflater = (LayoutInflater) spaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
    }
    
    private void setupMessagingSelector() {
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.initialScanAppSelectorCompatibleAppsIconLayout);
        HorizontalScrollView hr = new HorizontalScrollView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        hr.setLayoutParams(lp);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

        List<AppScanInfo> apps = SDSmsManagerApp.getInstalledPackages().getMessagingCompatibleList();
    	TextView warningTxt = (TextView)findViewById(R.id.initialScanAppSelectorWarning);
    	TextView compatibleTxt = (TextView)findViewById(R.id.initialScanAppSelectorCompatibleDesc);
        if (apps.size() < 1) {
        	warningTxt.setVisibility(View.VISIBLE);
        	compatibleTxt.setVisibility(View.GONE);
        } else {
        	launchDefaultSmsSelector();
        	if (apps.size() == 1) {
				scanManager.setPreferredMessagingApp(apps.get(0).pkgName);
        	}
        	warningTxt.setVisibility(View.GONE);
        	compatibleTxt.setVisibility(View.VISIBLE);
	        for(AppScanInfo scan : apps){
	        	RelativeLayout item = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.app_selector, null);
	        	ImageView img = (ImageView)item.findViewById(R.id.appSelectorCompatibleIcon);
	        	TextView text = (TextView)item.findViewById(R.id.appSelectorCompatibleDesc);
	        	ImageView googlePlay = (ImageView)item.findViewById(R.id.appSelectorGooglePlayIcon);
	        	googlePlay.setVisibility(View.GONE);
	        	try {
					img.setImageDrawable(getPackageManager().getApplicationIcon(scan.pkgName));
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            text.setText(scan.appName);
	            text.setWidth(img.getWidth());
	            item.setClickable(true);
	            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
	            		((int) this.getResources().getDimension(R.dimen.launcher_size)), 
	            		LayoutParams.WRAP_CONTENT);
	            llp.setMargins(10, 10, 10, 10);

	            final String pkgName = scan.pkgName;
	            if (pkgName.equals(settings.getString(SDScanManager.PRIMARY_MESSAGING_APP, ""))) {
	            	item.setBackgroundColor(getResources().getColor(R.color.selectorItemHighlight));
	            	doneBtnLayout.setVisibility(View.VISIBLE);
	            	editor.putBoolean(SdmmsConsts.VIEWED_SETUP, true);
	            	editor.commit();
	            } else {
	            	item.setBackgroundColor(getResources().getColor(R.color.selectorItemBackground));
	            }
	            
	            item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						scanManager.setPreferredMessagingApp(pkgName);
						resetHighlight(pkgName);
					}
	            });
	            layout.addView(item, llp);
	            messagingItems.put(scan.pkgName, item);
	        }
	        hr.addView(layout);
	
	        mainLayout.addView(hr);
        }
    }
    
    private void resetHighlight(String selectedPkg) {
    	for (String pkgName : messagingItems.keySet()) {
    		if (selectedPkg.equals(pkgName)) {
    			messagingItems.get(pkgName)
    				.setBackgroundColor(getResources().getColor(R.color.selectorItemHighlight));
    			doneBtnLayout.setVisibility(View.VISIBLE);
            	editor.putBoolean(SdmmsConsts.VIEWED_SETUP, true);
            	editor.commit();
           } else {
            	messagingItems.get(pkgName)
            		.setBackgroundColor(getResources().getColor(R.color.selectorItemBackground));
            }
    	}
    }
    
    public void compatibleAppsList(View v) {
    	//TODO
    	// go to list of recommended apps
    }
    
    public void setupDone(View v) {
    	setResult(RESULT_OK);
    	finish();
    }

    private void setupMarketAppsSelector() {
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.initialScanAppSelectorMarketAppsLayout);
        HorizontalScrollView hr = new HorizontalScrollView(this);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        hr.setLayoutParams(lp);

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

        String[] appPackages = getResources().getStringArray(R.array.appPackages);
        String[] appNames = getResources().getStringArray(R.array.appNames);
        final String[] appURL = getResources().getStringArray(R.array.appURL);
        TypedArray appIcons = getResources().obtainTypedArray(R.array.appIcons);

        for(int ii = 0; ii < appPackages.length; ii++) {
        	RelativeLayout item = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.app_selector, null);
        	ImageView googlePlay = (ImageView)item.findViewById(R.id.appSelectorGooglePlayIcon);
        	ImageView img = (ImageView)item.findViewById(R.id.appSelectorCompatibleIcon);
        	TextView text = (TextView)item.findViewById(R.id.appSelectorCompatibleDesc);
        	googlePlay.setImageResource(R.drawable.img_play_badge);
        	googlePlay.setVisibility(View.VISIBLE);
			
        	img.setImageDrawable(appIcons.getDrawable(ii));
            text.setText(appNames[ii]);
            item.setClickable(true);
            final String url = appURL[ii];
            
            item.setBackgroundColor(getResources().getColor(R.color.selectorItemBackground));

            item.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
							Uri.parse(url));
					startActivity(browserIntent);				
				}
            });
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
            		((int) this.getResources().getDimension(R.dimen.launcher_size)) + 20, 
            		LayoutParams.WRAP_CONTENT);
            llp.setMargins(10, 10, 10, 10);
            
            layout.addView(item, llp);
        }
        hr.addView(layout);

        mainLayout.addView(hr);
    }

    private void launchDefaultSmsSelector() {	
    	if (Utils.isDefaultSmsApp(this)) return;
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(getString(R.string.homeAppSelectorChangeDefSmsDescrTxt)));
		AlertDialog ad = new AlertDialog.Builder(this)
        .setTitle(getString(R.string.homeAppSelectorChangeDefSmsTxt))
        .setView(helpTextView)
        .setNeutralButton(getString(R.string.homeAppSelectorChangeDefSmsChangeTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());

    			//startActivity(intent);
    			startActivityForResult(intent, SdmmsConsts.CHANGE_DEF_SMS_SCREEN);
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

    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
    	spaDB.setScanListListener(scanListListener);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		runOnUiThread(updateUI);
    }

    @Override
    protected void onStop(){
        FlurryAgent.onEndSession(spaContext);
    	spaDB.removeScanListListener();
        super.onStop();
    }
    
    public void scanNonCompatibleFullFeaturedAppsView(View v) {
        FlurryAgent.logEvent("View scan list: NonCompatible Full featured SMS Apps");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.TYPE_MESSAGING | AppScanInfo.TYPE_UNREGISTERED);
    }

    public void scanCompatibleFullFeaturedAppsView(View v) {
        FlurryAgent.logEvent("View scan list: Compatible Full featured SMS Apps");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.TYPE_MESSAGING | AppScanInfo.TYPE_REGISTERED_APP);
    }

    public void scanOtherAppsView(View v) {
        FlurryAgent.logEvent("View scan list: Other SMS Apps");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.SMS_OTHER);
    }

    public void viewScanList(int display, int list) {
        Intent viewScanListIntent = new Intent(spaContext, Act_AppList.class);
        viewScanListIntent.putExtra(SDAppList.DISPLAY_OPTION, display);
        viewScanListIntent.putExtra(SDAppList.SCAN_OPTION, list);
        startActivityForResult(viewScanListIntent, SdmmsConsts.SCANLIST_SCREEN);
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

    private void showHelp(){

    }

    private void showAbout() {
        Intent aboutUsIntent = new Intent(spaContext, Act_AboutUs.class);
        startActivity(aboutUsIntent);
    }
}
