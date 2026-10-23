package com.sdmmllc.superdupersmsmanager.utils;


import com.flurry.android.FlurryAgent;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class ErrorReporting {
    
    private static final int FLURRY_VALUE_LENGTH_LIMIT = 255;

                                                                    	//TESTING ACCOUNT KEY     //PRODUCTION ACCOUNT KEY
    public static final String FLURRY_APP_KEY = SdmmsConsts.PRODUCTION ? "HJ4N98SKP2ZZ93PVX43M" : "6HRCZV2WB7T7DVRT6BKG";

                                                         //Production                            //Testing
    public static final String ACRA_CRASH_REPORT_KEY = ""; // "dDZod0ZVa2FHSjBPbk8zZjJ2cUlYdEE6MQ";
    
    public static void onError(String tag, Exception e){
        if(e != null && !SdmmsConsts.PRODUCTION){
            //onErrorFlurry(tag, e.getMessage(), e.getMessage());
            //call other error reporting APIs
            //ErrorReporter.getInstance().handleException(e);
        }
    }
    
    public static void onErrorFlurry(String tag, String message, String errorClass){
        FlurryAgent.onError(
                StringUtils.truncate(StringUtils.convertNullToEmpty(tag), FLURRY_VALUE_LENGTH_LIMIT),
                StringUtils.truncate(StringUtils.convertNullToEmpty(message), FLURRY_VALUE_LENGTH_LIMIT),
                StringUtils.truncate(StringUtils.convertNullToEmpty(errorClass), FLURRY_VALUE_LENGTH_LIMIT));
    }
}
