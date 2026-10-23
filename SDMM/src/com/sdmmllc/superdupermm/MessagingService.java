package com.sdmmllc.superdupermm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sdmmllc.superdupermm.data.AppRegistrationModel;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;


/**
 * This service is triggered internally only and is used to process incoming SMS and MMS messages
 * that the {@link com.example.android.smssample.receiver.MessagingReceiver} passes over. It&#39;s
 * preferable to handle these in a service in case there is significant work to do which may exceed
 * the time allowed in a receiver.
 */
public class MessagingService extends IntentService {
    public static final String TAG = "MessagingService";

    // These actions are for this app only and are used by MessagingReceiver to start this service
    public static final String ACTION_MY_RECEIVE_SMS = "com.sdmmllc.superdupersmsmanager.RECEIVE_SMS";
    public static final String ACTION_MY_RECEIVE_MMS = "com.sdmmllc.superdupersmsmanager.RECEIVE_MMS";
    public static final String ACTION_SEND_SMS = "android.intent.action.SEND";
    public static final String ACTION_SENDTO_SMS = "android.intent.action.SENDTO";
    
    private MessagingServiceModel serviceModel;
    private AppRegistrationModel mRegistrations;
    
    public MessagingService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
    	if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "creating service");
    	super.onCreate();
    	serviceModel = SDSmsManagerApp.getMessagingServiceModel();
    	mRegistrations = SDSmsManagerApp.getRegistrations();
    }

    @Override
    public IBinder onBind(Intent intent) {
    	if (serviceModel.getBinder() == null) serviceModel.setBinder(createBinder(intent));
		return serviceModel.getBinder().asBinder();
    }
    
    public ISDSmsReceiveService createBinder(Intent intent) {
		if (!intent.hasExtra(SDSmsConsts.SDSMS_ID) || !intent.hasExtra(SDSmsConsts.SDSMS_PACKAGE_NAME)
				|| !intent.hasExtra(SDSmsConsts.SDSMS_TYPE)
				|| !intent.hasExtra(SDSmsConsts.SDSMS_RECEIVER_NAME)) {
			Log.e(TAG, "service request missing SDSMS_ID or SDSMS_TYPE or SDSMS_PACKAGE_NAME or SDSMS_RECEIVER_NAME");
			return null;
		}
		final String callerId = intent.getStringExtra(SDSmsConsts.SDSMS_ID);
		final int type = intent.getIntExtra(SDSmsConsts.SDSMS_TYPE, SDSmsManager.ACCESS_TYPE_UNREGISTERED);
		String receiver = intent.getStringExtra(SDSmsConsts.SDSMS_RECEIVER_NAME);
		String pkgName = intent.getStringExtra(SDSmsConsts.SDSMS_PACKAGE_NAME);
		Log.i(TAG, "got receiver name: " + receiver + " pgkName: " + pkgName 
				+ " type: " + type + " callerId: " + callerId);
		Log.i(TAG, "got pkgName: " + pkgName);
        if (serviceModel.getBinder() == null) serviceModel.setBinder(getBinder(callerId));
        return serviceModel.getBinder();
    }
    
    private ISDSmsReceiveService getBinder(final String callerId) {
    	return new ISDSmsReceiveService.Stub() {
			@Override
			public boolean registerApp(Intent intent)
					throws RemoteException {
	        	Log.i(TAG, "onBind checking IBinder request type");
	        	if (!intent.hasExtra(SDSmsConsts.SDSMS_TYPE) || 
	        			!intent.hasExtra(SDSmsConsts.SDSMS_ID) || 
	        			!intent.hasExtra(SDSmsConsts.SDSMS_PACKAGE_NAME)) {
	            	Log.w(TAG, "onBind request type, SDSMS ID or package name missing, service not bound");
	            	return false;
	        	}
	        	int type = intent.getIntExtra(SDSmsConsts.SDSMS_TYPE, SDSmsManager.ACCESS_TYPE_UNREGISTERED);
	        	String id = intent.getStringExtra(SDSmsConsts.SDSMS_ID);
	        	Log.i(TAG, "onBind sending IBinder for type: " + type);
	        	mRegistrations.addRegistration(intent.getStringExtra(SDSmsConsts.SDSMS_PACKAGE_NAME));
	        	return serviceModel.registerApp(type, id, intent.getStringExtra(SDSmsConsts.SDSMS_PACKAGE_NAME));
			}
			
			@Override
    		public String registerReceiver(String id, int type, Intent intent, SDSmsReceiveCallback newCallback) 
    				throws RemoteException {
    			// TODO Auto-generated method stub
    			Log.i(TAG, "registerReceiver: setting callback for id: " + id + " bound by " + callerId);
    			Log.i(TAG, "registerReceiver: callback == null: " + (newCallback == null));
    			//mCallback.testCallbackConnection(TAG + ".getBinder successful for " + id);
    			String pkgName = intent.getStringExtra(SDSmsConsts.SDSMS_PACKAGE_NAME);
    			String receiver = intent.getStringExtra(SDSmsConsts.SDSMS_RECEIVER_NAME);
    			serviceModel.registerApp(type, id, pkgName);
    			boolean registered = serviceModel.isRegistered(type, id, pkgName);
    			if (!registered) {
        			Log.e(TAG, "registerReceiver failed and callback not set; Illegal attempt to access service");
    			}
    			Log.i(TAG, "registerReceiver: callback set; SDSmsReceiveService registered");
    			Log.i(TAG, "registerReceiver: callback pkgName: " + pkgName + " receiver: " + receiver);
    			MessagingServiceController controller = serviceModel.getControllers(type);
    	        controller.addCallback(id, pkgName, receiver, newCallback);
    			return "got it";
    		}

			@Override
			public String testConnection(String text) throws RemoteException {
    			Log.i(TAG, "testConnection: test string: " + text);
				return "Connection successful";
			}
        };
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
        	//TODO 
        	// persist msg
        	
			//serviceModel.checkControllers();
        	SDSmsManager.reconnect();
        	
        	int waitMax = 5000;
        	int waitTime = 0;
        	
        	Log.w(TAG, "SDSmsManager.isWaitingForServices(): " + SDSmsManager.isWaitingForServices());
    		synchronized(this) {
    			while(SDSmsManager.isWaitingForServices() && (waitTime < waitMax)) {
        			try {
        				wait(500);
        			} catch (Exception e) {
        				// do nothing
        			}
        			waitTime += 500;
                	Log.w(TAG, "SDSmsManager.isWaitingForServices(): " + SDSmsManager.isWaitingForServices());
        		}
        	}
        	
            String intentAction = intent.getAction();

        	if (ACTION_MY_RECEIVE_SMS.equals(intentAction)) {
            	Intent rebroadcast = new Intent(intent);
            	if (SDSmsManager.hasKitKat())
            		rebroadcast.setAction(SdmmsConsts.SMS_DELIVER);
            	else rebroadcast.setAction(SdmmsConsts.SMS_RECEIVED); 
            	Intent empty = new Intent(intent);
            	empty.setData(null);
            	empty.putExtras(new Bundle());
            	empty.putExtra(SDSmsConsts.SDSMS_DUPLICATE, SDSmsConsts.SDSMS_DUPLICATE);
            	empty.setAction(SdmmsConsts.SMS_RECEIVED);
            	Intent sendIntent;

            	// TODO: Handle incoming SMS
        		boolean abort = false;
        		int i = 0;
        		while (i <= SDSmsManager.ACCESS_TYPE_UNREGISTERED) {
        			if (i == SDSmsManager.ACCESS_TYPE_MESSAGING) 
        				sendIntent = rebroadcast;
        			else
        				sendIntent = empty;

        			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "rebroadcast.getAction():" + sendIntent.getAction());
        			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "onHandleIntent type:" + i + 
        					" serviceModel.getControllers(i).size(): " +
        					serviceModel.getControllers(i).size());
        			
        			// if the message aborts before it gets to a standard SMS app
        			// then ignore it, otherwise, continue as if nothing happened
        			// because standard SMS apps do not need to abort the message
        			if ((i < SDSmsManager.ACCESS_TYPE_MESSAGING) && !abort)
        				abort = serviceModel.getControllers(i).sendSMS(sendIntent);
        			else if (!abort) 
        				serviceModel.getControllers(i).sendSMSDeliver(sendIntent);
        			if (abort) {
        				Log.i(TAG, "onHandleIntent: sms aborted by controller group: " + i);
        			}
        			else Log.i(TAG, "onHandleIntent: sms not aborted by controller group: " + i);
        			i++;
        		}
                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                MessagingReceiver.completeWakefulIntent(intent);
            } else if (ACTION_MY_RECEIVE_MMS.equals(intentAction)) {
                // TODO: Handle incoming MMS

            	serviceModel.getControllers(SDSmsManager.ACCESS_TYPE_MESSAGING).sendMMS(intent);
                // Ensure wakelock is released that was created by the WakefulBroadcastReceiver
                MessagingReceiver.completeWakefulIntent(intent);
            } else if (ACTION_SEND_SMS.equals(intentAction) || ACTION_SENDTO_SMS.equals(intentAction)) {
    			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "reLaunch.getAction():" + intent.getAction());
            	serviceModel.getControllers(SDSmsManager.ACCESS_TYPE_MESSAGING).launchSMS(this);
                MessagingReceiver.completeWakefulIntent(intent);
            } else if (Intent.ACTION_MAIN.equals(intentAction)) {
    			if (SDSmsConsts.DEBUG_SERVICE) Log.i(TAG, "reLaunch.getAction():" + intent.getAction());
            	serviceModel.getControllers(SDSmsManager.ACCESS_TYPE_MESSAGING).launchSMS(this);
                MessagingReceiver.completeWakefulIntent(intent);
            }
        } 
    }
}