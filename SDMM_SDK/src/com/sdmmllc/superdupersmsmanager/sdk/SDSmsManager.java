package com.sdmmllc.superdupersmsmanager.sdk;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.sdmmllc.superdupermm.ISDSmsReceiveService;
import com.sdmmllc.superdupermm.ISDSmsSendService;
import com.sdmmllc.superdupermm.SDSmsIntentReceiver;
import com.sdmmllc.superdupermm.SDSmsIntentRegistrationReceiver;
import com.sdmmllc.superdupermm.SDSmsReceiveCallback;

public class SDSmsManager {
	
	public static final int ACCESS_TYPE_SYSTEM 			= 0;
	public static final int ACCESS_TYPE_SYSRESERVED 	= 1;
	public static final int ACCESS_TYPE_ANTISPAM 		= 2;
	public static final int ACCESS_TYPE_PLUGIN	 		= 3;
	public static final int ACCESS_TYPE_AUTORESPONDER 	= 4;
	public static final int ACCESS_TYPE_MSGRESERVED 	= 5;
	public static final int ACCESS_TYPE_MESSAGING 		= 6;
	public static final int ACCESS_TYPE_BACKUP 			= 7;
	public static final int ACCESS_TYPE_OBSERVER		= 8;
	public static final int ACCESS_TYPE_UNREGISTERED 	= 9;
	
	public static final String CHECK_SDSMS_STATUS 			= "CheckSDSmsStatus";
	public static final String SDSMS_SEND_TEXT 				= "com.sdmmllc.superdupermessagingmanager.SEND_SMS_MSG";
	public static final String SDSMS_SEND_DATA 				= "com.sdmmllc.superdupermessagingmanager.SEND_SMS_DATA";
	public static final String SDSMS_SEND_MULTIPART_TEXT 	= "com.sdmmllc.superdupermessagingmanager.SEND_SMS_MULTIPART";
	public static final String SDSMS_PACKAGE 				= "com.sdmmllc.superdupermessagingmanager";
	public static final String SDSMS_MAIN_CLASS				= "com.sdmmllc.superdupermm.ui.Act_Home";
	public static final String SDSMS_SEND_PKG 				= "com.sdmmllc.superdupermessagingmanager";
	public static final String SEND_SERVICE 				= "com.sdmmllc.superdupermm.SDSmsSendService";
	public static final String SEND_SERVICE_ACTION 			= "com.sdmmllc.superdupermm.ISDSmsSendService";
	public static final String SEND_SERVICE_PACKAGE 		= "com.sdmmllc.superdupermessagingmanager";
	public static final String RECEIVE_SERVICE 				= "com.sdmmllc.superdupermm.MessagingService";
	public static final String RECEIVE_SERVICE_ACTION 		= "com.sdmmllc.superdupermm.ISDSmsReceiveService";
	public static final String RECEIVE_SERVICE_PACKAGE 		= "com.sdmmllc.superdupermessagingmanager";
	public static final String START_SERVICE 				= "com.sdmmllc.superdupermm.SDSmsIntentReceiver";
	
	// fields for sending SMS
	public static final String DEST = "mDest";
	public static final String SVC_CENTER = "mServiceCenter";
	public static final String PORT = "port";
	public static final String MESSAGE = "text";
	public static final String MSG_DATA = "msg_data";
	public static final String MESSAGES = "messages";
	public static final String SENT_INTENT = "sentIntent";
	public static final String SENT_INTENTS = "sentIntents";
	public static final String DELIVERY_INTENT = "deliveryIntent";
	public static final String DELIVERY_INTENTS = "deliveryIntents";
	public static final String ABORT_BROADCAST = "abort";
	
	// fields for SDSmsManager comms
	public static final String TEST_BROADCAST = "test_sdmm";
	public static final String TEST_MSG = "This is a test message from Super Duper Messaging Manager";
	public static final String TEST_RESPONSE = "test_sdmm_response";
	public static final String TEST_DISPLAYED = "test_sdmm_displayed";
	public static final String TEST_UPDATE_REQUIRED = "test_sdmm_updated_required";
	public static final String TEST_SUCCESS = "test_sdmm_success";
	public static final String TEST_PACKAGE = "test_sdmm_package";
	public static final String TEST_VERSION = "test_sdmm_version";
	
	public static final String RESPONSE_MSG = "SDMM Default Response";
	
	public static final short DEF_DATA_PORT = 8091;
	
	private static SmsManager sManager;
	private static SDSmsManager sInstance;
	private ISDSmsSendService mISDSmsSendService = null;
	private ServiceConnection svcConn = null;

	private ISDSmsReceiveService mISDSmsReceiveService = null;
	private SDSmsReceiveCallbackConnection mSDSmsReceiveCallbackConn = null;
	private static SDSmsReceiveCallback mISDSmsReceiveCallback = null;
	private SDSmsReceiveCallback tempISDSmsReceiveCallback = null;
	
	private static Context mContext;
	private static Boolean sdsmsManagerInstalled = false;
	private static Boolean sdsmsManagerDefault = false;
	private static SDSmsIntentReceiver receiver;
	private static SDSmsIntentRegistrationReceiver registrationReceiver;
	private static boolean connected = false;
	
	private static String sdsmsID = "";
	private static int sdsmsType = ACCESS_TYPE_UNREGISTERED;
	private static String packageName = "";
	private static String mSendReceiver = "", mReceiveReceiver = "";
	private static String TAG = "SDSmsManager";
	private static boolean waitingForServices = false;
	private boolean settingNewCallback = false;
	private SDSmsManager mInstance = null;
	private static String sdmmPrefs = "sdmm_prefs";
	private static SharedPreferences prefs;
	private static SharedPreferences.Editor editor;
	private static int version = 0;
	
	private SDSmsManager (Context context, String id, int type, 
			String sendReceiver, String receiveReceiver) {
		mContext = context.getApplicationContext();
		isSdsmsIntstalled();
		sManager = SmsManager.getDefault();
		packageName = context.getPackageName();
		sdsmsID = id;
		sdsmsType = type;
		mSendReceiver = sendReceiver;
		mReceiveReceiver = receiveReceiver;
		mInstance = this;
		prefs = context.getSharedPreferences(sdmmPrefs, 0);
		editor = prefs.edit();
		version = Integer.valueOf(context.getString(R.string.sdmm_app_version));
		
		//registerInstallReceiver();
		if (!packageName.equals(SDSMS_PACKAGE)) registrationReceiver();
		initServices();
		waitForServiceConnection();
	}
	
	public static Context getContext() {
		return mContext;
	}

	public static SDSmsManager getDefault(Context context, String id, int type, 
			String sendReceiver, String receiveReceiver) {
		if (sInstance == null) {
			sInstance = new SDSmsManager(context, id, type, sendReceiver, receiveReceiver);
		}
		return sInstance;
	}
	
	public static void reconnect() {
		waitingForServices = true;
		if (sInstance == null) return;
		sInstance.removeServices();
		sInstance.initServices();
		sInstance.waitForServiceConnection();
	}
	
	public static void checkConnections() {
		connected = false;
		
	}
	
	public static int getVersion() {
		return version;
	}
	
	public static void disconnect() {
		waitingForServices = true;
		sInstance.removeServices();
	}
	
	public static void connect(String pkgName) {
		Intent intent = new Intent(SDSmsConsts.SDSMS_RELOAD);
		intent.setClassName(pkgName, SDSmsConsts.SDSMS_RELOAD_RECEIVER);
		mContext.sendBroadcast(intent);
	}

	public static boolean isSdsmsIntstalled(){
		for (ApplicationInfo packageInfo : mContext.getPackageManager().getInstalledApplications(0)) {
			if (packageInfo.packageName.equals(SDSMS_PACKAGE)) {
				sdsmsManagerInstalled = true;
				if (sdsmsManagerInstalled) isSdsmsDefaultSmsApp();
				if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "Found SDMM Package");
				return true;
			}
		}        
		sdsmsManagerInstalled = false;
		return false;
	}

	public static boolean hasKitKat() {
		return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
	}

	public static boolean isSdsmsDefaultSmsApp() {
		if (hasKitKat()) {
			sdsmsManagerDefault = SDSMS_PACKAGE.equals(Telephony.Sms.getDefaultSmsPackage(mContext));
			if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "Status of SDMM Package as Default: " + sdsmsManagerDefault);
			return sdsmsManagerDefault;
		}
		return false;
	}
	
	public static boolean isSdmmVerified() {
		return prefs.getBoolean(TEST_BROADCAST, false);
	}
	
	public static void setSdmmVerified(boolean verified) {
		editor.putBoolean(TEST_BROADCAST, verified);
		editor.commit();
	}
	
	public static boolean hasSdmmVerifiedDisplayed() {
		return prefs.getBoolean(TEST_DISPLAYED, false);
	}
	
	public static void setSdmmVerifiedDisplayed(boolean verified) {
		editor.putBoolean(TEST_DISPLAYED, verified);
		editor.commit();
	}
	
	public static void displaySdmmVerified() {
		if (prefs.getBoolean(TEST_BROADCAST, false) && !prefs.getBoolean(TEST_DISPLAYED, false))
			notifySdmmVerified(mContext);
	}
	
	public static void notifySetDefaultSms(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.sdmmChangeDefSmsTitleTxt));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.sdmmChangeDefSmsDescrTxt)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.sdmmChangeDefSmsChangeTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, SDSMS_PACKAGE);

    			//startActivity(intent);
    			context.startActivity(intent);
            }
        })
        .setNegativeButton(context.getString(R.string.sdmmChangeDefSmsNoChangeTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public static void notifySdmmRequired(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.instKitKatFoundWarningTitle));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.instKitKatFoundWarningDescr)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.instKitKatFoundWarningInstall), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, SDSMS_PACKAGE);

    			//startActivity(intent);
    			context.startActivity(intent);
            }
        })
        .setNegativeButton(context.getString(R.string.instKitKatFoundWarningMoreInfo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public static void notifySdmmVerified(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.sdmmVerifiedTitle));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.sdmmVerifiedDescr)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.sdmmVerifiedDone), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);

    			intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, SDSMS_PACKAGE);

    			//startActivity(intent);
    			context.startActivity(intent);
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	public static void launchSuperDuper(final Context context) {
		LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		TextView title = new TextView(context);
        title.setText(context.getString(R.string.sdmmLaunchTitleTxt));
        //title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setTextSize(20);

        final View helpTextView = mInflater.inflate(R.layout.about_helpview, null);
		final TextView updatesText = (TextView)helpTextView.findViewById(R.id.helptext);
		updatesText.setGravity(Gravity.CENTER_HORIZONTAL);
		updatesText.setText(Html.fromHtml(context.getString(R.string.sdmmLaunchDescrTxt)));
		AlertDialog ad = new AlertDialog.Builder(context)
        .setCustomTitle(title)
        .setView(helpTextView)
        .setNeutralButton(context.getString(R.string.sdmmLaunchTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            	Intent intent = new Intent();
            	intent.setClassName(SDSMS_PACKAGE, SDSMS_MAIN_CLASS);
    			context.startActivity(intent);
            }
        })
        .setNegativeButton(context.getString(R.string.sdmmLaunchNoLaunchTxt), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
        })
        .create();
		ad.show();

	}
	
	private void registerStartServiceReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(START_SERVICE);
		
		receiver = new SDSmsIntentReceiver();
		mContext.registerReceiver(receiver, filter);
	}
	
	private void registrationReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SDSmsConsts.SDSMS_RELOAD);
		registrationReceiver = new SDSmsIntentRegistrationReceiver();
		mContext.registerReceiver(registrationReceiver, filter);
	}
	
    private Thread svcMonitorThread;
    private Runnable svcMonitor = new Runnable() {
        @Override
        public void run() {
            while(mISDSmsSendService == null) {
                try {
                	Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
    		try {
    			mISDSmsSendService.testConnection("Testing connection");
    		} catch (Exception e1) {
    			e1.printStackTrace();
    		}
            if (mSDSmsReceiveCallbackConn == null) {
            	initServices();
            } else {
	            while(!mSDSmsReceiveCallbackConn.connected()) {
	                try {
	                	Thread.sleep(500);
	                } catch (InterruptedException e) {
	                }
	            }
	    		try {
	    			Intent intent = new Intent();
	    			intent.setClassName(RECEIVE_SERVICE_PACKAGE, RECEIVE_SERVICE);
	    			intent.putExtra(SDSmsConsts.SDSMS_ID, sdsmsID);
	    			intent.putExtra(SDSmsConsts.SDSMS_TYPE, sdsmsType);
	    			intent.putExtra(SDSmsConsts.SDSMS_PACKAGE_NAME, packageName);
	    			intent.putExtra(SDSmsConsts.SDSMS_RECEIVER_NAME, mReceiveReceiver);
	    			mISDSmsReceiveCallback = (SDSmsReceiveCallback) mSDSmsReceiveCallbackConn.getCallBack();
	    			Log.i(TAG, "svcMonitor: (mISDSmsReceiveCallback == null): " + (mISDSmsReceiveCallback == null));
	    			Log.i(TAG, "svcMonitor: sdsmsID: " + sdsmsID + " sdsmsType: " + sdsmsType);
	    			mISDSmsReceiveCallback.testCallbackConnection(TAG + ".svcMonitor testing");
	    			mSDSmsReceiveCallbackConn.testConnection("testing connection for: " + sdsmsID);
	    			boolean registered = mSDSmsReceiveCallbackConn.registerApp(intent);
	    			Log.i(TAG, "svcMonitor: app registered: " + registered);
	    			mSDSmsReceiveCallbackConn.registerReceiver(sdsmsID, sdsmsType, intent);
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    		waitingForServices = false;
	        }
        }
    };
    
    private Thread newCallbackThread;
    private Runnable setNewCallbackMonitor = new Runnable() {
        @Override
        public void run() {
            while(waitingForServices) {
                try {
                	Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
    		mISDSmsReceiveCallback = tempISDSmsReceiveCallback;
    		tempISDSmsReceiveCallback = null;
    		removeServices();
    		initServices();
    		waitForServiceConnection();
        }
    };
    
	private void waitForServiceConnection() {
		waitingForServices = true;
    	svcMonitorThread = new Thread(null, svcMonitor, "svcMonitor Thread : " + TAG);
    	svcMonitorThread.start();
	}
	
	public static boolean isWaitingForServices() {
		return waitingForServices;
	}
	
	private void initServices() {
		waitingForServices = true;
		svcConn = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder binder) {
				mISDSmsSendService = ISDSmsSendService.Stub.asInterface(binder);
				if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices.onServiceConnected ISDSmsSendService status of IBinder, service is null: " + (mISDSmsSendService == null));
			}

			public void onServiceDisconnected(ComponentName className) {
				mISDSmsSendService = null;
				if (SDSmsConsts.DEBUG_SERVICE) Log.w(TAG, "initServices.onServiceDisconnected ISDSmsSendService status of IBinder, service is null: " + (mISDSmsSendService == null));
			}
		};
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices svcConn getting send service");
		//Intent sendServiceIntent = new Intent(SEND_SERVICE_ACTION);
		Intent sendServiceIntent = new Intent();
		sendServiceIntent.setClassName(SEND_SERVICE_PACKAGE, SEND_SERVICE);
		sendServiceIntent.putExtra(SDSmsConsts.SDSMS_ID, sdsmsID);
		sendServiceIntent.putExtra(SDSmsConsts.SDSMS_PACKAGE_NAME, packageName);
		sendServiceIntent.putExtra(SDSmsConsts.SDSMS_RECEIVER_NAME, mSendReceiver);
		mContext.bindService(sendServiceIntent, svcConn, Context.BIND_AUTO_CREATE);
		mSDSmsReceiveCallbackConn = new SDSmsReceiveCallbackConnection(mContext, mSendReceiver, mISDSmsReceiveCallback);
		if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "initServices mSDSmsReceiveCallbackConn getting receive service");
		//Intent receiveServiceIntent = new Intent(RECEIVE_SERVICE_ACTION);
		Intent receiveServiceIntent = new Intent();
		receiveServiceIntent.setClassName(RECEIVE_SERVICE_PACKAGE, RECEIVE_SERVICE);
		receiveServiceIntent.putExtra(SDSmsConsts.SDSMS_ID, sdsmsID);
		receiveServiceIntent.putExtra(SDSmsConsts.SDSMS_TYPE, sdsmsType);
		receiveServiceIntent.putExtra(SDSmsConsts.SDSMS_PACKAGE_NAME, packageName);
		receiveServiceIntent.putExtra(SDSmsConsts.SDSMS_RECEIVER_NAME, mReceiveReceiver);
		mContext.bindService(receiveServiceIntent, mSDSmsReceiveCallbackConn, Context.BIND_AUTO_CREATE);
		waitForServiceConnection();
	}

	private void removeServices() {
		waitingForServices = true;
		if (svcConn != null) {
			mContext.unbindService(svcConn);
			svcConn = null;
		}
		if (mSDSmsReceiveCallbackConn != null) {
			mContext.unbindService(mSDSmsReceiveCallbackConn);
			mSDSmsReceiveCallbackConn = null;
		}
	}

	public void setReceiverCallback(SDSmsReceiveCallback callback) {
		tempISDSmsReceiveCallback = callback;
		newCallbackThread = new Thread(null, setNewCallbackMonitor, "newCallbackMonitor Thread : " + TAG);
		newCallbackThread.start();
	}
	
	public void monitorMessages(String id, String packageName, String smsReceiver) {
		Intent intent = new Intent();
		intent.setClassName(RECEIVE_SERVICE_PACKAGE, RECEIVE_SERVICE);
		intent.putExtra(SDSmsConsts.SDSMS_ID, id);
		intent.putExtra(SDSmsConsts.SDSMS_TYPE, sdsmsType);
		intent.putExtra(SDSmsConsts.SDSMS_PACKAGE_NAME, packageName);
		intent.putExtra(SDSmsConsts.SDSMS_RECEIVER_NAME, smsReceiver);
		try {
			mISDSmsReceiveCallback = mSDSmsReceiveCallbackConn.getCallBack();
			mISDSmsReceiveCallback.testCallbackConnection(TAG + ".monitorMessages testing");
			mISDSmsReceiveService.registerReceiver(id, sdsmsType, intent, mISDSmsReceiveCallback);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendTextMessage(
			String destinationAddress, String scAddress, String text,
			PendingIntent sentIntent, PendingIntent deliveryIntent) {
		if (isSdsmsDefaultSmsApp()) {
			//sendMsg.setClassName(SEND_SERVICE_PACKAGE, SEND_SERVICE);
			//SDSmsManager.mContext.startService(new Intent(SEND_SERVICE));
			//SDSmsManager.mContext.sendBroadcast(sendMsg);
			try {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendTextMessage using service");
				mISDSmsSendService.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
			} catch (RemoteException e) {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendTextMessage broadcast intent");
				Intent sendMsg = new Intent(SEND_SERVICE);
				sendMsg = new Intent(SDSMS_SEND_TEXT);
				sendMsg.putExtra(DEST, destinationAddress);
				sendMsg.putExtra(SVC_CENTER, scAddress);
				sendMsg.putExtra(MESSAGE, text);
				sendMsg.putExtra(SENT_INTENT, sentIntent);
				sendMsg.putExtra(DELIVERY_INTENT, deliveryIntent);
				SDSmsManager.mContext.sendBroadcast(sendMsg);
				e.printStackTrace();
			}
			try {
				SDSmsManager.mContext.unbindService(svcConn);
			} catch (Exception e) {
				// do nothing... apparently it wasn't bound
			} finally {
				initServices();
			}
		} else {
			sManager.sendTextMessage(destinationAddress, scAddress, text, sentIntent, deliveryIntent);
		}
	}

	public void sendMultipartTextMessage(
			String destinationAddress, String scAddress, ArrayList<String> parts,
			ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
		if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, "sending message");
		if (isSdsmsDefaultSmsApp()) {
			//sendMsg.setClassName(SEND_SERVICE_PACKAGE, SEND_SERVICE);
			//SDSmsManager.mContext.startService(new Intent(SEND_SERVICE));
			//SDSmsManager.mContext.bindService(sendMsg, svcConn, Context.BIND_AUTO_CREATE);
			//SDSmsManager.mContext.sendBroadcast(sendMsg);
			try {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendTextMessage using service");
				mISDSmsSendService.sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
			} catch (RemoteException e) {
				if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendTextMessage broadcast intent");
				Intent sendMsg = new Intent(SEND_SERVICE);
				sendMsg = new Intent(SDSMS_SEND_MULTIPART_TEXT);
				sendMsg.putExtra(DEST, destinationAddress);
				sendMsg.putExtra(SVC_CENTER, scAddress);
				sendMsg.putExtra(MESSAGES, parts);
				sendMsg.putExtra(SENT_INTENTS, sentIntents);
				sendMsg.putExtra(DELIVERY_INTENTS, deliveryIntents);
				if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".sendMultipartTextMessage broadcast intent");
				SDSmsManager.mContext.sendBroadcast(sendMsg);
				e.printStackTrace();
			}
			try {
				SDSmsManager.mContext.unbindService(svcConn);
			} catch (Exception e) {
				// do nothing... apparently it wasn't bound
			} finally {
				initServices();
			}
		} else {
			sManager.sendMultipartTextMessage(destinationAddress, scAddress, parts, sentIntents, deliveryIntents);
		}
	}

	public void sendDataMessage(
			String destinationAddress, String scAddress, short destinationPort,
			byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent) {
		if (isSdsmsDefaultSmsApp()) {
			Intent sendMsg = new Intent(SDSMS_SEND_DATA);
			sendMsg.putExtra(DEST, destinationAddress);
			sendMsg.putExtra(SVC_CENTER, scAddress);
			sendMsg.putExtra(PORT, destinationPort);
			sendMsg.putExtra(MSG_DATA, data);
			sendMsg.putExtra(SENT_INTENT, sentIntent);
			sendMsg.putExtra(DELIVERY_INTENT, deliveryIntent);
			if (SDSmsConsts.DEBUG_SEND_SMS) Log.i(TAG, ".sendDataMessage broadcast intent");
			SDSmsManager.mContext.sendBroadcast(sendMsg);
			try {
				SDSmsManager.mContext.unbindService(svcConn);
			} catch (Exception e) {
				// do nothing... apparently it wasn't bound
			} finally {
				initServices();
			}
		} else {
			sManager.sendDataMessage(destinationAddress, scAddress, destinationPort, data, sentIntent, deliveryIntent);
		}
	}

	public ArrayList<String> divideMessage(String text) {
		return sManager.divideMessage(text);
	}

	public static String contentMms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return SDSmsConsts.SDMMS_URI.toString();
		else return SDSmsConsts.MMS_URI.toString();
	}

	public static String startsWithMms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return "sdmms";
		else return "mms";
	}

	public static String getTypeFromAuthority(String authority) {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp() && authority.equals("sdmms")) return "mms";
		else return authority;
	}

	public static String contentSms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentSms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return SDSmsConsts.SDSMS_URI.toString();
		else return SDSmsConsts.SMS_URI.toString();
	}

	public static String contentMmsSms() {
		if (SDSmsConsts.DEBUG_DEFAULT) Log.i(TAG, "contentMmsSms sdsmsManagerDefault: " + sdsmsManagerDefault);
		if (isSdsmsDefaultSmsApp()) return SDSmsConsts.SDCONTENT_URI.toString();
		else return SDSmsConsts.CONTENT_URI.toString();
	}

	public static final class Sms {

		private Sms () {
		}

		private static final Uri CONTENT_URI = Uri.parse("content://sms");
		private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms");

		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		public static final class Inbox {
			private Inbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/inbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/inbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Sent {
			private Sent() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/sent");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/sent");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Draft {
			private Draft() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/draft");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/draft");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Outbox {
			private Outbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://sms/outbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/outbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Conversations {
			private static final Uri CONTENT_URI = Uri.parse("content://sms/conversations");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdsms/conversations");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}
	}

	public static final class Mms {

		private Mms() {
		}

		private static final Uri CONTENT_URI = Uri.parse("content://mms");
		private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms");

		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		private static final Uri REPORT_REQUEST_URI = Uri.withAppendedPath(CONTENT_URI, "report-request");
		private static final Uri SDREPORT_REQUEST_URI = Uri.withAppendedPath(SDCONTENT_URI, "report-request");

		public static Uri REPORT_REQUEST_URI() {
			if (isSdsmsDefaultSmsApp()) return SDREPORT_REQUEST_URI;
			else return REPORT_REQUEST_URI;
		}

		private static final Uri REPORT_STATUS_URI = Uri.withAppendedPath(CONTENT_URI, "report-status");
		private static final Uri SDREPORT_STATUS_URI = Uri.withAppendedPath(SDCONTENT_URI, "report-status");

		public static Uri REPORT_STATUS_URI() {
			if (isSdsmsDefaultSmsApp()) return SDREPORT_STATUS_URI;
			else return REPORT_STATUS_URI;
		}

		public static final String DEFAULT_SORT_ORDER = "date DESC";

		public static final class Inbox {
			private Inbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://mms/inbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/inbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Sent {
			private Sent() {
			}

			private static final Uri CONTENT_URI = Uri.parse("content://mms/sent");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/sent");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Draft {
			private Draft() {
			}

			private static final Uri CONTENT_URI = Uri.parse("content://mms/drafts");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/drafts");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}

		public static final class Outbox {
			private Outbox() {
			}
			
			private static final Uri CONTENT_URI = Uri.parse("content://mms/outbox");
			private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms/outbox");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}
	}

	public static final class MmsSms {
		private MmsSms() {
		}

		private static final Uri CONTENT_URI = Uri.parse("content://mms-sms/");
		private static final Uri SDCONTENT_URI = Uri.parse("content://sdmms-sdsms/");
		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}
		
		private static final Uri CONTENT_DRAFT_URI = Uri.parse("content://mms-sms/draft");
		private static final Uri SDCONTENT_DRAFT_URI = Uri.parse("content://sdmms-sdsms/draft");
		public static Uri CONTENT_DRAFT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_DRAFT_URI;
			else return CONTENT_DRAFT_URI;
		}
		
		private static final Uri CONTENT_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
		private static final Uri SDCONTENT_CONVERSATIONS_URI = Uri.parse("content://sdmms-sdsms/conversations");
		public static Uri CONTENT_CONVERSATIONS_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_CONVERSATIONS_URI;
			else return CONTENT_CONVERSATIONS_URI;
		}

		private static final Uri CONTENT_FILTER_BYPHONE_URI = Uri.parse("content://mms-sms/messages/byphone");
		private static final Uri SDCONTENT_FILTER_BYPHONE_URI = Uri.parse("content://sdmms-sdsms/messages/byphone");
		public static Uri CONTENT_FILTER_BYPHONE_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_FILTER_BYPHONE_URI;
			else return CONTENT_FILTER_BYPHONE_URI;
		}

		private static final Uri CONTENT_UNDELIVERED_URI = Uri.parse("content://mms-sms/undelivered");
		private static final Uri SDCONTENT_UNDELIVERED_URI = Uri.parse("content://sdmms-sdsms/undelivered");
		public static Uri CONTENT_UNDELIVERED_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_UNDELIVERED_URI;
			else return CONTENT_UNDELIVERED_URI;
		}

		private static final Uri CONTENT_LOCKED_URI = Uri.parse("content://mms-sms/locked");
		private static final Uri SDCONTENT_LOCKED_URI = Uri.parse("content://sdmms-sdsms/locked");
		public static Uri CONTENT_LOCKED_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_LOCKED_URI;
			else return CONTENT_LOCKED_URI;
		}

		private static final Uri SEARCH_URI = Uri.parse("content://mms-sms/search");
		private static final Uri SDSEARCH_URI = Uri.parse("content://sdmms-sdsms/search");
		public static Uri SEARCH_URI() {
			if (isSdsmsDefaultSmsApp()) return SDSEARCH_URI;
			else return SEARCH_URI;
		}


		public static final class PendingMessages {
			private PendingMessages() {
			}
			
			private static final Uri CONTENT_URI = Uri.withAppendedPath(MmsSms.CONTENT_URI, "pending");
			private static final Uri SDCONTENT_URI = Uri.withAppendedPath(MmsSms.SDCONTENT_URI, "pending");

			public static Uri CONTENT_URI() {
				if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
				else return CONTENT_URI;
			}
		}
	}

	public static final class Threads {
		private Threads() {
		}

		private static final Uri THREAD_ID_CONTENT_URI = Uri.parse("content://mms-sms/threadID");
		private static final Uri SDTHREAD_ID_CONTENT_URI = Uri.parse("content://sdmms-sdsms/threadID");

		public static Uri THREAD_ID_CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDTHREAD_ID_CONTENT_URI;
			else return THREAD_ID_CONTENT_URI;
		}

		private static final Uri CONTENT_URI = Uri.withAppendedPath(MmsSms.CONTENT_URI, "conversations");
		private static final Uri SDCONTENT_URI = Uri.withAppendedPath(MmsSms.SDCONTENT_URI, "conversations");

		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		private static final Uri OBSOLETE_THREADS_URI = Uri.withAppendedPath(CONTENT_URI, "obsolete");
		private static final Uri SDOBSOLETE_THREADS_URI = Uri.withAppendedPath(SDCONTENT_URI, "obsolete");

		public static Uri OBSOLETE_THREADS_URI() {
			if (isSdsmsDefaultSmsApp()) return SDOBSOLETE_THREADS_URI;
			else return OBSOLETE_THREADS_URI;
		}

	}

	public static final class Rate {
		private Rate() {
		}

		private static final Uri CONTENT_URI = Uri.withAppendedPath(Mms.CONTENT_URI, "rate");
		private static final Uri SDCONTENT_URI = Uri.withAppendedPath(Mms.SDCONTENT_URI, "rate");
		
		public static Uri CONTENT_URI() {
			if (isSdsmsDefaultSmsApp()) return SDCONTENT_URI;
			else return CONTENT_URI;
		}

		public static final String SENT_TIME = "sent_time";
	}
	
}