package com.sdmmllc.superdupermm.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;

import com.sdmmllc.superdupermessagingmanager.R;

public class SpaUserNotification {
	
	public SpaUserNotification(String str) {
		setType(str);
	}

	private String notification_type, appName = "";
		
	// notification scheduling
	public int notification_id = 0, timeOfDay = 0, dayOfWeek = 0, dayOfMonth = 0, delayPeriod = 0;
	public boolean immediateNotify = true, delayedNotify = false, alertNotify = false;
	public boolean emailSent_status = false, statusbar_status = false;
	
	// notification mechanisms and content
	public boolean hasEmailAddress = false, hasConfidentialEmailAddress = false;
	public String dialogTitle = "", dialogMsg = "", emailSubject = "", emailBody = "",
		confidentialEmailAddress = "", priorConfidentialEmailAddress = "";
	
	public String getAppName() {
		return appName;
	}

	public void setAppName(String app_name) {
		if (app_name != null) this.appName = app_name;
	}

	public String getType() {
		return notification_type;
	}
	
	public void setType(String notify_type) {
		notification_type = notify_type;
	}
	
	public static void showDialog(Context context, String title, String msg) {
    	AlertDialog.Builder dialog = new AlertDialog.Builder(context);
    	dialog.setIcon(android.R.drawable.ic_dialog_alert);
    	dialog.setTitle(title);
    	dialog.setMessage(msg);
    	dialog.setNeutralButton(context.getString(R.string.sysDone), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel(); 
			}
		});
    	dialog.show(); //Show the AlertDialog
	}
	
	public boolean statusbar = true, sound = false, vibrate = false, light = false;
	
	public boolean lightDefault = true;
	public int lightColor = 0xff00ff00, lightOn = 300, lightOff = 1000;
	
	public boolean vibrateDefault = true;
	public long[] vibratePattern = {0,100};
	
	public boolean soundDefault = true;
	public Uri soundFile = Uri.parse("");
	
	public boolean statusbarDefaultIcon = true;
	public int statusbarIcon = 0;
	public boolean statusbarDefaultNotification = true;
	public boolean statusbarTickerUseTextMsg = false;
	public boolean statusbarDefaultTicker = true, statusbarDefaultTitle = true, statusbarDefaultText = true;
	public CharSequence statusbarTicker = "", statusbarTitle = "", statusbarText = "";
	
    public static String tmpId = "000", notificationUpdated = "updatedData";

    public static int sb_icon1 = R.drawable.notification_icon, 
		sb_icon2 = R.drawable.notification_icon2,
		sb_icon3 = R.drawable.notification_icon3,
		sb_icon4 = R.drawable.notification_icon4,
		sb_icon5 = R.drawable.notification_icon5;
	
	public static int getIcon(int icon_id) {
		switch (icon_id) {
		case 0:
			return sb_icon1;
		case 1:
			return sb_icon2;
		case 2:
			return sb_icon3;
		case 3:
			return sb_icon4;
		case 4:
			return sb_icon5;
		default:
			return sb_icon1;
		}
	}
	
	public static int getIconId(int drawable_id) {
		if (drawable_id == R.drawable.notification_icon) return 0;
		else if (drawable_id == R.drawable.notification_icon2) return 1;
		else if (drawable_id == R.drawable.notification_icon3) return 2;
		else if (drawable_id == R.drawable.notification_icon4) return 3;
		else if (drawable_id == R.drawable.notification_icon5) return 4;
		else return -1;
	}
}
