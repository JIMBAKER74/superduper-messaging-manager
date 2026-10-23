package com.sdmmllc.superdupermm.ui;

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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.analytics.tracking.android.Fields;
import com.placed.client.android.persistent.PlacedAgent;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.Installation;
import com.sdmmllc.superdupermm.MessagingService;
import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.scan.SDScanListener;
import com.sdmmllc.superdupermm.scan.SDScanManager;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.Utils;

public class ComposeSmsActivity extends Activity {

    private static final String TAG = "Act_Home";
    private Activity mActivity;
    private SharedPreferences settings;
    private HorizontalScrollView hr = null;
    private LayoutInflater mInflater;
    private SDScanManager scanManager;
    private Hashtable<String, RelativeLayout> messagingItems = new Hashtable<String, RelativeLayout>();

	private SDScanListener mSDScanListener = new SDScanListener() {

		@Override
		public void updateScanProgress(int scanItemsTotal,
				int scanItemsCompleted) {
		}

		@Override
		public void compilingResults() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void scanComplete(String pkgName, String action) {
			runOnUiThread(updateUI);
		}
		
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_home);
        mActivity = this;
        scanManager = SDScanManager.getScanManager(this);

        settings = getSharedPreferences(SDScanManager.SCAN_PREFS, 0);
        
        PlacedAgent.registerApp(this.getApplicationContext(), getString(R.string.placedId));

    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
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
        SDSmsManagerApp.getScanManager().addScanListener(mSDScanListener);
    }
    
    @Override
    public void onStop() {
        SDSmsManagerApp.getScanManager().removeScanListener(mSDScanListener);
        super.onStop();
    }
    
    private void setupUI() {
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.homeAppSelectorLayout);
        if (hr != null) mainLayout.removeView(hr);
        hr = new HorizontalScrollView(mActivity);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.BELOW, R.id.homecontentLayout);
        hr.setLayoutParams(lp);

        LinearLayout layout = new LinearLayout(mActivity);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

        List<AppScanInfo> apps = SDSmsManagerApp.getInstalledPackages().getMessagingCompatibleList();
        LinearLayout marketLayout = (LinearLayout)findViewById(R.id.homeAppSelectorMarketLayout);
    	TextView compatibleTxt = (TextView)findViewById(R.id.homeLauncherInstructions);
    	TextView warningTxt = (TextView)findViewById(R.id.homeAppSelectorWarning);
        
        if (apps.size() < 1) {
        	compatibleTxt.setVisibility(View.GONE);
        	warningTxt.setVisibility(View.VISIBLE);
            marketLayout.setVisibility(View.VISIBLE);
            setupMarketAppsSelector();
        } else {
        	if (settings.getBoolean(SdmmsConsts.VIEWED_SETUP, false)) launchDefaultSmsSelector();
        	compatibleTxt.setVisibility(View.VISIBLE);
        	warningTxt.setVisibility(View.GONE);
            marketLayout.setVisibility(View.GONE);
	        for(AppScanInfo scan : apps){
	        	RelativeLayout item = (RelativeLayout)mActivity.getLayoutInflater().inflate(R.layout.app_selector, null);
	        	ImageView img = (ImageView)item.findViewById(R.id.appSelectorCompatibleIcon);
	        	TextView text = (TextView)item.findViewById(R.id.appSelectorCompatibleDesc);
	        	try {
					img.setImageDrawable(getPackageManager().getApplicationIcon(scan.pkgName));
				} catch (NameNotFoundException e) {
					// if this happened, the app is no longer installed or there is some
					// other display problem... do not show this app
					item.setVisibility(View.GONE);
					e.printStackTrace();
				}
	            text.setText(scan.appName);
	            text.setWidth(img.getWidth());
	            item.setClickable(true);
	            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
	            		((int) mActivity.getResources().getDimension(R.dimen.launcher_size)), 
	            		LayoutParams.WRAP_CONTENT);
	            llp.setMargins(0, 0, 0, 10);
	
	            final String pkgName = scan.pkgName;
	            item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
			            Intent reLaunch = new Intent(Intent.ACTION_MAIN);
			            reLaunch.setPackage(pkgName);
			            startActivity(reLaunch);
					}
	            });
	            
	            item.setLongClickable(true);
	            item.setOnLongClickListener(new OnLongClickListener(){
					@Override
					public boolean onLongClick(View v) {
						scanManager.setPreferredMessagingApp(pkgName);
						resetHighlight(pkgName);
						return false;
					}
	            });
	            if (pkgName.equals(settings.getString(SDScanManager.PRIMARY_MESSAGING_APP, ""))) {
	            	item.setBackgroundColor(getResources().getColor(R.color.selectorItemHighlight));
	            } else {
	            	item.setBackgroundColor(getResources().getColor(R.color.sdmmBgGray));
	            }
	           
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
           } else {
            	messagingItems.get(pkgName)
            		.setBackgroundColor(getResources().getColor(R.color.selectorItemBackground));
            }
    	}
    }
    
    private Runnable updateUI = new Runnable() {

		@Override
		public void run() {
			setupUI();
		}
    	
    };

    @Override
    public void onResume(){
        if (!settings.getBoolean(SdmmsConsts.VIEWED_SETUP, false) || SdmmsConsts.DEBUG_INSTALL) {
            Installation install = new Installation(this);
            install.finishInstall(this);
        	startActivityForResult(new Intent(this, Act_Welcome.class), 0);
        }
        
        // Placeholder to process incoming SEND/SENDTO intents
        String intentAction = getIntent() == null ? null : getIntent().getAction();
        Log.i(TAG, "Package name: " + this.getPackageName() + " intentAction:" + intentAction);
        if (!TextUtils.isEmpty(intentAction) && ((Intent.ACTION_SENDTO.equals(intentAction)
                || Intent.ACTION_SEND.equals(intentAction)))) {
            Intent reLaunch = getIntent();
            reLaunch.setClass(this, MessagingService.class);
            this.startService(reLaunch);
        } 
        
    	setupUI();
        super.onResume();
    }
    
    private void setupMarketAppsSelector() {
        LinearLayout mainLayout = (LinearLayout)findViewById(R.id.homeAppSelectorMarketAppsLayout);
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

    public void viewApps(View v) {
        Intent appsIntent = new Intent(this, Act_ViewApps.class);
        startActivity(appsIntent);
    }
    
    public void viewSettings(View v) {
        Intent appsIntent = new Intent(this, Act_Settings.class);
        startActivity(appsIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SdmmsConsts.CHANGE_DEF_SMS_SCREEN) {
        }
    }
}
