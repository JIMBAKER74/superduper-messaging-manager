package com.sdmmllc.superdupermm.scan;


public class UserNotify {

	public static final String USER_NOTIFY_TBL = "SubscriptionNotification";
	
	public static final String[] USER_NOTIFY_COLUMNS = {
		"_id",
		"usr_notify_type",
		"usr_time_of_day",
		"usr_day_of_week",
		"usr_day_of_month",
		"usr_immediate_flg", // 5
		"usr_delayed_flg",
		"usr_delay_period",
		"usr_email",
		"usr_confidential_email",
		"phone_id", // 10
		"bar_flg",
		"def_icon_flg",
		"icon_id",
		"defbar_flg",
		"barusemsg_flg", //15
		"defticker_flg",
		"deftitle_flg",
		"deftext_flg",
		"barticker",
		"bartitle", // 20
		"bartext",
		"light_flg",
		"deflight_flg",
		"light_color",
		"light_on", //25
		"light_off",
		"vib_flg",
		"defvib_flg",
		"vib_pattern",
		"sound_flg", // 30
		"defsound_flg",
		"sound_path",
	};

}
