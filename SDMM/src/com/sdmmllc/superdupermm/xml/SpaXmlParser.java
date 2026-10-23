package com.sdmmllc.superdupermm.xml;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.Context;
import android.util.Log;

import com.google.android.vending.licensing.util.Base64;
import com.sdmmllc.superdupermm.comms.CommManager;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;


public class SpaXmlParser {
	
    private final static String TAG = "SpaXmlParser";
    
    public final static String yalf1 = "yalf1_", yalf2 = "yalf2_", yalf3 = "yalf3_", yalf4 = "yalf4_", yalf5 = "yalf5_";
    
    private static CommManager commManager;
    
	public SpaXmlParser(Context context) {
		commManager = CommManager.getCommManager(context);
	}

    public static List<SpaXmlResultSpywareListInfo> parseXmlSpywareListResponse(Context context, String response) {
		if (commManager == null) commManager = CommManager.getCommManager(context);
        
        try {
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            
            NodeList resultNodes = doc.getElementsByTagName(SpaXmlResultSpywareListInfo.riskListXML);            
            Node resultNode = resultNodes.item(0);            
            NodeList attrsList = resultNode.getChildNodes();
            
            List<SpaXmlResultSpywareListInfo> result = new ArrayList<SpaXmlResultSpywareListInfo>();
            SpaXmlResultSpywareListInfo listResult = new SpaXmlResultSpywareListInfo();
            SpaXmlResultAppPackageInfo pkgResult = new SpaXmlResultAppPackageInfo();
            for (int i=0; i < attrsList.getLength(); i++) {
                
                Node pkgNode = attrsList.item(i);                
                NodeList pkgAttrsList = pkgNode.getChildNodes();
                Node firstPkgChild = pkgNode.getFirstChild();
        		if (SdmmsConsts.DEBUG_XML) 
                	Log.i(TAG, "parseXmlSpywareListResponse: pkgNode.getNodeName(): " + pkgNode.getNodeName());
                if (SpaXmlResultSpywareListInfo.riskListInfo.equalsIgnoreCase(pkgNode.getNodeName()) && firstPkgChild!=null) {
                	// the start of new list info
                	listResult = new SpaXmlResultSpywareListInfo();
                    for (int j=0; j < pkgAttrsList.getLength(); j++) {
                        
	                    Node node = pkgAttrsList.item(j);                
	                    Node firstChild = node.getFirstChild();
	            		if (SdmmsConsts.DEBUG_XML) 
	            			Log.i(TAG, "parseXmlSpywareListResponse: new list info");
		                if (SpaXmlResultSpywareListInfo.riskListID.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	listResult.mAppListID = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskListID - " + listResult.mAppListID);
		            		listResult.hasResult = true;
		                } else
		                if (SpaXmlResultSpywareListInfo.riskListType.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	listResult.mRiskListType = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskListType - " + listResult.mRiskListType);
		            		listResult.hasResult = true;
		                } else
		                if (SpaXmlResultSpywareListInfo.riskListDate.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	listResult.mRiskListDate = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskListDate - " + listResult.mRiskListDate);
		            		listResult.hasResult = true;
		                } else
		                if (SpaXmlResultSpywareListInfo.riskListMinAppVersion.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	listResult.mRiskListMinAppVersion = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskListMinAppVersion - " + listResult.mRiskListMinAppVersion);
		            		listResult.hasResult = true;
		                } else
		                if (SpaXmlResultSpywareListInfo.riskListDownloadDate.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	listResult.mRiskListDownloadDate = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskListDownloadDate - " + listResult.mRiskListDownloadDate);
		            		listResult.hasResult = true;
		                }
                    }
	                result.add(listResult);
                } else
                if (SpaXmlResultAppPackageInfo.riskListPackage.equalsIgnoreCase(pkgNode.getNodeName()) && firstPkgChild!=null) {
                	// the start of new package info
            		if (SdmmsConsts.DEBUG_XML) 
                		Log.i(TAG, "parseXmlSpywareListResponse: start new package info");
                    pkgResult = new SpaXmlResultAppPackageInfo();
                    for (int j=0; j < pkgAttrsList.getLength(); j++) {
                        
	                    Node node = pkgAttrsList.item(j);                
	                    Node firstChild = node.getFirstChild();
	            		if (SdmmsConsts.DEBUG_XML) 
        					Log.i(TAG, "parseXmlSpywareListResponse: checking new package info");
	            		if (SdmmsConsts.DEBUG_XML) 
        					Log.i(TAG, "parseXmlSpywareListResponse: node.getNodeName()" + node.getNodeName());
	            		if (SdmmsConsts.DEBUG_XML) 
            				if (firstChild == null) 
                				Log.i(TAG, "parseXmlSpywareListResponse: firstChild.getNodeValue(): is null");
            				else Log.i(TAG, "parseXmlSpywareListResponse: firstChild.getNodeValue(): " + firstChild.getNodeValue());
		                if (SpaXmlResultAppPackageInfo.riskListID.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mAppListID = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskListID - " + pkgResult.mAppListID);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.packageName.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mPackageName = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mPackageName - " + pkgResult.mPackageName);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.packageAppName.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mPackageAppName = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mPackageAppName - " + pkgResult.mPackageAppName);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.versionCode.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mVersionCode = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mVersionCode - " + pkgResult.mVersionCode);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.versionName.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mVersionName = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mVersionName - " + pkgResult.mVersionName);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.appType.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mAppType = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mAppType - " + pkgResult.mAppType);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.createdByDescr.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mCreatedByDescr = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mCreatedByDescr - " + pkgResult.mCreatedByDescr);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.otherNames.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mOtherNames = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mOtherNames - " + pkgResult.mOtherNames);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.flags.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mFlags = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mFlags - " + pkgResult.mFlags);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.riskDescr.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mRiskDescr = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRiskDescr - " + pkgResult.mRiskDescr);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.recommendAction.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mRecommendAction = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mRecommendAction - " + pkgResult.mRecommendAction);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.entryDate.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mEntryDate = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mEntryDate - " + pkgResult.mEntryDate);
		            		pkgResult.hasResult = true;
		                } else
		                if (SpaXmlResultAppPackageInfo.updateDate.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
		                	pkgResult.mUpdateDate = firstChild.getNodeValue();
		            		if (SdmmsConsts.DEBUG_XML) 
		            			Log.i(TAG, "parseXmlSpywareListResponse: mUpdateDate - " + pkgResult.mUpdateDate);
		            		pkgResult.hasResult = true;
		                }
                    }
                    // add the result to the proper list
                    for (int j = 0; j < result.size(); j++) {
                    	if (result.get(j).mAppListID.equals(pkgResult.mAppListID)) {
		            		if (SdmmsConsts.DEBUG_XML) 
	            				Log.i(TAG, "parseXmlSpywareListResponse: adding package mPackageName - " + pkgResult.mPackageName);
                    		result.get(j).packageInfo.add(pkgResult);
		            		if (SdmmsConsts.DEBUG_XML) 
                    			Log.i(TAG, "parseXmlSpywareListResponse: RiskList " 
                    				+ pkgResult.mAppListID + " - PKG COUNT: " + result.get(j).packageInfo.size());
                    		j = result.size();
                    	}
                    }
                }
            }
    		if (SdmmsConsts.DEBUG_XML) 
    			Log.d(TAG, "parseXmlSpywareListResponse : # of lists updated: " + result.size());
            
            return result;
        }
        catch (Exception e) {
            ErrorReporting.onError(TAG, e);
            commManager.parseError();
            e.printStackTrace();
        }
        return null;
    }

    public static SpaXmlResultSpywareListInfo parseXmlUpdateResponse(Context context, String response) {
		if (commManager == null) commManager = CommManager.getCommManager(context);
        
        try {
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            
            NodeList resultNodes = doc.getElementsByTagName(SpaXmlResultSpywareListInfo.riskListUpdateXML);            
            Node resultNode = resultNodes.item(0);            
            NodeList attrsList = resultNode.getChildNodes();
            
            SpaXmlResultSpywareListInfo result = new SpaXmlResultSpywareListInfo();
            
            for (int i=0; i < attrsList.getLength(); i++) {
                
                Node node = attrsList.item(i);                
                Node firstChild = node.getFirstChild();
                
                if (SpaXmlResultSpywareListInfo.riskListID.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    result.mAppListID = firstChild.getNodeValue();
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlUpdateResponse: mRiskListID - " + result.mAppListID);
                    result.hasResult = true;
                } else
                if (SpaXmlResultSpywareListInfo.riskListType.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    result.mRiskListType = firstChild.getNodeValue();
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlUpdateResponse: mRiskListType - " + result.mRiskListType);
                    result.hasResult = true;
                } else
                if (SpaXmlResultSpywareListInfo.riskListDate.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    result.mRiskListDate = firstChild.getNodeValue();
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlUpdateResponse: mRiskListDate - " + result.mRiskListDate);
                    result.hasResult = true;
                } else
                if (SpaXmlResultSpywareListInfo.riskListMinAppVersion.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    result.mRiskListMinAppVersion = firstChild.getNodeValue();
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlUpdateResponse: mRiskListMinAppVersion - " + result.mRiskListMinAppVersion);
                    result.hasResult = true;
                } else
                if (SpaXmlResultSpywareListInfo.riskListDownloadDate.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    result.mRiskListDownloadDate = firstChild.getNodeValue();
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlUpdateResponse: mRiskListDownloadDate - " + result.mRiskListDownloadDate);
                    result.hasResult = true;
                } else
                    if (SpaXmlResultSpywareAppInfo.appUpdateXML.equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                        NodeList appAttrsList = node.getChildNodes();
                        
                        SpaXmlResultSpywareAppInfo appResult = new SpaXmlResultSpywareAppInfo();
                        for (int j=0; j < appAttrsList.getLength(); j++) {
                            
                            Node appNode = appAttrsList.item(j);                
                            Node appFirstChild = appNode.getFirstChild();
                            
                            if (SpaXmlResultSpywareAppInfo.appVersionCode.equalsIgnoreCase(appNode.getNodeName()) && appFirstChild!=null) {
                            	appResult.mAppVersionCode = appFirstChild.getNodeValue();
                        		if (SdmmsConsts.DEBUG_XML) 
                        			Log.i(TAG, "parseXmlUpdateResponse: mAppVersionCode - " + appResult.mAppVersionCode);
                        		appResult.hasResult = true;
                            } else
                            if (SpaXmlResultSpywareAppInfo.appVersion.equalsIgnoreCase(appNode.getNodeName()) && appFirstChild!=null) {
                            	appResult.mAppVersion = appFirstChild.getNodeValue();
                        		if (SdmmsConsts.DEBUG_XML) 
                        			Log.i(TAG, "parseXmlUpdateResponse: mAppVersion - " + appResult.mAppVersion);
                        		appResult.hasResult = true;
                            } else
                            if (SpaXmlResultSpywareAppInfo.appVersionDate.equalsIgnoreCase(appNode.getNodeName()) && appFirstChild!=null) {
                            	appResult.mAppVersionDate = appFirstChild.getNodeValue();
                        		if (SdmmsConsts.DEBUG_XML) 
                        			Log.i(TAG, "parseXmlUpdateResponse: mAppVersionCode - " + appResult.mAppVersionDate);
                        		appResult.hasResult = true;
                            }
                        }
                        result.appVersionInfo = appResult;
                   }
            }
    		if (SdmmsConsts.DEBUG_XML) 
    			Log.d(TAG, "parseXmlUpdateResponse : " + result.toString());
            
            return result;
        }
        catch (Exception e) {
            ErrorReporting.onError(TAG, e);
            commManager.parseError();
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean parseXmlPackageUpdateResponse(Context context, String response) {
		if (commManager == null) commManager = CommManager.getCommManager(context);
        
        try {
            
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(response)));
            
            NodeList resultNodes = doc.getElementsByTagName(SpaXmlResultSpywareListInfo.riskListUpdateXML);            
            Node resultNode = resultNodes.item(0);            
            NodeList attrsList = resultNode.getChildNodes();
            
            boolean resultScan = false;
            boolean resultScanItem = false;
            
            for (int i=0; i < attrsList.getLength(); i++) {
                
                Node node = attrsList.item(i);                
                Node firstChild = node.getFirstChild();
                
                if ("DevScan".equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    resultScan = (firstChild.getNodeValue().equals("1"));
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlPackageUpdateResponse: DevScan - " + firstChild.getNodeValue());
                } else
                if ("DevScanItem".equalsIgnoreCase(node.getNodeName()) && firstChild!=null) {
                    resultScanItem = (firstChild.getNodeValue().equals("1"));
            		if (SdmmsConsts.DEBUG_XML) 
            			Log.i(TAG, "parseXmlPackageUpdateResponse: DevScanItem - " + firstChild.getNodeValue());
                }
            }
    		if (SdmmsConsts.DEBUG_XML) 
    			Log.d(TAG, "parseXmlPackageUpdateResponse : result" + (resultScan&resultScanItem));
            
            return resultScan&resultScanItem;
        }
        catch (Exception e) {
            ErrorReporting.onError(TAG, e);
            commManager.parseError();
            e.printStackTrace();
        }
        return false;
    }

   public static String yalf(String old_yalf) {
    	return Base64.encodeWebSafe(old_yalf.getBytes(), true);
    }
    
    public static String yalf1(String old_yalf) {
		try {
			byte[] test = old_yalf.getBytes("UTF-8");
	    	String xor = (yalf1+yalf2+yalf3+yalf4+yalf5+yalf1+yalf2+yalf3+yalf4+yalf5);
	    	byte[] test2 = new byte[test.length];
	    	byte[] test3 = xor.getBytes("UTF-8");
	    	for (int i = 0; i < test.length; i++) {
	    		test2[i] = test3[i];
	    	}
	    	byte[] out = new byte[test.length];
	    	for (int i = 0; i < test.length; i++) {
	    		out[i] = (byte) (test[i]^test2[i]);
	    	}
	    	return Base64.encodeWebSafe((byte[])out, true);
		} catch (UnsupportedEncodingException e) {
            ErrorReporting.onError(TAG, e);
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
    }
    
    public static String yalfold(String old_yalf) {
    	char[] buf1 = old_yalf.toCharArray();
    	byte[] test = new byte[buf1.length];
    	old_yalf.getBytes(0, old_yalf.length(), test, 0);
    	String xor = (yalf1+yalf2+yalf3+yalf4+yalf5+yalf1+yalf2+yalf3+yalf4+yalf5);
    	byte[] test2 = new byte[buf1.length];
    	xor.getBytes(0, old_yalf.length(), test2, 0);
    	byte[] out = new byte[buf1.length];
    	for (int i = 0; i < buf1.length; i++) {
    		out[i] = (byte) (test[i]^test2[i]);
    	}
    	return Base64.encodeWebSafe((byte[])out, true);
    }
}
