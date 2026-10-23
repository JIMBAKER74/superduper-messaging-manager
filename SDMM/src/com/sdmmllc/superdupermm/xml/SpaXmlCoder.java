package com.sdmmllc.superdupermm.xml;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.sdmmllc.superdupermm.comms.CommManager;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class SpaXmlCoder {
	private static final String TAG = "SpaXmlCoder"; 
    private static final String SPA_UPDATE_URL = "http://www.secondphoneapps.com/cgi-bin/update.pl";
    private static final String SPA_SPYWARE_URL = "http://www.secondphoneapps.com/cgi-bin/spyware_list.pl";
    private List<SpaXmlResultSpywareListInfo> packageInfoResult;
    private SpaXmlResultSpywareListInfo listInfoResult;
    public boolean haveUpdates = false;
    private SpaXmlCoderListener mSpaXmlCoderListener;
    
    private SpaXmlHttpRetriever httpRetriever;
    private CommManager commManager;
    private Context spaContext;
    
    public SpaXmlCoder(Context context) {
        httpRetriever = new SpaXmlHttpRetriever(context);
    	commManager = CommManager.getCommManager(context);
    	spaContext = context;
    }
    
    
    public void setSpaXmlCoderListener(SpaXmlCoderListener spaXmlCoderListener) {
    	mSpaXmlCoderListener = spaXmlCoderListener;
    }
    
    public void getUpdateInfo(String accountInfo) {
        if (!commManager.networkAvailable()) return;
        
    	accountInfo = accountInfo.replaceAll(" ", "+");
        String url = String.format(SPA_UPDATE_URL, accountInfo);        
        String response = httpRetriever.retrieve(url);
        listInfoResult = SpaXmlParser.parseXmlUpdateResponse(spaContext, response);
        if (listInfoResult != null) {
        	haveUpdates = true;
        	if (mSpaXmlCoderListener != null)
        		mSpaXmlCoderListener.getUpdateInfoResponse();
        }
    }
    
    public void getAppListInfo(String accountInfo, String versionCD) {
        if (!commManager.networkAvailable()) return;
        
    	accountInfo = accountInfo.replaceAll(" ", "+");
        String url = String.format(SPA_SPYWARE_URL + "?versionCode=" + versionCD, accountInfo);        
        String response = httpRetriever.retrieve(url);
        packageInfoResult = SpaXmlParser.parseXmlSpywareListResponse(spaContext, response);
        if (packageInfoResult != null) haveUpdates = true;
		if (SdmmsConsts.DEBUG_SCAN) 
			Log.i(TAG, "getRiskListInfo: complete");
		if (mSpaXmlCoderListener != null) mSpaXmlCoderListener.getAppListInfoResponse();
    }
    
    public SpaXmlResultSpywareListInfo getUpdateXml() {
    	return listInfoResult;
    }

    public List<SpaXmlResultSpywareListInfo> getAppListInfoXml() {
    	return packageInfoResult;
    }
}
