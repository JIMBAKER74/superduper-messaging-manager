package com.sdmmllc.superdupermm.ui;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.Fields;
import com.millennialmedia.android.MMAdView;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.AdListenerAdapter;
import com.sdmmllc.superdupermm.comms.CommManager;
import com.sdmmllc.superdupermm.comms.CommStatusAdapter;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.scan.SDAppList;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class Act_ViewApps extends Activity {

    private static final String TAG = "Act_ViewApps";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Context spaContext;
    private SDAppList scanList;
    private ArrayAdapter<AppScanInfo> pkgAA;
    private Button fullFeaturedAppsBtn, antispamAppsBtn, pluginAppsBtn, 
    	autoresponderAppsBtn, backupAppsBtn, otherAppsBtn;

    private SDScanManager scanManager;
    private CommStatusAdapter commStatus = new CommStatus();
    private CommManager commManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_view_apps);
        spaContext = this;
        commManager = CommManager.getCommManager(this);
        commManager.addCommStatusListener(this, commStatus);
        scanList = SDSmsManagerApp.getInstalledPackages();
//    	if (SpaTextConsts.debugLevel > 7) 
			Log.i(TAG, "onCreate: current scan ID:" + scanList.scanID);
//    	if (SpaTextConsts.debugLevel > 7) 
			Log.i(TAG, "onCreate: current scan size:" + scanList.size());
        scanManager = SDSmsManagerApp.getScanManager();
    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
    	
    	fullFeaturedAppsBtn = (Button)findViewById(R.id.viewAppsFullBtn);
    	antispamAppsBtn = (Button)findViewById(R.id.viewAppsAntispamBtn);
    	pluginAppsBtn = (Button)findViewById(R.id.viewAppsPluginBtn);
    	autoresponderAppsBtn = (Button)findViewById(R.id.viewAppsResponderBtn);
    	backupAppsBtn = (Button)findViewById(R.id.viewAppsBackupBtn);
    	otherAppsBtn = (Button)findViewById(R.id.viewAppsOtherBtn);
    	
    	enableButtons();
    }


    @Override
    public void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    
    private void enableButtons() {
    	if (scanList.getMessagingCompatibleCount() < 1) fullFeaturedAppsBtn.setEnabled(false);
    	antispamAppsBtn.setEnabled(false);
    	pluginAppsBtn.setEnabled(false);
    	autoresponderAppsBtn.setEnabled(false);
    	backupAppsBtn.setEnabled(false);
    	otherAppsBtn.setEnabled(true);
    }
    
    public void viewCompatibleSMSApps(View v) {
        FlurryAgent.logEvent("View scan list: SMS");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.TYPE_MESSAGING | AppScanInfo.TYPE_REGISTERED_APP);
    }

    private void adSetup() {
    	//HashTable<String, String> map = new HashTable<String, String>();
    	//MMAdView adView = new MMAdView(this, getString(R.string.mmScanAdID), MMAdView.BANNER_AD_TOP, 30, null);
    	//adView.setId(MMAdViewSDK.DEFAULT_VIEWID);
    	//adView.setListener(new AdListener());
    	//LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    	//LinearLayout layout = (LinearLayout)findViewById(R.id.asScanAd);
    	//layout.addView(adView, lp);
    }
    
    public class AdListener extends AdListenerAdapter {
    	@Override
    	public void MMAdReturned(MMAdView ad) {
    		runOnUiThread(removeView);
    	}
    }
    
    public Runnable removeView = new Runnable() {
		@Override
		public void run() {
			((TextView)findViewById(R.id.viewAppsAdBuffer)).setVisibility(View.GONE);
		}
    };
    
    public void viewNonSMSApps(View v) {
        FlurryAgent.logEvent("View scan list: Non-SMS");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.SMS_NONE);
    }

    public void viewSecondarySMSApps(View v) {
        FlurryAgent.logEvent("View scan list: Secondary SMS");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.SMS_OTHER);
    }

    public void viewMessagingApps(View v) {
        FlurryAgent.logEvent("View scan list: Messaging Apps");
        viewScanList(SDAppList.DISPLAY_TYPE, AppScanInfo.TYPE_MESSAGING);
    }

    public void viewScanList(int display_option, int type) {
        Intent viewScanListIntent = new Intent(spaContext, Act_AppList.class);
        viewScanListIntent.putExtra(SDAppList.DISPLAY_OPTION, type);
        viewScanListIntent.putExtra(SDAppList.SCAN_OPTION, type);
        startActivityForResult(viewScanListIntent, SdmmsConsts.SCANLIST_SCREEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, String.format("requestCode = %s resultCode = %s",
                requestCode, resultCode));
    }

    @Override
    public void onStop() {
        FlurryAgent.onEndSession(spaContext);
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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

    private void showAbout() {
        Intent aboutUsIntent = new Intent(spaContext, Act_AboutUs.class);
        startActivity(aboutUsIntent);
    }

    private void showHelp(){

    }
    private class CommStatus extends CommStatusAdapter {

        @Override
        public void genericError(String title, String message) {
            //			if (SpaTextConsts.debugLevel > 9)
            Log.e(TAG, title + ": " + message);
            AlertDialog.Builder dialog = new AlertDialog.Builder(spaContext);
            dialog.setIcon(android.R.drawable.ic_dialog_alert);
            dialog.setCancelable(true);
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.setNegativeButton(
                    getString(R.string.sysOK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog,
                                    int which) {
                    dialog.cancel();
                }
            });
            dialog.show(); //Show the AlertDialog

        }

        @Override
        public void noNetwork(String title, String message) {
            genericError(title, message);
        }

        @Override
        public void commError(String title, String message) {
            genericError(title, message);

        }

        @Override
        public void serverError(String title, String message) {
            genericError(title, message);

        }

        @Override
        public void parseError(String title, String message) {
            genericError(title, message);

        }

        @Override
        public void hasResult(String title, String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void resultOK(String title, String message) {
            // TODO Auto-generated method stub

        }

        @Override
        public void networkError(String title, String message) {
            genericError(title, message);
        }

    }

}
