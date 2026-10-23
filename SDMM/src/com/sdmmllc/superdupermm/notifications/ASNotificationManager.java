package com.sdmmllc.superdupermm.notifications;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupermm.data.SDDB;
import com.sdmmllc.superdupermm.data.SDDBConn;
import com.sdmmllc.superdupermm.scan.SDSmsAppInstallMonitor;
import com.sdmmllc.superdupermm.ui.Act_Home;
import com.sdmmllc.superdupersmsmanager.SDSmsManagerApp;
import com.sdmmllc.superdupersmsmanager.SdmmsConsts;

public class ASNotificationManager {
	static String TAG = "ASNotificationManager";
	public static String 
		BASIC_NOTIFY = "1", 
		BG_SCAN_PROGRESS_NOTIFY = "2", 
		BG_SCAN_COMPLETE_CLEAR_NOTIFY = "3", 
		BG_SCAN_COMPLETE_WARNING_NOTIFY = "4", 
		SCAN_PROGRESS_NOTIFY = "5", 
		SCAN_COMPLETE_CLEAR_NOTIFY = "6", 
		SCAN_COMPLETE_WARNING_NOTIFY = "7", 
		INSTALL_SCAN_PROGRESS_NOTIFY = "8", 
		INSTALL_SCAN_COMPLETE_CLEAR_NOTIFY = "9", 
		INSTALL_SCAN_COMPLETE_WARNING_NOTIFY = "10",
		CONFIDENTIAL_EMAIL_REMOVED_NOTIFY = "11", 
		SPYWARE_APP_RECLASSIFIED_NOTIFY = "12",

		PASSWORD_CHANGE_NOTIFICATION = "13", 
		CONFIDENTIAL_EMAIL_CHANGE_NOTIFICATION = "14",
		UNINSTALL_ANTISPY_NOTIFICATION = "15", 
		SUBSCRIPTION_CANCELLED_NOTIFICATION = "16",
		SUBSCRIPTION_TRANSFER_NOTIFICATION = "17", 
		RISKLIST_DEFINITIONS_UPDATE_NOTIFICATION = "18",
		NEW_APP_VERSION_NOTIFICATION = "19",
	
		SERVER_ERROR_NOTIFICATION = "30", 
		NETWORK_ERROR_NOTIFICATION = "31", 
		COMM_ERROR_NOTIFICATION = "32",
				
		UNINSTALL_NO_COMPATIBLE_SMS_APPS_NOTIFY = "39", 
		UNINSTALL_SCAN_PROGRESS_NOTIFY = "40", 
		UNINSTALL_SCAN_COMPLETE_CLEAR_NOTIFY = "41", 
		UNINSTALL_SCAN_COMPLETE_WARNING_NOTIFY = "42";
		
	public static String 
		NOTIFY_PREFS = "NotifyPrefs", 
		NOTIFY_IMMEDIATE_FLG = "immediateFlg",
		NOTIFY_DELAYED_FLG = "delayed", 
		NOTIFY_DELAY_SPAN = "delayAmount", 
		NOTIFY_EMAIL_FLG = "EmailFlg", 
		NOTIFY_CONFIDENTIAL_EMAIL_FLG = "ConfidentialEmailFlg", 
		NOTIFY_STATUS_BAR_FLG = "StatusBar", 
		NOTIFY_SOUND_FLG = "SoundFlg", 
		NOTIFY_LIGHT_FLG = "LightFlg", 
		NOTIFY_VIBRATE_FLG = "VibrateFlg",
		NOTIFY_SCAN_PROGRESS_FLG = "ScanProgressFlg", 
		NOTIFY_SCAN_COMPLETE_FLG = "ScanCompleteFlg",
		NOTIFY_INSTALL_FLG = "InstallFlg", 
		NOTIFY_DISPLAY_ALERTS_FLG = "ALertsFlg";
	
	public static String SCAN_ALARM = "ScanAlarm", DELAYED_ALARM = "DelayedAlarm";
	
	public static String
		SCAN_FREQUENCY = "ScanFreq", 
		// scan frequency options
		SCAN_MANUAL = "Manual", SCAN_DAILY = "Daily", SCAN_WEEKLY = "Weekly", SCAN_MONTHLY = "Monthly",
		// scan time
		SCAN_HOUR = "ScanHour", SCAN_MINUTE = "ScanMin", 
		// scan event options
		NOTIFY_SPYWARE = "SpywareNotify", NOTIFY_SAFE = "SafeNotify", NOTIFY_ALL = "AllNotify";
	
	Calendar test = Calendar.getInstance();
	static java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT);
		
    private Context spaContext; 
    private SDDB spaDB;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String userid = "";
	private static SpaXmlNotificationCoder coder;

	ArrayList<SpaUserNotification> mNotifications = new ArrayList<SpaUserNotification>();
	
	public ASNotificationManager (Context context) {
		spaContext = context;
		spaDB = SDSmsManagerApp.getSDDB();
        SDDBConn conn = spaDB.open(false);
        //userid = spaDB.getUserId();
        mNotifications = spaDB.getUserNotificationList();
        spaDB.close(conn);
        resetBackgroundScan();
        coder = new SpaXmlNotificationCoder(spaContext);
	}
	
	public static ASNotificationManager getNotificationManager(Context context) {
		return SDSmsManagerApp.getNotificationManager();
	}
	
	public static void sendScanNotifications(final Context context, final String type) {
		sendScanNotifications(context, type, null);
	}
	
	public static void sendScanNotifications(
			final Context context, final String type, final String appName) {
        Thread thread = new Thread(null, new Runnable() {
	            @Override
	            public void run() {
//	        		if (SdmmsConsts.DEBUG_NOTIFICATION) 
	        			Log.i(TAG, "sendScanNotifications: sending scan notification");
	            	//SpaTextDB spaDB = SpaTextDB.getSpaTextDB(context);
	            	//SDDBConn conn = spaDB.open(false);
	            	//String userid = spaDB.getUserId();
	            	//String confidentialEmail = spaDB.getUserAccount(userid).confidentialEmailAddr;
	            	//spaDB.close(conn);
	        			// TODO
	            	//SpaUserAccount tmpUser = SpaUserAccount.getUserAccount(context);
	            	SpaUserNotification tmpNotification = new SpaUserNotification(type);
	            	tmpNotification.setAppName(appName);
	            	//if (tmpUser.getConfidentialEmailAddr().length() > 0) {
	            	//	tmpNotification.confidentialEmailAddress = tmpUser.getConfidentialEmailAddr();
	            	//	tmpNotification.hasConfidentialEmailAddress = true;
	            	//}
	            	
	            	ASNotificationManager.setupNotification(context, tmpNotification);
	            	//TODO
	            	//if (tmpNotification.hasConfidentialEmailAddress) 
	            	//	ASNotificationManager.confidentialEmailNotification(tmpNotification, tmpUser.getAccountId());
	            	if (tmpNotification.statusbar&&!tmpNotification.delayedNotify)
	            		ASNotificationManager.sendNotification(tmpNotification, context);
	            	if (tmpNotification.statusbar&&tmpNotification.delayedNotify) {
	            		Calendar cal = Calendar.getInstance();
	            		cal.add(Calendar.HOUR, 2);
	            		ASNotificationManager.delayedNotification(cal, context, tmpNotification.getType());
	            	}
//	        		if (SdmmsConsts.DEBUG_NOTIFICATION) 
	            		Log.i(TAG, "sendScanNotifications: done sending scan notification");
	            }
        	}, "SendingNotifications");
        thread.start();
	}

	public static void sendInstallScanNotification(final Context context, final String type, final String appName) {
        Thread thread = new Thread(null, new Runnable() {
	            @Override
	            public void run() {
	            	SDDB spaDB = SDSmsManagerApp.getSDDB();
	            	SDDBConn conn = spaDB.open(false);
	            	//String userid = spaDB.getUserId();
	            	spaDB.close(conn);
	            	//TODO
	            	//SpaUserNotification tmpNotification = new SpaUserNotification(userid);
	            	SpaUserNotification tmpNotification = new SpaUserNotification("");
	            	tmpNotification.setType(type);
	            	tmpNotification.setAppName(appName);
	            	ASNotificationManager.setupNotification(context, tmpNotification);
	            	//if (tmpNotification.hasConfidentialEmailAddress) 
	            	//	ASNotificationManager.confidentialEmailNotification(tmpNotification, userid);
	            	if (tmpNotification.statusbar&&!tmpNotification.delayedNotify)
	            		ASNotificationManager.sendNotification(tmpNotification, context);
	            	if (tmpNotification.statusbar&&tmpNotification.delayedNotify) {
	            		Calendar cal = Calendar.getInstance();
	            		cal.add(Calendar.HOUR, 2);
	            		ASNotificationManager.delayedNotification(cal, context, tmpNotification.getType());
	            	}
	            }
        	}, "SendingNotifications");
        thread.start();
	}

	public static void sendDelayedInstallScanNotification(final Context context, final String type) {
        Thread thread = new Thread(null, new Runnable() {
	            @Override
	            public void run() {
	            	SDDB spaDB = SDSmsManagerApp.getSDDB();
	            	SDDBConn conn = spaDB.open(false);
	            	//TODO
	            	//String userid = spaDB.getUserId();
	            	spaDB.close(conn);
	            	//TODO
	            	//SpaUserNotification tmpNotification = new SpaUserNotification(userid);
	            	SpaUserNotification tmpNotification = new SpaUserNotification("");
	            	tmpNotification.setType(type);
	            	ASNotificationManager.setupNotification(context, tmpNotification);
	            	//if (tmpNotification.hasConfidentialEmailAddress) 
	            	//	ASNotificationManager.confidentialEmailNotification(tmpNotification, userid);
	            	if (tmpNotification.statusbar)
	            		ASNotificationManager.sendNotification(tmpNotification, context);
	            }
        	}, "SendingNotifications");
        thread.start();
	}

	private String getUserId() {
		if (userid.length() < 1) {
	        SDDBConn conn = spaDB.open(false);
	        //TODO
	        //userid = spaDB.getUserId();
	        spaDB.close(conn);
		}
		return userid;
	}
	
	public void clearNotificationMonitor(int notify_id) {
		if (SdmmsConsts.DEBUG_NOTIFICATION) 
			Log.i(TAG, "clearNotificationMonitor: clear notification monitor notify_id: " + notify_id);
		SDDBConn conn = spaDB.open(true);
		if (notify_id > 0) {
			for (SpaUserNotification tmpNotify : mNotifications) {
				if (tmpNotify.notification_id == notify_id) {
					spaDB.deleteNotification(notify_id+"");
				}
			}
		} else {
			if (SdmmsConsts.DEBUG_NOTIFICATION) 
				Log.i(TAG, "clearNotificationMonitor: clearing all notifications");
			for (SpaUserNotification tmpNotify : mNotifications) {
				spaDB.deleteNotification(tmpNotify.notification_id+"");
			}
		}
		mNotifications = spaDB.getUserNotificationList();
		spaDB.close(conn);
	}
	
	public void resetNotificationMonitor() {
		if (SdmmsConsts.DEBUG_NOTIFICATION) 
			Log.i(TAG, "resetNotificationMonitor: reseting notifications");
		SDDBConn conn = spaDB.open(true);
		mNotifications = spaDB.getUserNotificationList();
		spaDB.close(conn);
	}
	
	private boolean validOptionFlg(String option) {
		if (option.equals(ASNotificationManager.NOTIFY_VIBRATE_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_DISPLAY_ALERTS_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_CONFIDENTIAL_EMAIL_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_IMMEDIATE_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_DELAYED_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_EMAIL_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_INSTALL_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_LIGHT_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_SCAN_COMPLETE_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_SCAN_PROGRESS_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_SOUND_FLG)) return true;
		if (option.equals(ASNotificationManager.NOTIFY_STATUS_BAR_FLG)) return true;
		return false;
	}
	
	private boolean validFrequency(String frequency, int value) {
		if (!(frequency.equals(SCAN_MONTHLY)||frequency.equals(SCAN_WEEKLY)||
				frequency.equals(SCAN_DAILY)||frequency.equals(SCAN_MANUAL))) return false;
		if (frequency.equals(SCAN_MONTHLY)) {
			if (value > 28) return false;
		} else if (frequency.equals(SCAN_WEEKLY)){
			if (value > 7 || value < 1) return false;
		}
		return true;
	}
	
	private boolean validTime(int hour, int min) {
		if (hour > 23) return false;
		if (min > 59) return false;
		return true;
	}
	
	public void setNotificationFrequency(String frequency, int value, int hour, int min) {
		if (!validFrequency(frequency, value)) return;
		if (!validTime(hour, min)) return;
    	SharedPreferences.Editor editor = spaContext.getSharedPreferences(NOTIFY_PREFS, 0).edit();
    	editor.putString(SCAN_FREQUENCY, frequency);
    	editor.putInt(frequency, value);
    	editor.putInt(SCAN_HOUR, hour);
    	editor.putInt(SCAN_MINUTE, min);
    	editor.commit();
    	if (frequency.equals(SCAN_MONTHLY)) setMonthlyScan(value, hour, min);
    	else if (frequency.equals(SCAN_WEEKLY)) setWeeklyScan(value, hour, min);
    	else if (frequency.equals(SCAN_DAILY)) setDailyScan(hour, min); 
    	else cancelScan();
	}
	
	public void resetBackgroundScan() {
    	SharedPreferences settings = spaContext.getSharedPreferences(NOTIFY_PREFS, 0);
    	if (settings.contains(SCAN_FREQUENCY)&&settings.contains(SCAN_HOUR)&&settings.contains(SCAN_MINUTE)) {
	    	String frequency = settings.getString(SCAN_FREQUENCY, "");
	    	int value = settings.getInt(frequency, 0);
	    	int hour = settings.getInt(SCAN_HOUR, 0);
	    	int min = settings.getInt(SCAN_MINUTE, 0);
	    	if (frequency.equals(SCAN_MONTHLY)) setMonthlyScan(value, hour, min);
	    	else if (frequency.equals(SCAN_WEEKLY)) setWeeklyScan(value, hour, min);
	    	else if (frequency.equals(SCAN_DAILY)) setDailyScan(hour, min); 
	    	else cancelScan();
    	}
	}
	
	private Calendar scanCal = Calendar.getInstance();
	
	private void setMonthlyScan(int day, int hour, int min) {
		scanCal.set(scanCal.get(Calendar.YEAR), scanCal.get(Calendar.MONTH) + 1, day, hour, min);
		setScan(scanCal);
	}
	
	private void setWeeklyScan(int dayOfWeek, int hour, int min) {
		scanCal = Calendar.getInstance();
		int dayOfMonth = scanCal.get(Calendar.DAY_OF_MONTH);
		int newDayOfWeek = scanCal.get(Calendar.DAY_OF_WEEK) - dayOfWeek;
		int hourDiff = scanCal.get(Calendar.HOUR_OF_DAY) - hour;
		int minDiff = scanCal.get(Calendar.MINUTE) - min;
		if (newDayOfWeek == 0) {
			if (hourDiff == 0) {
				if (minDiff == 0 || minDiff > 0) dayOfMonth += 7;
			} else if (hourDiff > 0) dayOfMonth += 7;
		} else if (newDayOfWeek > 0) dayOfMonth += 7 - newDayOfWeek;
		scanCal.set(scanCal.get(Calendar.YEAR), scanCal.get(Calendar.MONTH),
				dayOfMonth, hour, min);
		setScan(scanCal);
	}
	
	private void setDailyScan(int hour, int min) {
		scanCal = Calendar.getInstance();
		int dayOfMonth = 0;
		int hourDiff = scanCal.get(Calendar.HOUR_OF_DAY) - hour;
		int minDiff = scanCal.get(Calendar.MINUTE) - min;
		if (hourDiff == 0) {
			if (minDiff == 0 || minDiff > 0) dayOfMonth += 1;
		} else if (hourDiff > 0) dayOfMonth += 1;
		scanCal.set(scanCal.get(Calendar.YEAR), scanCal.get(Calendar.MONTH), 
				scanCal.get(Calendar.DAY_OF_MONTH) + dayOfMonth, hour, min);
		setScan(scanCal);
	}

	private void setScan(Calendar cal) {
//    	if (SpaTextConsts.debugLevel > 7) 
    		Log.i(TAG, "getScanHistoryList: setScan: Date/Time: " + df.format(cal.getTimeInMillis()));
		AlarmManager alarmMgr = (AlarmManager)spaContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(spaContext, SDSmsAppInstallMonitor.class);
		intent.setAction(SCAN_ALARM);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(spaContext, 0, intent, 0);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
	}
	
	private void cancelScan() {
		AlarmManager alarmMgr = (AlarmManager)spaContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(spaContext, ASNotificationMonitor.class);
		intent.setAction(SCAN_ALARM);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(spaContext, 0, intent, 0);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), pendingIntent);
	}
	
	private static void delayedNotification(Calendar cal, Context context, String type) {
		AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, ASNotificationMonitor.class);
		intent.setAction(DELAYED_ALARM);
		intent.putExtra(ASNotificationManager.NOTIFY_DELAYED_FLG, type);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
	}
	
	public void setNotificationEvent(String event, boolean notify) {
		if (!(event.equals(NOTIFY_SPYWARE)||event.equals(NOTIFY_SAFE)||event.equals(NOTIFY_ALL))) return;
    	SharedPreferences.Editor editor = spaContext.getSharedPreferences(NOTIFY_PREFS, 0).edit();
    	editor.putBoolean(event, notify);
    	editor.commit();
	}
	
	public void setNotificationFlg(String notify_option, boolean flag) {
        if (validOptionFlg(notify_option)) {
        	SharedPreferences.Editor editor = spaContext.getSharedPreferences(NOTIFY_PREFS, 0).edit();
        	editor.putBoolean(notify_option, flag);
        	editor.commit();
        }
	}
	
	public void setDelayedTimeSpan(long value) {
       	SharedPreferences.Editor editor = spaContext.getSharedPreferences(NOTIFY_PREFS, 0).edit();
       	editor.putLong(ASNotificationManager.NOTIFY_DELAY_SPAN, value);
       	editor.commit();
	}
	
	public static void alertNotification(Context context, SpaUserNotification tmpNotification) {
		if (!tmpNotification.alertNotify) return;
		SpaUserNotification.showDialog(context, tmpNotification.dialogTitle, tmpNotification.dialogMsg);
	}
	
	public static boolean emailNotification(SpaUserNotification tmpNotification, String userid) {
		if (!tmpNotification.hasEmailAddress) return false;
		coder.emailNotification(tmpNotification, userid);
		return tmpNotification.emailSent_status;
	}
	
	public boolean confidentialEmailNotification(SpaUserNotification tmpNotification) {
		return ASNotificationManager.confidentialEmailNotification(tmpNotification, userid);
	}
	
	public static boolean confidentialEmailNotification(SpaUserNotification tmpNotification, String userid) {
//		if (SdmmsConsts.DEBUG_NOTIFICATION) 
			Log.i(TAG, "confidentialEmailNotification: checking address");
		if (!tmpNotification.hasConfidentialEmailAddress) return false;
//		if (SdmmsConsts.DEBUG_NOTIFICATION) 
			Log.i(TAG, "confidentialEmailNotification: sending email notification");
		coder.confidentialEmailNotification(tmpNotification, userid);
		return tmpNotification.emailSent_status;
	}
	
	public static void sendNotification(SpaUserNotification tmpNotification, Context context) {
		if (!tmpNotification.statusbar) return;
       	SharedPreferences settings = context.getSharedPreferences(NOTIFY_PREFS, 0);
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(context, Act_Home.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		int icon = SpaTextNotification.getIcon(tmpNotification.statusbarIcon);
		Notification notification;
		boolean notifyMe = false;
		String ticker = (String) tmpNotification.statusbarTicker;
		ticker = ticker.replaceAll("APPNAME", "\"" + tmpNotification.getAppName() + "\"");
		ticker = ticker.replaceAll("TIMESTAMP", " " + df.format(Calendar.getInstance().getTime()) + " ");
		String title = (String) tmpNotification.statusbarTitle;
		title = title.replaceAll("APPNAME", "\"" + tmpNotification.getAppName() + "\"");
		title = title.replaceAll("TIMESTAMP", " " + df.format(Calendar.getInstance().getTime()) + " ");
		String statusbar = (String) tmpNotification.statusbarText;
		statusbar = statusbar.replaceAll("APPNAME", "\"" + tmpNotification.getAppName() + "\"");
		statusbar = statusbar.replaceAll("TIMESTAMP", " " + df.format(Calendar.getInstance().getTime()) + " ");
		CharSequence tickerText = ticker.subSequence(0, ticker.length());
		CharSequence contentTitle = title.subSequence(0, title.length());
		CharSequence contentText = statusbar.subSequence(0, statusbar.length());
		if (settings.getBoolean(ASNotificationManager.NOTIFY_STATUS_BAR_FLG, true)) {
			notifyMe = true;
			long when = System.currentTimeMillis();
			notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		} else {
			notification = new Notification();
		}
		if (settings.getBoolean(ASNotificationManager.NOTIFY_SOUND_FLG, false)) {
			notification.defaults |= Notification.DEFAULT_SOUND;
			notifyMe = true;
		}
		if (settings.getBoolean(ASNotificationManager.NOTIFY_VIBRATE_FLG, false)) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notifyMe = true;
		}
		if (settings.getBoolean(ASNotificationManager.NOTIFY_LIGHT_FLG, true)) {
			notification.defaults |= Notification.DEFAULT_LIGHTS;
		    notification.flags |= Notification.FLAG_SHOW_LIGHTS; 
		    notifyMe = true;
		}
		if (notifyMe) {
			notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
			mNotificationManager.notify(icon, notification);
		} else {
			mNotificationManager.notify(icon, new Notification());
		}
	}
	
    public static boolean setupNotification(Context context, SpaUserNotification tmpNotification) {
    	SharedPreferences settings = context.getSharedPreferences(NOTIFY_PREFS, 0);
    	tmpNotification.alertNotify = settings.getBoolean(ASNotificationManager.NOTIFY_DISPLAY_ALERTS_FLG, true);
    	tmpNotification.hasEmailAddress = settings.getBoolean(ASNotificationManager.NOTIFY_EMAIL_FLG, true);
    	tmpNotification.hasConfidentialEmailAddress = settings.getBoolean(ASNotificationManager.NOTIFY_CONFIDENTIAL_EMAIL_FLG, false);
    	tmpNotification.immediateNotify = settings.getBoolean(ASNotificationManager.NOTIFY_IMMEDIATE_FLG, true);
    	tmpNotification.delayedNotify = settings.getBoolean(ASNotificationManager.NOTIFY_DELAYED_FLG, false);
    	tmpNotification.light = settings.getBoolean(ASNotificationManager.NOTIFY_LIGHT_FLG, true);
    	tmpNotification.sound = settings.getBoolean(ASNotificationManager.NOTIFY_SOUND_FLG, false);
    	tmpNotification.statusbar = settings.getBoolean(ASNotificationManager.NOTIFY_STATUS_BAR_FLG, true);
    	tmpNotification.vibrate = settings.getBoolean(ASNotificationManager.NOTIFY_VIBRATE_FLG, false);
    	tmpNotification.statusbarIcon = SpaTextNotification.sb_icon1;
    	if (tmpNotification.getType().equals(BASIC_NOTIFY)) {
    		
    	} else if (tmpNotification.getType().equals(ASNotificationManager.BG_SCAN_PROGRESS_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.bgScanProgressNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.bgScanProgressNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.bgScanProgressNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.bgScanProgressNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.bgScanProgressNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.bgScanProgressNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.bgScanProgressNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.BG_SCAN_COMPLETE_CLEAR_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.bgScanClearNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.bgScanClearNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.bgScanClearNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.bgScanClearNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.bgScanClearNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.bgScanClearNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.bgScanClearNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.BG_SCAN_COMPLETE_WARNING_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.bgScanAlertNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.bgScanAlertNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.bgScanAlertNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.bgScanAlertNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.bgScanAlertNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.bgScanAlertNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.bgScanAlertNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.SCAN_PROGRESS_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.realTimeScanProgressNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.realTimeScanProgressNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.realTimeScanProgressNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.realTimeScanProgressNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.realTimeScanProgressNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.realTimeScanProgressNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.realTimeScanProgressNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.SCAN_COMPLETE_CLEAR_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.realTimeScanClearNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.realTimeScanClearNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.realTimeScanClearNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.realTimeScanClearNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.realTimeScanClearNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.realTimeScanClearNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.realTimeScanClearNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.SCAN_COMPLETE_WARNING_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.realTimeScanAlertNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.realTimeScanAlertNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.realTimeScanAlertNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.realTimeScanAlertNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.realTimeScanAlertNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.realTimeScanAlertNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.realTimeScanAlertNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.INSTALL_SCAN_PROGRESS_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.installScanProgressNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.installScanProgressNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.installScanProgressNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.installScanProgressNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.installScanProgressNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.installScanProgressNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.installScanProgressNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.INSTALL_SCAN_COMPLETE_CLEAR_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.installScanClearNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.installScanClearNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.installScanClearNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.installScanClearNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.installScanClearNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.installScanClearNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.installScanClearNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.INSTALL_SCAN_COMPLETE_WARNING_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.installScanAlertNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.installScanAlertNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.installScanAlertNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.installScanAlertNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.installScanAlertNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.installScanAlertNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.installScanAlertNotifyStatusTitle);

    	} else if (tmpNotification.getType().equals(ASNotificationManager.UNINSTALL_SCAN_PROGRESS_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.uninstallScanProgressNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.uninstallScanProgressNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.uninstallScanProgressNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.uninstallScanProgressNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.uninstallScanProgressNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.uninstallScanProgressNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.uninstallScanProgressNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.UNINSTALL_NO_COMPATIBLE_SMS_APPS_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.uninstallScanNoCompatibleSmsAppsNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.UNINSTALL_SCAN_COMPLETE_CLEAR_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.uninstallScanClearNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.uninstallScanClearNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.uninstallScanClearNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.uninstallScanClearNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.uninstallScanClearNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.uninstallScanClearNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.uninstallScanClearNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.UNINSTALL_SCAN_COMPLETE_WARNING_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.uninstallScanAlertNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.uninstallScanAlertNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.uninstallScanAlertNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.uninstallScanAlertNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.uninstallScanAlertNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.uninstallScanAlertNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.uninstallScanAlertNotifyStatusTitle);

    	} else if (tmpNotification.getType().equals(ASNotificationManager.CONFIDENTIAL_EMAIL_REMOVED_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.confidentialEmailRemovedNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.confidentialEmailRemovedNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.confidentialEmailRemovedNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.confidentialEmailRemovedNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.confidentialEmailRemovedNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.confidentialEmailRemovedNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.confidentialEmailRemovedNotifyStatusTitle);
    		tmpNotification.hasConfidentialEmailAddress = true;
    	} else if (tmpNotification.getType().equals(ASNotificationManager.SPYWARE_APP_RECLASSIFIED_NOTIFY)) {
    		tmpNotification.emailBody = context.getString(R.string.spywareReclassifyNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.spywareReclassifyNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.spywareReclassifyNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.spywareReclassifyNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.spywareReclassifyNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.spywareReclassifyNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.spywareReclassifyNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.SERVER_ERROR_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.serverErrorNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.serverErrorNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.serverErrorNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.serverErrorNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.serverErrorNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.serverErrorNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.serverErrorNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.NETWORK_ERROR_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.networkErrorNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.networkErrorNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.networkErrorNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.networkErrorNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.networkErrorNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.networkErrorNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.networkErrorNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.COMM_ERROR_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.commErrorNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.commErrorNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.commErrorNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.commErrorNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.commErrorNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.commErrorNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.commErrorNotifyStatusTitle);

    	} else if (tmpNotification.getType().equals(ASNotificationManager.PASSWORD_CHANGE_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.passwordChangeNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.passwordChangeNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.passwordChangeNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.passwordChangeNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.passwordChangeNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.passwordChangeNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.passwordChangeNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.CONFIDENTIAL_EMAIL_CHANGE_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.confidentialEmailChangeNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.confidentialEmailChangeNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.confidentialEmailChangeNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.confidentialEmailChangeNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.confidentialEmailChangeNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.confidentialEmailChangeNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.confidentialEmailChangeNotifyStatusTitle);
    		tmpNotification.hasConfidentialEmailAddress = true;
    	} else if (tmpNotification.getType().equals(ASNotificationManager.RISKLIST_DEFINITIONS_UPDATE_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.riskListUpdateNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.riskListUpdateNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.riskListUpdateNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.riskListUpdateNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.riskListUpdateNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.riskListUpdateNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.riskListUpdateNotifyStatusTitle);
    	} else if (tmpNotification.getType().equals(ASNotificationManager.NEW_APP_VERSION_NOTIFICATION)) {
    		tmpNotification.emailBody = context.getString(R.string.appUpdateNotifyEmailMessage);
    		tmpNotification.emailSubject = context.getString(R.string.appUpdateNotifyEmailSubject);
    		tmpNotification.dialogMsg = context.getString(R.string.appUpdateNotifyMsg);
    		tmpNotification.dialogTitle = context.getString(R.string.appUpdateNotifyTitle);
    		tmpNotification.statusbarText = context.getString(R.string.appUpdateNotifyStatusMessage);
    		tmpNotification.statusbarTicker = context.getString(R.string.appUpdateNotifyStatusTicker);
    		tmpNotification.statusbarTitle = context.getString(R.string.appUpdateNotifyStatusTitle);
    	}
    	return false;
    }
}
