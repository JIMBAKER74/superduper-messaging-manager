package com.sdmmllc.superdupermm.notifications;

import android.net.Uri;

import com.sdmmllc.superdupermessagingmanager.R;

public class SpaTextNotification {
	
	String phoneID;
	
	boolean statusbar = true, sound = false, vibrate = false, light = false;
	
	boolean lightDefault = true;
	int lightColor = 0xff00ff00, lightOn = 300, lightOff = 1000;
	
	boolean vibrateDefault = true;
	long[] vibratePattern = {0,100};
	
	boolean soundDefault = true;
	Uri soundFile = Uri.parse("");
	
	boolean statusbarDefaultIcon = true;
	int statusbarIcon = 0;
	boolean statusbarDefaultNotification = true;
	boolean statusbarTickerUseTextMsg = false;
	boolean statusbarDefaultTicker = true, statusbarDefaultTitle = true, statusbarDefaultText = true;
	CharSequence statusbarTicker = "", statusbarTitle = "", statusbarText = "";
	
	public SpaTextNotification(String str) {
		phoneID = str;
	}

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
