package com.sdmmllc.superdupermm.ui;

import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class Act_AppList extends ListActivity {

    String TAG = "Act_AppList";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private Context spaContext;
    private ListView scanListView;
    private SDAppList scanList;
    private SDDB spaDB;
    private ArrayList<AppScanInfo> pkgList;
    private ArrayAdapter<AppScanInfo> pkgAA;
    private LayoutInflater mInflater;
    private int scanOption;
    private int scanListId, displayOption;
    private SDScanManager scanManager;
    private TextView listTitle;
    
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
	    	//scanList = scanManager.getScanList(scanListId);
	        pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, scanOption));
	        Collections.sort(pkgList, AppListComparator.getComparator(AppListComparator.APP_NAME));
	    	pkgAA = getArrayAdapter(spaContext);
	        setListAdapter(pkgAA);
	    	pkgAA.notifyDataSetChanged();
	    	if (SdmmsConsts.DEBUG_SCAN_ACT) 
				Log.i(TAG, "updateUI: listener done");
		}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_app_list);
        spaContext = this;
        //userAccount = SpaUserAccount.getUserAccount(spaContext);
        scanManager = SDScanManager.getScanManager(this);
        spaDB = SDSmsManagerApp.getSDDB();
        scanList = SDSmsManagerApp.getInstalledPackages();
        scanListId = scanList.scanID;
        scanOption = getIntent().getExtras().getInt(SDAppList.SCAN_OPTION);
        displayOption = getIntent().getExtras().getInt(SDAppList.DISPLAY_OPTION);
        if (getIntent().getExtras().containsKey(SdmmsConsts.SCAN_LIST_ID)) {
            scanListId = getIntent().getExtras().getInt(SdmmsConsts.SCAN_LIST_ID);
        }
        
        listTitle = (TextView)findViewById(R.id.appsListTitle);
        
        if (displayOption == SDAppList.DISPLAY_CATEGORY) {
	        if (scanOption == AppScanInfo.SMS_NONE) listTitle.setText(getString(R.string.appsListTitle1Txt));
	        if (scanOption == AppScanInfo.SMS_OTHER) listTitle.setText(getString(R.string.appsListTitle2Txt));
	        if (scanOption == AppScanInfo.SMS_APP) listTitle.setText(getString(R.string.appsListTitle3Txt));
        } else {
        	if ((scanOption & AppScanInfo.TYPE_REGISTERED_APP) == AppScanInfo.TYPE_REGISTERED_APP) 
        		listTitle.setText(getString(R.string.appsListTitle4Txt));
        	if ((scanOption & AppScanInfo.TYPE_UNREGISTERED) == AppScanInfo.TYPE_UNREGISTERED) 
        		listTitle.setText(getString(R.string.appsListTitle5Txt));
        }
        
    	//scanList = scanManager.getScanList(scanListId);
        pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, scanOption));
        mInflater = (LayoutInflater) spaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        scanListView = getListView();
        pkgAA = getArrayAdapter(spaContext);
        setListAdapter(pkgAA);
        if (AppScanInfo.WATCH_LIST == scanOption) {
            //Watchlist
        	scanListView.setBackgroundColor(getResources().getColor(R.color.scanYellow));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.scanYellow));
        } else if (AppScanInfo.SMS_OTHER == scanOption) {
            //Watchlist
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmGreen));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmGreen));
        } else if (AppScanInfo.SAFE == scanOption) {
            //Safe
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmGreen));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmGreen));
        } else if (AppScanInfo.SMS_NONE == scanOption) {
            //Safe
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmGreen));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmGreen));
        } else if (AppScanInfo.UNSAFE == scanOption) {
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmRed));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmRed));
        } else if (AppScanInfo.SMS_APP == scanOption) {
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmBlue));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmBlue));
        } else if ((scanOption & AppScanInfo.TYPE_REGISTERED_APP) == AppScanInfo.TYPE_REGISTERED_APP) {
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmBlue));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmBlue));
        } else if ((scanOption & AppScanInfo.TYPE_UNREGISTERED) == AppScanInfo.TYPE_UNREGISTERED) {
        	scanListView.setBackgroundColor(getResources().getColor(R.color.sdmmYellow));
        	scanListView.setCacheColorHint(getResources().getColor(R.color.sdmmYellow));
        }
    	SDSmsManagerApp.getGaTracker().set(Fields.SCREEN_NAME, TAG);
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

    private ArrayAdapter<AppScanInfo> getArrayAdapter(Context context) {
        return new ArrayAdapter<AppScanInfo>(context, R.layout.as_scanlistitem, pkgList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;
                if (null == convertView) {
                    row = mInflater.inflate(R.layout.as_scanlistitem, parent, false);
                } else {
                    row = convertView;
                }


                TextView pkgName = (TextView) row.findViewById(R.id.scanListItemTitle);
                TextView pkgDescr = (TextView) row.findViewById(R.id.scanListItemDescr);

                Button btnViewInfo = (Button) row.findViewById(R.id.scanListItemViewInfoBtn);
                Button btnMarkSafe = (Button) row.findViewById(R.id.scanListItemMarkSafeBtn);
                Button btnMarkUnsafe = (Button) row.findViewById(R.id.scanListItemMarkUnsafeBtn);
                Button btnMarkWatchlist = (Button) row.findViewById(R.id.scanListItemMarkIdkBtn);
                Button btnUninstall = (Button) row.findViewById(R.id.scanListItemUninstallBtn);

                Log.i(TAG, "listType = " + scanOption);
                final AppScanInfo pkgItem = getItem(position);
                String filtersFound = "";
                Integer filterKey = 0;
                Enumeration<Integer> filterKeys = pkgItem.usesPerms.keys();
                while (filterKeys.hasMoreElements()) {
                    filterKey = filterKeys.nextElement();
                    filtersFound += "\n" + pkgItem.usesPerms.getFilter(filterKey).mFilter.permName; 
                    filtersFound += " - Priority:" + pkgItem.usesPerms.getFilter(filterKey).permPriority;
                }
                pkgName.setText(pkgItem.appName);
                String descr = pkgItem.pkgName;
                if (pkgItem.isSysApp()) 
                	descr += "<br><i><b>" + getString(R.string.scanListItemSysAppDescr) + "</b></i>";
                if (pkgItem.isUserClassified()) 
                	descr += "<br><i><b>" + getString(R.string.scanListItemUserListDescr) + "</b></i>";
                if (pkgItem.isUninstalled())
                	descr += "<br><i><b>" + getString(R.string.scanListItemUninstalledTxt) + 
                	" " + df.format(pkgItem.uninstallDt) + "</b></i>";
                if (filtersFound.length() > 1) 
                	descr += "<br>" + filtersFound;
                if (pkgItem.smsReceivedReceiver.length() > 1) 
                	descr += "<br>" + pkgItem.smsReceivedReceiver;
                if (pkgItem.smsDeliveredReceiver.length() > 1) 
                	descr += "<br>" + pkgItem.smsDeliveredReceiver;
                if (pkgItem.smsRespondViaMessage.length() > 1) 
                	descr += "<br>" + pkgItem.smsRespondViaMessage;
                if (pkgItem.smsSend.length() > 1) 
                	descr += "<br>" + pkgItem.smsSend;
                if (pkgItem.smsSendTo.length() > 1) 
                	descr += "<br>" + pkgItem.smsSendTo;
                
                descr = getString(R.string.appsListTitlePkgLbl) + " " + pkgItem.pkgName + "<br/>";
                if (pkgItem.isKitKatSMS()) 
                	descr += getString(R.string.appsListTitleKitKatMessaging) + "<br/>";
                if (pkgItem.isSysApp()) 
                	descr += getString(R.string.appsListTitleSysApp) + "<br/>";
                pkgDescr.setText(Html.fromHtml(descr));


                if (AppScanInfo.WATCH_LIST == scanOption) {
                    //Watchlist
                    btnMarkWatchlist.setVisibility(View.GONE);
                    btnUninstall.setVisibility(View.GONE);
                    btnMarkUnsafe.setVisibility(View.VISIBLE);
                    btnMarkSafe.setVisibility(View.VISIBLE);
                } else if (AppScanInfo.SMS_OTHER == scanOption) {
                    //Watchlist
                    btnMarkWatchlist.setVisibility(View.GONE);
                    btnUninstall.setVisibility(View.GONE);
                    btnMarkUnsafe.setVisibility(View.VISIBLE);
                    btnMarkSafe.setVisibility(View.VISIBLE);
                } else if (AppScanInfo.SAFE == scanOption) {
                    //Safe
                    btnMarkSafe.setVisibility(View.GONE);
                    btnUninstall.setVisibility(View.GONE);
                    btnMarkWatchlist.setVisibility(View.VISIBLE);
                    btnMarkUnsafe.setVisibility(View.VISIBLE);
                } else if (AppScanInfo.SMS_NONE == scanOption) {
                    //Safe
                    btnMarkSafe.setVisibility(View.GONE);
                    btnUninstall.setVisibility(View.GONE);
                    btnMarkWatchlist.setVisibility(View.VISIBLE);
                    btnMarkUnsafe.setVisibility(View.VISIBLE);
                } else if (AppScanInfo.UNSAFE == scanOption) {
                    //Spyware
                    btnMarkUnsafe.setVisibility(View.GONE);
                    btnMarkWatchlist.setVisibility(View.VISIBLE);
                    btnMarkSafe.setVisibility(View.VISIBLE);

                    if (pkgItem.isSysApp()) {
                        //Can't uninstall system apps so hide the button
                        btnUninstall.setVisibility(View.GONE);
                    } else {
                        btnUninstall.setVisibility(View.VISIBLE);
                    }
                } else if (AppScanInfo.SMS_APP == scanOption) {
                    //Spyware
                    btnMarkUnsafe.setVisibility(View.GONE);
                    btnMarkWatchlist.setVisibility(View.VISIBLE);
                    btnMarkSafe.setVisibility(View.VISIBLE);

                    if (pkgItem.isSysApp()) {
                        //Can't uninstall system apps so hide the button
                        btnUninstall.setVisibility(View.GONE);
                    } else {
                        btnUninstall.setVisibility(View.VISIBLE);
                    }
                }

                if (btnViewInfo.getVisibility() == View.VISIBLE) {
                	if (!pkgItem.isUninstalled()) {
                		btnViewInfo.setEnabled(true);
	                    btnViewInfo.setOnClickListener(new OnClickListener() {
	                        @Override
	                        public void onClick(View v) {
//                          	Intent viewIntent = new Intent(spaContext, SpaASScanItem.class);
//                          	viewIntent.putExtra(SpaASConsts.SCAN_LIST_ID, scanList.scanID);
//                          	viewIntent.putExtra(SpaASConsts.SCAN_ITEM_ID, pkgItem.pkgName);
//                          	viewIntent.putExtra(SpaTextConsts.SCAN_TYPE, listType);
//                          	startActivityForResult(viewIntent, SpaASConsts.VIEWSCANITEM);
	
	                        	showInstalledAppDetails(spaContext, pkgItem.pkgName);
	                        }
	                    });
                	} else btnViewInfo.setEnabled(false);
                }

                if (btnUninstall.getVisibility() == View.VISIBLE) {
                    btnUninstall.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Launch Uninstall intent
                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE);
                            uninstallIntent.setData(Uri.parse("package:" + pkgItem.pkgName));
                            startActivity(uninstallIntent);
                        }
                    });
                }

                if (btnMarkUnsafe.getVisibility() == View.VISIBLE) {
                    btnMarkUnsafe.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
			 		      	if (SdmmsConsts.DEBUG_SCAN_ACT) 
	                            Log.i(TAG, "btnMakUnsafe.setOnClickListener: package: " + pkgItem.pkgName);
                            scanList.setUserUnsafe(spaContext, pkgItem.pkgName);
                            pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, scanOption));
                            pkgAA.remove(pkgItem);
                            Toast.makeText(spaContext, pkgItem.appName + " " + getString(R.string.scanListItemMovedToUnsafeListTxt), Toast.LENGTH_LONG).show();
                       }
                    });
                }

                if (btnMarkSafe.getVisibility() == View.VISIBLE) {
                    btnMarkSafe.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
			        	if (SdmmsConsts.DEBUG_SCAN_ACT) 
                            Log.i(TAG, "btnMarkSafe.setOnClickListener: package: " + pkgItem.pkgName);
                            scanList.setUserSafe(spaContext, pkgItem.pkgName);
                            pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, scanOption));
                            pkgAA.remove(pkgItem);
                            Toast.makeText(spaContext, pkgItem.appName + " " + getString(R.string.scanListItemMovedToSafeListTxt), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                if (btnMarkWatchlist.getVisibility() == View.VISIBLE) {
                    btnMarkWatchlist.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
			        	if (SdmmsConsts.DEBUG_SCAN_ACT)
                            Log.i(TAG, "btnMarkWatchlist.setOnClickListener: package: " + pkgItem.pkgName);
                            scanList.setUserWatchlist(spaContext, pkgItem.pkgName);
                            pkgList = new ArrayList<AppScanInfo>(scanList.getList(displayOption, scanOption));
                            pkgAA.remove(pkgItem);
                            Toast.makeText(spaContext, pkgItem.appName + " " + getString(R.string.scanListItemMovedToWatchlistTxt), Toast.LENGTH_LONG).show();
                        }
                    });
                }

                btnMarkWatchlist.setVisibility(View.GONE);
                btnMarkSafe.setVisibility(View.GONE);
                btnMarkUnsafe.setVisibility(View.GONE);
                return row;
            }
        };
    }

    private static final String SCHEME = "package";

    private static final String APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName";

    private static final String APP_PKG_NAME_22 = "pkg";

    private static final String APP_DETAILS_PACKAGE_NAME = "com.android.settings";

    private static final String APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails";

    public static void showInstalledAppDetails(Context context, String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent.setAction(ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts(SCHEME, packageName, null);
            intent.setData(uri);
        } else { // below 2.3
            final String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22
                    : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME,
                    APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        context.startActivity(intent);
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
