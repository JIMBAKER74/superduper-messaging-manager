package com.sdmmllc.superdupermm.xml;

import java.util.ArrayList;
import java.util.List;

public class SpaXmlResultSpywareListInfo {

    public static String riskListUpdateXML = "RiskListUpdate", riskListXML = "RiskList",
    	riskListInfo = "risk_list_info",
    	riskListID = "risk_list_ID", riskListType = "risk_list_type_FLG", 
    	riskListDate = "risk_list_creation_TS", riskListMinAppVersion = "risk_list_min_app_version_CD",
    	riskListDownloadDate = "risk_list_download_TS";
    public String mAppListID, mRiskListType, mRiskListDate, mRiskListMinAppVersion,
    	mRiskListDownloadDate;
    public SpaXmlResultSpywareAppInfo appVersionInfo = new SpaXmlResultSpywareAppInfo();
    public List<SpaXmlResultAppPackageInfo> packageInfo = new ArrayList<SpaXmlResultAppPackageInfo>();
    public boolean hasResult = false;
}
