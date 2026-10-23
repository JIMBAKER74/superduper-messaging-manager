package com.sdmmllc.superdupermm.notifications;

import java.util.Calendar;

import android.content.Context;

import com.sdmmllc.superdupermm.comms.CommManager;

public class SpaXmlNotificationCoder {
	
	private static String TAG = "SpaXmlNotificationCoder";
    private static final String BASE_URL = "http://www.secondphoneapps.com/";
    private static final String SPA_EMAIL_URL = BASE_URL + "cgi-bin/email_notify.pl";
    private static final String SPA_CONFIDENTIAL_EMAIL_URL = BASE_URL + "cgi-bin/confidential_email_notify.pl";
    boolean haveUpdates = false;
    SpaXmlNotificationCoderListener mSpaXmlNotificationCoderListener;
	java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);
   
    private CommManager commManager;
    private Context spaContext;
    
    public SpaXmlNotificationCoder(Context context) {
    	commManager = CommManager.getCommManager(context);
    	spaContext = context;
    }
    
    public void setSpaXmlCoderListener(SpaXmlNotificationCoderListener spaXmlNotificationCoderListener) {
    	mSpaXmlNotificationCoderListener = spaXmlNotificationCoderListener;
    }
    
    public String getBody(SpaUserNotification notification) {
		String body = notification.emailBody;
		body = body.replaceAll("APPNAME", "\"" + notification.getAppName() + "\"");
		body = body.replaceAll("TIMESTAMP", df.format(Calendar.getInstance().getTime()));
		if (notification.priorConfidentialEmailAddress.length() > 0) 
			body = body.replaceAll("OLD_EMAIL", notification.priorConfidentialEmailAddress);
		if (notification.confidentialEmailAddress.length() > 0) 
			body = body.replaceAll("NEW_EMAIL", notification.confidentialEmailAddress);
		return body;
    }
    
    public String getSubject(SpaUserNotification notification) {
		String subject = notification.emailSubject;
		subject = subject.replaceAll("APPNAME", "\"" + notification.getAppName() + "\"");
		subject = subject.replaceAll("TIMESTAMP", df.format(Calendar.getInstance().getTime()));
		if (notification.priorConfidentialEmailAddress.length() > 0) 
			subject = subject.replaceAll("OLD_EMAIL", notification.priorConfidentialEmailAddress);
		if (notification.confidentialEmailAddress.length() > 0) 
			subject = subject.replaceAll("NEW_EMAIL", notification.confidentialEmailAddress);
		return subject;
    }
    
    //TODO
    // implement if appropriate
    public boolean emailNotification(SpaUserNotification notification, String userid) {
/**        if (!commManager.networkAvailable()) return false;
	    DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(SPA_EMAIL_URL);
    	if (SdmmsConsts.DEBUG_NOTIFICATION) 
     		Log.i(TAG, "emailNotification: connecting...");

		try {
		    // Add your data
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			if (SdmmsConsts.DEBUG_NOTIFICATION) { 
				Log.i(TAG, "emailNotification: userid - " + userid);
				Log.i(TAG, "emailNotification: email_subject - " + getSubject(notification));
				Log.i(TAG, "emailNotification: email_body - " + getBody(notification));
			}
			nameValuePairs.add(new BasicNameValuePair("userid", userid));
			nameValuePairs.add(new BasicNameValuePair("subject", getSubject(notification)));
			nameValuePairs.add(new BasicNameValuePair("body", getBody(notification)));
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		    // Execute HTTP Post Request
	        try {
	            HttpResponse getResponse = httpclient.execute(httppost);
	            HttpEntity getResponseEntity = getResponse.getEntity();

	            if (getResponseEntity != null) {
	            	String response = EntityUtils.toString(getResponseEntity);
	                Log.d(TAG, response);
	                SpaXmlNotificationResult tmpCheck = SpaXmlParser.parseXmlEmailResponse(spaContext, response);
	            	if (mSpaXmlNotificationCoderListener != null)
	            		mSpaXmlNotificationCoderListener.getEmailResponse();
	            	notification.emailSent_status = tmpCheck.messageSent;
	            	return tmpCheck.hasResult;
	            } else return false;
	        } catch (IOException e) {
                ErrorReporting.onError(TAG, e);
                commManager.networkError();
	            e.printStackTrace();
	        }

        	if (mSpaXmlNotificationCoderListener != null)
        		mSpaXmlNotificationCoderListener.getEmailResponse();
		    return false;
		} catch (IOException e) {
            ErrorReporting.onError(TAG, e);
            commManager.networkError();
		    // TODO Auto-generated catch block
		}
    	if (mSpaXmlNotificationCoderListener != null)
    		mSpaXmlNotificationCoderListener.getEmailResponse(); **/
		return false;
	}

//TODO
    // implement this if appropriate
    public boolean confidentialEmailNotification(SpaUserNotification notification, String userid) {
/**	    DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(SPA_CONFIDENTIAL_EMAIL_URL);
//    	if (SdmmsConsts.DEBUG_NOTIFICATION) 
     		Log.i(TAG, "confidentialEmailNotification: connecting...");

		try {
		    // Add your data
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//			if (SdmmsConsts.DEBUG_NOTIFICATION) { 
				Log.i(TAG, "confidentialEmailNotification: userid - " + userid);
				Log.i(TAG, "confidentialEmailNotification: email_subject - " + getSubject(notification));
				Log.i(TAG, "confidentialEmailNotification: email_body - " + getBody(notification));
//			}
			nameValuePairs.add(new BasicNameValuePair("userid", userid));
			nameValuePairs.add(new BasicNameValuePair("subject", getSubject(notification)));
			nameValuePairs.add(new BasicNameValuePair("body", getBody(notification)));
			nameValuePairs.add(new BasicNameValuePair("emailAddress", notification.confidentialEmailAddress));
			nameValuePairs.add(new BasicNameValuePair("priorEmailAddress", notification.priorConfidentialEmailAddress));
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		    // Execute HTTP Post Request
	        try {
	            HttpResponse getResponse = httpclient.execute(httppost);
	            HttpEntity getResponseEntity = getResponse.getEntity();

	            if (getResponseEntity != null) {
	            	String response = EntityUtils.toString(getResponseEntity);
	                Log.d(TAG, response);
	                SpaXmlNotificationResult tmpCheck = SpaXmlParser.parseXmlEmailResponse(spaContext, response);
	            	if (mSpaXmlNotificationCoderListener != null)
	            		mSpaXmlNotificationCoderListener.getEmailResponse();
	            	notification.emailSent_status = tmpCheck.messageSent;
	            	return tmpCheck.hasResult;
	            } else return false;
	        } catch (IOException e) {
                ErrorReporting.onError(TAG, e);
                commManager.networkError();
	            e.printStackTrace();
	        }

        	if (mSpaXmlNotificationCoderListener != null)
        		mSpaXmlNotificationCoderListener.getEmailResponse();
		    return false;
		} catch (IOException e) {
            ErrorReporting.onError(TAG, e);
            commManager.networkError();
		    // TODO Auto-generated catch block
		}
    	if (mSpaXmlNotificationCoderListener != null)
    		mSpaXmlNotificationCoderListener.getEmailResponse(); */
		return false;
	}
}
