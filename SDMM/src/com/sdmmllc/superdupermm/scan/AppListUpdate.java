package com.sdmmllc.superdupermm.scan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.sdmmllc.superdupermm.data.AppScanInfo;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDBConn;
import com.sdmmllc.superdupermm.xml.SpaXmlCoder;
import com.sdmmllc.superdupermm.xml.SpaXmlCoderAdapter;
import com.sdmmllc.superdupermm.xml.SpaXmlCoderListener;
import com.sdmmllc.superdupermm.xml.SpaXmlResultAppPackageInfo;
import com.sdmmllc.superdupermm.xml.SpaXmlResultSpywareListInfo;
import com.sdmmllc.superdupermm.xml.SpaXmlUpdateInfo;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class AppListUpdate {
	
	private String TAG = "ScanPkgUpdate";
	private SDDB spaDB;
	private Context spaContext;
	private SpaXmlUpdateInfo xmlUpdateInfo;
	private SpaXmlResultSpywareListInfo xmlResult;
    private List<SpaXmlResultSpywareListInfo> listInfoResult;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private AppListUpdateListener mScanPkgUpdateListener;
    private boolean updateResponse = false;
	
	public AppListUpdate(Context context) {
		spaContext = context;
    	settings = spaContext.getSharedPreferences(SdmmsConsts.SCAN_FILE, 0);
    	editor = spaContext.getSharedPreferences(SdmmsConsts.SCAN_FILE, 0).edit();
    	SDDB spaDB = SDSmsManagerApp.getSDDB();
    	SDDBConn conn = spaDB.open(false);
    	editor.putString(SdmmsConsts.APP_INFO_VERSION, spaDB.getCurrentRegisteredAppListId()+"");
    	editor.commit();
    	spaDB.close(conn);
    	try {
    	    ComponentName comp = new ComponentName(spaContext, AppListUpdate.class);
    	    PackageInfo pinfo = spaContext.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
    	    int versionCode = pinfo.versionCode;
    	    if (!settings.getString(SdmmsConsts.SDSMS_VERSION, "0").equals(versionCode+"")) {
    	    	editor.putString(SdmmsConsts.SDSMS_VERSION, versionCode+"");
    	    	editor.commit();
    	    }
    	} catch(android.content.pm.PackageManager.NameNotFoundException e) {
    	}
    	xmlUpdateInfo = new SpaXmlUpdateInfo(
        	settings.getString(SdmmsConsts.SDSMS_VERSION, "0"),
        	settings.getString(SdmmsConsts.APP_INFO_VERSION, "0")
        );
		if (SdmmsConsts.DEBUG_SCAN) 
    		Log.i(TAG, "ScanPkgUpdate: xmlUpdateInfo.getAppVersion():" + xmlUpdateInfo.getAppVersion());
		if (SdmmsConsts.DEBUG_SCAN) 
    		Log.i(TAG, "ScanPkgUpdate: xmlUpdateInfo.getSpywareVersion():" + xmlUpdateInfo.getAppListVersion());
	}
	
	public void setScanPkgUpdateListener(AppListUpdateListener spaPkgUpdateListener) {
		mScanPkgUpdateListener = spaPkgUpdateListener;
	}
	
	public void removeScanPkgUpdateListener(AppListUpdateListener spaPkgUpdateListener) {
		mScanPkgUpdateListener = null;
	}
	
	public boolean getUpdate() {
		final SpaXmlCoder xmlCoder = new SpaXmlCoder(spaContext);
		SpaXmlCoderListener spaXmlCoderListener = new SpaXmlCoderAdapter() {
			@Override
			public void getUpdateInfoResponse() {
				xmlResult = xmlCoder.getUpdateXml();
				if (xmlResult.hasResult) {
		        	xmlUpdateInfo.setAppVersionAvailable(xmlResult.appVersionInfo.mAppVersionCode);
		        	xmlUpdateInfo.setAppListVersionAvailable(xmlResult.mAppListID);
				}
				updateResponse = true;
				if(mScanPkgUpdateListener != null) mScanPkgUpdateListener.getUpdateResponse();
			}
			
		};
		xmlCoder.setSpaXmlCoderListener(spaXmlCoderListener);
		xmlCoder.getUpdateInfo("");
		return updateResponse;
	}
	
	public boolean getAppListUpdate() {
		final SpaXmlCoder xmlCoder = new SpaXmlCoder(spaContext);
		SpaXmlCoderListener spaXmlCoderListener = new SpaXmlCoderAdapter() {
			@Override
			public void getAppListInfoResponse() {
    			if (xmlCoder.haveUpdates) {
    				listInfoResult = xmlCoder.getAppListInfoXml();
    	    		if (SdmmsConsts.DEBUG_SCAN) 
    	    			Log.i(TAG, "returnSpywareInfo: listInfoResult.size(): " + listInfoResult.size());
    		    	// get the app database
    	    		spaDB = SDSmsManagerApp.getSDDB();
    				SDDBConn tmpConn = spaDB.open(true);
    		        for (SpaXmlResultSpywareListInfo tmpListInfo : listInfoResult) {
        	    		if (SdmmsConsts.DEBUG_SCAN) 
    	    				Log.i(TAG, "returnSpywareInfo: checking for list ID: " + tmpListInfo.mAppListID);
    	    			if (!spaDB.hasRegisteredAppList(tmpListInfo.mAppListID)) {
    	    	    		if (SdmmsConsts.DEBUG_SCAN) 
    		        			Log.i(TAG, "returnSpywareInfo: list ID not found (inserting): " + tmpListInfo.mAppListID);
    		        		spaDB.insertAppListInfo(tmpListInfo);
    		        	}
        	    		if (SdmmsConsts.DEBUG_SCAN) 
    	    				Log.i(TAG, "returnSpywareInfo: number of packages: " + tmpListInfo.packageInfo.size());
    		        	for (SpaXmlResultAppPackageInfo tmpPackageInfo : tmpListInfo.packageInfo) {
            	    		if (SdmmsConsts.DEBUG_SCAN) 
    		        			Log.i(TAG, "returnSpywareInfo: checking for package: " + tmpPackageInfo.mPackageName);
    		        		if (!spaDB.hasRegisteredAppListPackage(tmpPackageInfo.mPackageName)) {
                	    		if (SdmmsConsts.DEBUG_SCAN) 
    		        				Log.i(TAG, "returnSpywareInfo: package not found (inserting): " + tmpPackageInfo.mPackageName);
    		        			spaDB.insertAppListPackageInfo(tmpPackageInfo);
    		        		}
    		        	}
    		        }
    		        spaDB.close(tmpConn);
    	        	editor.putString(SdmmsConsts.APP_INFO_VERSION, xmlResult.mAppListID);
    	        	editor.commit();
    			}
				if(mScanPkgUpdateListener != null) mScanPkgUpdateListener.getSpywareUpdateResponse();
			}
		};
		xmlCoder.setSpaXmlCoderListener(spaXmlCoderListener);
		xmlCoder.getAppListInfo("", xmlUpdateInfo.getAppListVersion());
		return true;
	}
	
	public String sendPackageUpdate(AppScanInfo scanItem, long scanStartDt, long scanEndDt, 
			int scan_ID, String dev_ID, int scanCnt, int sysApps, int spyApps) {
		// Create a new HttpClient and Post Header
		
		// the following will help resolve https problem if the certificate does not match the URL
		//HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

	    //DefaultHttpClient client = new DefaultHttpClient();

	    //SchemeRegistry registry = new SchemeRegistry();
	    //SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
	    //socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);
	    //registry.register(new Scheme("https", socketFactory, 443));
	    //SingleClientConnManager mgr = new SingleClientConnManager(client.getParams(), registry);
	    //DefaultHttpClient httpclient = new DefaultHttpClient(mgr, client.getParams());

	    // Set verifier      
	    //HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);			
	    DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://www.superdupersms.com/cgi-bin/package_update.pl");
		if (SdmmsConsts.DEBUG_SCAN) 
     		Log.i(TAG, "sendPackageUpdate: connecting...");

		try {
		    // Add your data
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    nameValuePairs.add(new BasicNameValuePair("ds_dev_ID", dev_ID+""));
		    nameValuePairs.add(new BasicNameValuePair("ds_scan_ID", scan_ID+""));
		    nameValuePairs.add(new BasicNameValuePair("ds_start_TS", scanStartDt+""));
		    nameValuePairs.add(new BasicNameValuePair("ds_end_TS", scanEndDt+""));
		    nameValuePairs.add(new BasicNameValuePair("ds_risk_list_version_ID", xmlUpdateInfo.getAppListVersion()));
		    nameValuePairs.add(new BasicNameValuePair("ds_sys_items_cnt_NUM", sysApps+""));
		    nameValuePairs.add(new BasicNameValuePair("ds_risk_items_cnt_NUM", spyApps+""));
		    nameValuePairs.add(new BasicNameValuePair("ds_items_scan_cnt_NUM", scanCnt+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_scan_ID", scan_ID+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_package_ID", scanItem.pkgId+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_package_name_TXT", scanItem.pkgName));
		    nameValuePairs.add(new BasicNameValuePair("dsi_category_ID", scanItem.pkgType));
		    nameValuePairs.add(new BasicNameValuePair("dsi_flags", scanItem.pkgType+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_usr_category_ID", scanItem.userFlgs+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_usr_category_TS", scanItem.userDt+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_risk_Flg", scanItem.pkgFlgs+""));
		    nameValuePairs.add(new BasicNameValuePair("dsi_risk_category_Flg", scanItem.appCats+""));
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		    // Execute HTTP Post Request
	        try {
	            HttpResponse getResponse = httpclient.execute(httppost);
	            HttpEntity getResponseEntity = getResponse.getEntity();

	            if (getResponseEntity != null) {
	                String response = EntityUtils.toString(getResponseEntity);
	                Log.d(TAG, response);
	                return response;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

		    return "";
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		}
		return "";
	}
}
