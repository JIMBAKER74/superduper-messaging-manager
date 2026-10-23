package com.sdmmllc.superdupersmsmanager.sdk.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Set;

import android.app.Notification;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.provider.Telephony;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.android.sdmms.util.ClassDiscovery;
import com.sdmmllc.superdupersmsmanager.database.sqlite.SqliteWrapper;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsConsts;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;
import com.sdmmllc.superdupersmsmanager.sdk.providers.MmsProvider;
import com.sdmmllc.superdupersmsmanager.sdk.providers.MmsSmsProvider;
import com.sdmmllc.superdupersmsmanager.sdk.providers.SmsProvider;

import dalvik.system.PathClassLoader;

public class SdmmsUtil {
	public static String TAG = "SdmmsUtil";
	
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    public static boolean isDefaultSmsApp(Context context) {
        if (hasKitKat()) {
            return context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context));
        }

        return true;
    }
    
    public static Uri uriToMms(Uri uri) {
		if (SDSmsManager.isSdsmsDefaultSmsApp()) {
	    	if (MmsProvider.uriConvertMatch(uri)) return MmsProvider.convert(uri);
	    	if (MmsSmsProvider.uriConvertMatch(uri)) return MmsSmsProvider.convert(uri);
	    	if (SmsProvider.uriConvertMatch(uri)) return SmsProvider.convert(uri);
		}
    	return uri;
	}

	public static Uri uriToSdmms(Uri uri) {
		if (SDSmsManager.isSdsmsDefaultSmsApp()) {
	    	if (MmsProvider.uriInvertMatch(uri)) return MmsProvider.invert(uri);
	    	if (MmsSmsProvider.uriInvertMatch(uri)) return MmsSmsProvider.invert(uri);
	    	if (SmsProvider.uriInvertMatch(uri)) return SmsProvider.invert(uri);
		}
    	return uri;
	}

    public static final Uri uriConverter(Uri uri) {
		if (isDefaultSmsApp(SDSmsManager.getContext())) {
        	if (MmsProvider.uriConvertMatch(uri)) return MmsProvider.convert(uri);
        	if (MmsSmsProvider.uriConvertMatch(uri)) return MmsSmsProvider.convert(uri);
        	if (SmsProvider.uriConvertMatch(uri)) return SmsProvider.convert(uri);
        	return uri;
		} else {
        	if (MmsProvider.uriInvertMatch(uri)) return MmsProvider.invert(uri);
        	if (MmsSmsProvider.uriInvertMatch(uri)) return MmsSmsProvider.invert(uri);
        	if (SmsProvider.uriInvertMatch(uri)) return SmsProvider.invert(uri);
        	return uri;
		}
    }

	public static final String NOTIFICATION_BUILDER_ADDKIND = "com.sdmmllc.superdupersmsmanager.Notification.Builder.addkind";
	public static Notification.Builder addKind(Notification.Builder noti, String kind)   {
        try {
			Method m = Notification.Builder.class.getMethod("addKind", String.class);
			m.setAccessible(true);
			try {
				return (Notification.Builder) m.invoke(noti, kind);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Notification.Builder.addKind");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Notification.Builder.addKind");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Notification.Builder.addKind");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, ".addKind could not invoke method Notification.Builder.addKind");
            return null;
		}
        return null;
	}
	
	public static final String CONNECTIVITY_MANAGER_REQUESTROUTETOHOSTADDRESS = "com.sdmmllc.superdupersmsmanager.ConnectivityManager.requestRouteToHostAddress";
	public static final String CONNECTIVITY_MANAGER_REQUESTROUTETOHOSTADDRESS_NETWORK_TYPE = "networkType";
	public static final String CONNECTIVITY_MANAGER_REQUESTROUTETOHOSTADDRESS_HOST_ADDRESS = "hostAddress";
	
	public static boolean connMgrRequestRouteToHostAddress(ConnectivityManager cm, int networkType, InetAddress hostAddress)   {
        try {
			Method m = ConnectivityManager.class.getMethod("requestRouteToHostAddress", int.class, InetAddress.class);
			m.setAccessible(true);
			try {
				return (Boolean) m.invoke(cm, networkType, hostAddress);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method ConnectivityManager.requestRouteToHostAddress");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method ConnectivityManager.requestRouteToHostAddress");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method ConnectivityManager.requestRouteToHostAddress");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, ".connMgrRequestRouteToHostAddress could not invoke method ConnectivityManager.requestRouteToHostAddress");
            return false;
		}
        return false;
	}
	
	public static final String SMS_ADDMESSAGETOURI = "com.sdmmllc.superdupersmsmanager.Sms.addMessageToUri";
	public static final String SMS_ADDMESSAGETOURI_URI_STRING = "uri_string";
	public static final String SMS_ADDMESSAGETOURI_ADDRESS = "address";
	public static final String SMS_ADDMESSAGETOURI_BODY = "body";
	public static final String SMS_ADDMESSAGETOURI_SUBJECT = "subject";
	public static final String SMS_ADDMESSAGETOURI_DATE = "date";
	public static final String SMS_ADDMESSAGETOURI_READ = "read";
	public static final String SMS_ADDMESSAGETOURI_DELIVERY_REPORT = "deliveryReport";
	public static final String SMS_ADDMESSAGETOURI_THREADID = "threadId";
	public static final String SMS_ADDMESSAGETOURI_SUB_ID = "sub_id";
	
	public static Uri smsAddMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, 
			Long date, boolean read, boolean deliveryReport, Long threadId, int sub_id)   {
		Log.i(TAG, "smsAddMessageToUri: uri:" + uri.toString());
		if (isDefaultSmsApp(SDSmsManager.getContext())) {
	        try {
				Method m = Sms.class.getDeclaredMethod("addMessageToUri", 
						ContentResolver.class, 
						Uri.class, 
						String.class, String.class, String.class,
						Long.class, boolean.class, boolean.class, long.class, int.class);
				m.setAccessible(true);
				try {
					return (Uri) m.invoke(null, 
							resolver, 
							uri, 
							address, body, null, 
							date, read, deliveryReport, threadId, sub_id);
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.addMessageToUri");
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.addMessageToUri");
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.addMessageToUri");
				}
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				Log.e(TAG, ".smsAddMessage could not invoke method Sms.addMessageToUri");
	            return uri;
			}
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsAddMessageToUri called method by reflection");
	        return uri;
		} else {
			Intent addMessageToUri = new Intent(SMS_ADDMESSAGETOURI);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_URI_STRING, uri.toString());
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_ADDRESS, address);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_BODY, body);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_SUBJECT, subject);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_DATE, date);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_READ, read);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_DELIVERY_REPORT, deliveryReport);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_THREADID, threadId);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_SUB_ID, sub_id);
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsAddMessageToUri broadcast intent");
			SDSmsManager.getContext().sendBroadcast(addMessageToUri);
			return uri;
		}
	}
	
	public static Uri smsAddMessageToUri(ContentResolver resolver, Uri uri, String address, String body, String subject, 
			Long date, boolean read, boolean deliveryReport, long threadId)   {
		Log.i(TAG, "smsAddMessageToUri: uri:" + uri.toString());
		if (isDefaultSmsApp(SDSmsManager.getContext())) {
			if (LogTag.DEBUG_SEND) {
				Log.i(TAG, "uri = " + uri + "  address = " + address + "  body: " + body + "\nsubject:" + subject
						+ " date: " + date + " read: " + read + " deliveryReport: " + deliveryReport 
						+ " threadId: " + threadId);
			}
	        try {
				Method m = Sms.class.getDeclaredMethod("addMessageToUri", 
						ContentResolver.class, 
						Uri.class, 
						String.class, String.class, String.class,
						Long.class, boolean.class, boolean.class, long.class);
				m.setAccessible(true);
				try {
					Uri retval = (Uri) m.invoke(null, resolver, 
							uri, 
							address, body, null, 
							date, read, deliveryReport, threadId);
					if (LogTag.DEBUG_SEND) {
						Log.i(TAG, "addMessageToUri: " + retval);
					} 
					return retval;
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.addMessageToUri");
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.addMessageToUri");
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.addMessageToUri");
				}
			} catch (NoSuchMethodException e1) {
				Class cls = Sms.class;
				Log.i(TAG, "Class is " + cls);
				for(Method method : cls.getDeclaredMethods())
					Log.i(TAG, ""+method);
				e1.printStackTrace();
				Log.e(TAG, ".smsAddMessageToUri could not invoke method Sms.addMessageToUri");
	            return uri;
			}
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsAddMessageToUri called method by reflection");
	        return uri;
		} else {
			Intent addMessageToUri = new Intent(SMS_ADDMESSAGETOURI);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_URI_STRING, uri.toString());
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_ADDRESS, address);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_BODY, body);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_SUBJECT, subject);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_DATE, date);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_READ, read);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_DELIVERY_REPORT, deliveryReport);
			addMessageToUri.putExtra(SMS_ADDMESSAGETOURI_THREADID, threadId);
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsAddMessageToUri broadcast intent");
			SDSmsManager.getContext().sendBroadcast(addMessageToUri);
			return uri;
		}
	}
	
	public static final String SMS_INBOX_ADDMESSAGE = "com.sdmmllc.superdupersmsmanager.Sms.Inbox.addMessage";
	public static final String SMS_INBOX_ADDMESSAGE_ADDRESS = "address";
	public static final String SMS_INBOX_ADDMESSAGE_BODY = "body";
	public static final String SMS_INBOX_ADDMESSAGE_SUBJECT = "subject";
	public static final String SMS_INBOX_ADDMESSAGE_DATE = "date";
	public static final String SMS_INBOX_ADDMESSAGE_READ = "read";

	public static String smsInboxAddMessage(ContentResolver resolver, String address, String body, String subject, Long date)  {
		Log.i(TAG, "smsInboxAddMessage: date:" + date);
		if (isDefaultSmsApp(SDSmsManager.getContext())) {
	        try {
				Method m = Sms.Inbox.class.getMethod("addMessage", ContentResolver.class, String.class, String.class, String.class,
						Long.class, boolean.class);
				m.setAccessible(true);
				try {
					return (String) m.invoke(null, resolver, address, body, null, date, false);
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.Inbox.addMessage");
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.Inbox.addMessage");
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.Inbox.addMessage");
				}
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				Log.e(TAG, ".smsInboxAddMessage could not invoke method Sms.Inbox.addMessage");
	            return address;
			}
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsInboxAddMessage called method by reflection");
	        return address; 
		} else {
			Intent addMessage = new Intent(SMS_INBOX_ADDMESSAGE);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_ADDRESS, address);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_BODY, body);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_SUBJECT, subject);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_DATE, date);
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsInboxAddMessage broadcast intent");
			SDSmsManager.getContext().sendBroadcast(addMessage);
			return address;
		}
	}
	
	public static String smsInboxAddMessage(ContentResolver resolver, String address, String body, String subject, Long date, boolean read)  {
		Log.i(TAG, "smsInboxAddMessage: date:" + date);
		if (isDefaultSmsApp(SDSmsManager.getContext())) {
	        try {
				Method m = Sms.Inbox.class.getMethod("addMessage", ContentResolver.class, String.class, String.class, String.class,
						Long.class, Boolean.class);
				m.setAccessible(true);
				try {
					return (String) m.invoke(null, resolver, address, body, null, date, read);
				} catch (IllegalAccessException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.Inbox.addMessage");
				} catch (IllegalArgumentException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.Inbox.addMessage");
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
					Log.e(TAG, "error with return value of reflected method Sms.Inbox.addMessage");
				}
			} catch (NoSuchMethodException e1) {
				e1.printStackTrace();
				Log.e(TAG, ".smsInboxAddMessage could not invoke method Sms.Inbox.addMessage");
	            return address;
			}
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsInboxAddMessage called method by reflection");
	        return address;
		} else {
			Intent addMessage = new Intent(SMS_INBOX_ADDMESSAGE);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_ADDRESS, address);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_BODY, body);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_SUBJECT, subject);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_DATE, date);
			addMessage.putExtra(SMS_INBOX_ADDMESSAGE_READ, read);
			if (SDSmsConsts.DEBUG_UTILS) Log.i(TAG, ".smsInboxAddMessage broadcast intent");
			SDSmsManager.getContext().sendBroadcast(addMessage);
			return address;
		}
	}
	
	public static final String SMSMESSAGE_FRAGMENTTEXT= "com.sdmmllc.superdupersmsmanager.SmsMessage.fragmentText";
	public static final String SMSMESSAGE_FRAGMENTTEXT_TEXT = "text";
	
	public static ArrayList<String> smsMessageFragmentText(String text) {
        try {
			Method m = SmsMessage.class.getMethod("fragmentText", String.class);
			m.setAccessible(true);
			try {
				return (ArrayList<String>) m.invoke(null, text);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.fragmentText");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.fragmentText");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.fragmentText");
			}
		} catch (NoSuchMethodException e1) {

			e1.printStackTrace();
			Log.e(TAG, ".smsMessageFragmentText could not invoke method Mms.fragmentText");
            return new ArrayList<String>(0);
		}
        return new ArrayList<String>(0);
	}
	
	public static final String MMS_EXTRACTADDRSPEC = "com.sdmmllc.superdupersmsmanager.Mms.extractAddrSpec";
	public static final String MMS_EXTRACTADDRSPEC_ADDRESS = "address";
	
	public static String mmsextractAddrSpec(String address) {
        try {
			Method m = Mms.class.getMethod("extractAddrSpec", String.class);
			m.setAccessible(true);
			try {
				return (String) m.invoke(null, address);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.extractAddrSpec");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.extractAddrSpec");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.extractAddrSpec");
			}
		} catch (NoSuchMethodException e1) {

			e1.printStackTrace();
			Log.e(TAG, ".mmsextractAddrSpec could not invoke method Mms.extractAddrSpec");
            return address;
		}
        return address;
	}
	
	public static final String MMS_ISEMAILADDRESS = "com.sdmmllc.superdupersmsmanager.Mms.isEmailAddress";
	public static boolean mmsisEmailAddress(String number) {
        try {
			Method m = Mms.class.getMethod("isEmailAddress", String.class);
			m.setAccessible(true);
			try {
				return (Boolean) m.invoke(null, number);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isEmailAddress");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isEmailAddress");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isEmailAddress");
			}
		} catch (NoSuchMethodException e1) {

			e1.printStackTrace();
			Log.e(TAG, ".mmsisEmailAddress could not invoke method Mms.isEmailAddress");
            return false;
		}
        return false;
	}
	
	public static final String SMS_MOVEMESSAGETOFOLDER = "com.sdmmllc.superdupersmsmanager.Sms.moveMessageToFolder";
	public static final String SMS_MOVEMESSAGETOFOLDER_URI_STRING = "uri_string";
	public static final String SMS_MOVEMESSAGETOFOLDER_FOLDER = "folder";
	public static final String SMS_MOVEMESSAGETOFOLDER_ERROR = "error";
	
	public static boolean smsmoveMessageToFolder(Context context, Uri uri, int folder, int error) {
		if (LogTag.DEBUG_SEND)
			Log.i(TAG, "uri==null: " + (uri==null) + " - uri = " + uri + " context==null: " + (context==null));
        if (uri == null) {
            return false;
        }

        boolean moved = false;
        boolean markAsUnread = false;
        boolean markAsRead = false;
        switch(folder) {
        case Consts.MESSAGE_TYPE_INBOX:
        case Consts.MESSAGE_TYPE_DRAFT:
            break;
        case Consts.MESSAGE_TYPE_OUTBOX:
        case Consts.MESSAGE_TYPE_SENT:
            markAsRead = true;
            break;
        case Consts.MESSAGE_TYPE_FAILED:
        case Consts.MESSAGE_TYPE_QUEUED:
            markAsUnread = true;
            break;
        default:
            return false;
        }

        ContentValues values = new ContentValues(3);

        values.put(Consts.TYPE, folder);
        if (markAsUnread) {
            values.put(Consts.READ, 0);
        } else if (markAsRead) {
            values.put(Consts.READ, 1);
        }
        values.put(Consts.ERROR_CODE, error);

        boolean update = 1 == SqliteWrapper.update(context, context.getContentResolver(),
                uri, values, null, null);
		if (LogTag.DEBUG_SEND)
			Log.i(TAG, "update status: " + update);
		return update;
/**        
        try {
			Method m = Sms.class.getMethod("moveMessageToFolder", Context.class, Uri.class, int.class, int.class);
			m.setAccessible(true);
			try {
				moved = (Boolean) m.invoke(null, context, uri, folder, error);
				Log.i(TAG, "moved = " + moved);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Sms.moveMessageToFolder");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Sms.moveMessageToFolder");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Sms.moveMessageToFolder");
			}
		} catch (NoSuchMethodException e1) {

			e1.printStackTrace();
			Log.e(TAG, ".smsmoveMessageToFolder could not invoke method Sms.moveMessageToFolder");
            return moved;
		}
        return moved;**/
	}
	
	public static boolean smsisOutgoingFolder(Integer mBoxId) {
		return  (mBoxId == Consts.MESSAGE_TYPE_FAILED)
				|| (mBoxId == Consts.MESSAGE_TYPE_OUTBOX)
				|| (mBoxId == Consts.MESSAGE_TYPE_SENT)
				|| (mBoxId == Consts.MESSAGE_TYPE_QUEUED);
	}

	public static boolean isEmailAddress(String number) {
        try {
			Method m = Mms.class.getMethod("isEmailAddress", String.class);
			m.setAccessible(true);
			try {
				return (Boolean) m.invoke(null, number);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isEmailAddress");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isEmailAddress");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isEmailAddress");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method Mms.isEmailAddress");
		}
        return false;
	}

	public static final String MMS_ISPHONENUMBER = "com.sdmmllc.superdupersmsmanager.Mms.isPhoneNumber";
	public static boolean isPhoneNumber(String number) {
        try {
			Method m = Mms.class.getMethod("isPhoneNumber", String.class);
			m.setAccessible(true);
			try {
				return (Boolean) m.invoke(null, number);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isPhoneNumber");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isPhoneNumber");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Mms.isPhoneNumber");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method Mms.isPhoneNumber");
		}
        return false;
	}

	public static final String PHONE_NUMBER_UTILS_NORMALIZENUMBER = "com.sdmmllc.superdupersmsmanager.PhoneNumberUtils.normalizeNumber";
	public static String normalizeNumber(String number) {
        try {
			Method m = PhoneNumberUtils.class.getMethod("normalizeNumber", String.class);
			m.setAccessible(true);
			try {
				return (String) m.invoke(null, number);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.normalizeNumber");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.normalizeNumber");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.normalizeNumber");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method PhoneNumberUtils.normalizeNumber");
		}
        return PhoneNumberUtils.formatNumber(number);
	}

	public static final String PHONE_NUMBER_UTILS_FORMATNUMBERTOE164 = "com.sdmmllc.superdupersmsmanager.PhoneNumberUtils.formatNumberToE164";
	public static String formatNumberToE164(String number, String iso) {
        try {
			Method m = PhoneNumberUtils.class.getMethod("formatNumberToE164", String.class, String.class);
			m.setAccessible(true);
			try {
				return (String) m.invoke(null, number, iso);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumberToE164");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumberToE164");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumberToE164");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method PhoneNumberUtils.formatNumberToE164");
		}
        return PhoneNumberUtils.formatNumber(number);
	}

	public static final String PHONE_NUMBER_UTILS_FORMATNUMBER = "com.sdmmllc.superdupersmsmanager.PhoneNumberUtils.formatNumber";
	public static String formatNumber(String number, String iso) {
        try {
			Method m = PhoneNumberUtils.class.getMethod("formatNumber", String.class, String.class);
			m.setAccessible(true);
			try {
				return (String) m.invoke(null, number, iso);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumber");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumber");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumber");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method PhoneNumberUtils.formatNumber");
		}
        return PhoneNumberUtils.formatNumber(number);
	}

	public static String formatNumber(String number, String pref, String iso) {
        try {
			Method m = PhoneNumberUtils.class.getMethod("formatNumber", String.class, String.class, String.class);
			m.setAccessible(true);
			try {
				return (String) m.invoke(null, number, pref, iso);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumber");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumber");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.formatNumber");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method PhoneNumberUtils.formatNumber");
		}
        return PhoneNumberUtils.formatNumber(number);
	}

	public static final String PHONE_NUMBER_UTILS_COMPARELOOSELY = "com.sdmmllc.superdupersmsmanager.PhoneNumberUtils.compareLoosely";
	public static boolean compareLoosely(String number, String number2) {
        try {
			Method m = PhoneNumberUtils.class.getMethod("compareLoosely", String.class, String.class);
			m.setAccessible(true);
			try {
				return (Boolean) m.invoke(null, number, number2);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.compareLoosely");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.compareLoosely");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method PhoneNumberUtils.compareLoosely");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method PhoneNumberUtils.compareLoosely");
		}
        return false;
	}

	public static String replaceUnicodeDigits(String number) {
	    StringBuilder normalizedDigits = new StringBuilder(number.length());
	    for (char c : number.toCharArray()) {
	        int digit = Character.digit(c, 10);
	        if (digit != -1) {
	            normalizedDigits.append(digit);
	        } else {
	            normalizedDigits.append(c);
	        }
	    }
	    return normalizedDigits.toString();
	}

	public static final String NETWORK_UTILS_TRIMV4ADDRZEROS = "com.sdmmllc.superdupersmsmanager.NetworkUtils.trimV4AddrZeros";
	public static String trimV4AddrZeros(String addr) {
		if (addr == null) return null;
		String[] octets = addr.split("\\.");
		if (octets.length != 4) return addr;
		StringBuilder builder = new StringBuilder(16);
		String result = null;
		for (int i = 0; i < 4; i++) {
		    try {
		        if (octets[i].length() > 3) return addr;
		        builder.append(Integer.parseInt(octets[i]));
		    } catch (NumberFormatException e) {
		        return addr;
		    }
		    if (i < 3) builder.append('.');
		}
		result = builder.toString();
		return result;
	}

	public static final String THREADS_GETORCREATETHREADID = "com.sdmmllc.superdupersmsmanager.Threads.getOrCreateThreadId";
	public static long getOrCreateThreadId(Context context, String number) {
        try {
        	String apkName = context.getPackageManager().getApplicationInfo(
        			context.getApplicationContext().getPackageName(), 0).sourceDir;
        	PathClassLoader myPathClassLoader =
        		    new dalvik.system.PathClassLoader(
        		    apkName,
        		    ClassLoader.getSystemClassLoader());

    		//Class<?> handler = Class.forName("Threads", true, myPathClassLoader);
    		Class handler = Class.forName("android.provider.Telephony$Threads");
    		Method m = handler.getMethod("getOrCreateThreadId", Context.class, String.class);
			m.setAccessible(true);
			try {
				return (Long) m.invoke(null, context, number);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Threads.getOrCreateThreadId");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Threads.getOrCreateThreadId");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Threads.getOrCreateThreadId");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method Threads.getOrCreateThreadId");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "could not invoke method Threads.getOrCreateThreadId - package name not available");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "Threads class not found to invoke method NetworkUtils.getOrCreateThreadId");
		}
        return 0;
	}

	public static long getOrCreateThreadId(Context context, Set<String> recipients) {
        try {
        	String apkName = context.getPackageManager().getApplicationInfo(
        			context.getApplicationContext().getPackageName(), 0).sourceDir;
        	PathClassLoader myPathClassLoader =
        		    new dalvik.system.PathClassLoader(
        		    apkName,
        		    ClassLoader.getSystemClassLoader());

    		//Class<?> handler = Class.forName("android.provider.Telephony.Threads", true, myPathClassLoader);
    		Class handler = Class.forName("android.provider.Telephony$Threads");
    		Method m = handler.getMethod("getOrCreateThreadId", Context.class, Set.class);
			m.setAccessible(true);
			try {
				return (Long) m.invoke(null, context, recipients);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Threads.getOrCreateThreadId");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Threads.getOrCreateThreadId");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method Threads.getOrCreateThreadId");
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
			Log.e(TAG, "could not invoke method Threads.getOrCreateThreadId");
		} catch (NameNotFoundException e) {

			e.printStackTrace();
			Log.e(TAG, "could not invoke method Threads.getOrCreateThreadId - package name not available");
		} catch (ClassNotFoundException e) {
			try {
				ClassDiscovery.discover(TAG, "android.provider.Telephony");
				printClassInfo(Class.forName("android.provider.Telephony"));
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			Log.e(TAG, "Threads class not found to invoke method Threads.getOrCreateThreadId");
		}
        return 0;
	}

	public static final String GSMALPHABET_CHARTOGSM = "com.sdmmllc.superdupersmsmanager.GsmAlphabet.charToGsm";
	public static long charToGsm(Context context, char c, boolean throwException) {
        try {
        	String apkName = context.getPackageManager().getApplicationInfo(
        			context.getApplicationContext().getPackageName(), 0).sourceDir;
        	PathClassLoader myPathClassLoader =
        		    new dalvik.system.PathClassLoader(
        		    apkName,
        		    ClassLoader.getSystemClassLoader());

    		Class<?> handler = Class.forName("GsmAlphabet", true, myPathClassLoader);
    		Method m = handler.getMethod("charToGsm", char.class, boolean.class);
			m.setAccessible(true);
			try {
				return (Long) m.invoke(null, c, throwException);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method GsmAlphabet.charToGsm");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method GsmAlphabet.charToGsm");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method GsmAlphabet.charToGsm");
			}
		} catch (NoSuchMethodException e1) {

			e1.printStackTrace();
			Log.e(TAG, "could not invoke method GsmAlphabet.charToGsm");
		} catch (NameNotFoundException e) {

			e.printStackTrace();
			Log.e(TAG, "could not invoke method GsmAlphabet.charToGsm - package name not available");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "GsmAlphabet class not found to invoke method NetworkUtils.charToGsm");
		}
        return 0;
	}

	public static final String PHONENUMBERFORMATTINGTEXTWATCHER = "com.sdmmllc.superdupersmsmanager.PhoneNumberFormattingTextWatcher";
	public static PhoneNumberFormattingTextWatcher PhoneNumberFormattingTextWatcher(String country) {
        try {
        	Class<?> handler = Class.forName("android.telephony.PhoneNumberFormattingTextWatcher");
    		Constructor<?>[] constructors = handler.getDeclaredConstructors();
    		return (PhoneNumberFormattingTextWatcher) constructors[1].newInstance(country);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "PhoneNumberFormattingTextWatcher class not found");
		} catch (InstantiationException e) {
			e.printStackTrace();
			Log.e(TAG, "PhoneNumberFormattingTextWatcher class not found");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			Log.e(TAG, "PhoneNumberFormattingTextWatcher class illegal access atempt");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			Log.e(TAG, "PhoneNumberFormattingTextWatcher class illegal argument");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			Log.e(TAG, "PhoneNumberFormattingTextWatcher class invocation target exception");
		}
        return null;
	}
	
	public static void printClassInfo(Object myClass) {
		Class<?> clazz = myClass.getClass();
        Log.i(TAG, "Class info: " + myClass);
        // list with all inner classes of the class or interface
        Class<?>[] clazzes = clazz.getDeclaredClasses();
        for (int i=0; i<clazzes.length; i++) {
            System.out.println("Found class: " + clazzes[i]);
        }
         
        Method[] methods;
        // list with all public member methods of the class or interface
        methods = clazz.getMethods();
        for (int i=0; i<methods.length; i++) {
            System.out.println("Found public method: " + methods[i]);
        }
         
        // list with all member methods of the class or interface
        methods = clazz.getDeclaredMethods();
        for (int i=0; i<methods.length; i++) {
            System.out.println("Found method: " + methods[i]);
        }
	}
}
