package com.sdmmllc.superdupermm.comms;

import java.util.Hashtable;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class CommManager {

	public static String TAG = "ConnectionManager";
	Context spaContext;
	int connectionProgress = 0, errorType = 0;
	Hashtable<Activity, CommStatusAdapter> mCommStatus = new Hashtable<Activity, CommStatusAdapter>();
	
	public static CommManager getCommManager(Context context) {
		return SDSmsManagerApp.getCommManager();
	}
	
	public CommManager(Context context) {
		spaContext = context;
	}
	
	private boolean isNetworkAvailable() {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "isNetworkAvailable: checking network");
		ConnectivityManager connectivityManager = 
			(ConnectivityManager)spaContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "isNetworkAvailable: network available =" + (activeNetworkInfo != null));
		return activeNetworkInfo != null;
	}
	
	public void addCommStatusListener(Activity activity, CommStatusAdapter commStatus) {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "addCommStatusListener: adding listener, count: " + mCommStatus.size());
		mCommStatus.put(activity, commStatus);
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "addCommStatusListener: listener added, count: " + mCommStatus.size());
	}
	
	public void removeCommStatusListener(CommStatusAdapter commStatus) {
		if (mCommStatus.contains(commStatus)) mCommStatus.remove(commStatus);
	}
	
	public void notify(int resultType, String title, String message) {
		errorType = resultType;
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "notify: checking for listeners, count: " + mCommStatus.size());
		Set<Activity> activities = mCommStatus.keySet();
		for (Activity activity : activities) {
			if (activity.hasWindowFocus()) {
				if (SdmmsConsts.DEBUG_COMMS) 
					Log.i(TAG, "notify: listener found, calling...");
				mCommStatus.get(activity).result(resultType, title, message);
				activity.runOnUiThread(mCommStatus.get(activity));
			}
		}
	}
	
	public void reNotify() {
		notify(errorType);
	}
	
	public void notify(int errorType) {
		if (errorType == CommStatusAdapter.GENERIC_ERROR) genericError();
		else if (errorType == CommStatusAdapter.PARSE_ERROR) parseError();
		else if (errorType == CommStatusAdapter.NETWORK_ERROR) networkError();
		else if (errorType == CommStatusAdapter.SERVER_ERROR) serverError();
		else if (errorType == CommStatusAdapter.NO_CONNECTION) networkAvailable();
	}
	
	public boolean networkAvailable() {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "networkAvailable: checking network...");
		if (!isNetworkAvailable()) {
			if (SdmmsConsts.DEBUG_COMMS) 
				Log.i(TAG, "networkAvailable: network not available, notify.");
			errorType = CommStatusAdapter.NO_CONNECTION;
			notify(CommStatusAdapter.NO_CONNECTION, 
					spaContext.getString(R.string.noNetworkTitle),
					spaContext.getString(R.string.noNetworkMessage));
			return false;
		}
		boolean reachable = true;
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "networkAvailable: attempting to reach host.");
		//try {
		//	reachable = InetAddress.getByName("antispycorp.com").isReachable(1000);
		//} catch (UnknownHostException e) {
		//} catch (IOException e) {
		//} finally {
		//}
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "networkAvailable: host reachable: " + reachable);
		if (!reachable) {
			networkError();
			return false;
		}
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "networkAvailable: network is available.");
		errorType = 0;
		return true;
	}

	public void networkError() {
		networkError(spaContext.getString(R.string.commErrorTitle), 
				spaContext.getString(R.string.commErrorMessage));
	}
	
	public void networkError(String title, String message) {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "networkError: notify.");
		errorType = CommStatusAdapter.NETWORK_ERROR;
		notify(CommStatusAdapter.NETWORK_ERROR, title, message);
	}

	public void serverError() {
		networkError(spaContext.getString(R.string.serverErrorTitle), 
				spaContext.getString(R.string.serverErrorMessage));
	}
	
	public void serverError(String title, String message) {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "serverError: notify.");
		errorType = CommStatusAdapter.SERVER_ERROR;
		notify(CommStatusAdapter.SERVER_ERROR, title, message);
	}

	public void parseError() {
		parseError(spaContext.getString(R.string.parseErrorTitle), spaContext.getString(R.string.parseErrorMessage));
	}
	
	public void parseError(String title, String message) {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "parseError: notify.");
		errorType = CommStatusAdapter.PARSE_ERROR;
		notify(CommStatusAdapter.PARSE_ERROR, title, message);
	}

	public void genericError() {
		parseError(spaContext.getString(R.string.genericErrorTitle), spaContext.getString(R.string.genericErrorMessage));
	}
	
	public void genericError(String title, String message) {
		if (SdmmsConsts.DEBUG_COMMS) 
			Log.i(TAG, "parseError: notify.");
		errorType = CommStatusAdapter.GENERIC_ERROR;
		notify(CommStatusAdapter.GENERIC_ERROR, title, message);
	}
}
