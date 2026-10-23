package com.sdmmllc.superdupermm.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SDIntentFilters {
	
	public static String NO_FILTERS = "NoFilters";

	public static String ACTION_RESPOND_VIA_MESSAGE = "android.intent.action.RESPOND_VIA_MESSAGE";
	public static String ACTION_SEND = "android.intent.action.SEND";
	public static String ACTION_SENDTO = "android.intent.action.SENDTO";
	public static String SCHEME_SMSTO = "smsto";
	public static String SCHEME_SMS = "sms";
	public static String SCHEME_MMSTO = "mmsto";
	public static String SCHEME_MMS = "mms";
	
	public static String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static String SMS_DELIVERED = "android.provider.Telephony.SMS_DELIVER";
	
	public static final String WAP_PUSH_DELIVER = "android.provider.Telephony.WAP_PUSH_DELIVER";
	
	public static final String DEFAULT_CATEGORY = "android.intent.category.DEFAULT";
	

	private SDIntentFilters() {
	}
	
	public static final Map<String, Filter> Filters;
	static { 
		Map<String, Filter> filter = new HashMap<String, Filter>();
		filter.put(NO_FILTERS, new Filter(NO_FILTERS, "", ""));
		filter.put(ACTION_RESPOND_VIA_MESSAGE, new Filter(ACTION_RESPOND_VIA_MESSAGE, "", ""));
		filter.put(ACTION_SEND, new Filter(ACTION_SEND, "", ""));
		filter.put(ACTION_SENDTO, new Filter(ACTION_SENDTO, "", ""));
		filter.put(SCHEME_SMSTO, new Filter(SCHEME_SMSTO, "", ""));
		filter.put(SCHEME_SMS, new Filter(SCHEME_SMS, "", ""));
		filter.put(SMS_RECEIVED, new Filter(SMS_RECEIVED, "", ""));
		filter.put(SMS_DELIVERED, new Filter(SMS_DELIVERED, "", ""));
		filter.put(WAP_PUSH_DELIVER, new Filter(WAP_PUSH_DELIVER, "", ""));
		Filters = Collections.unmodifiableMap(filter);
	}
	
	public static class Filter {
		public String permDescr,
				permLbl,
				permName;
		
		public Filter(String name, String descr, String lbl) {
			permDescr = descr;
			permName = name;
			permLbl = lbl;
		}
		
	    @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((permDescr == null) ? 0 : permDescr.hashCode());
			result = prime * result
					+ ((permLbl == null) ? 0 : permLbl.hashCode());
			result = prime * result
					+ ((permName == null) ? 0 : permName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Filter other = (Filter) obj;
			if (permDescr == null) {
				if (other.permDescr != null)
					return false;
			} else if (!permDescr.equals(other.permDescr))
				return false;
			if (permLbl == null) {
				if (other.permLbl != null)
					return false;
			} else if (!permLbl.equals(other.permLbl))
				return false;
			if (permName == null) {
				if (other.permName != null)
					return false;
			} else if (!permName.equals(other.permName))
				return false;
			return true;
		}
	}
	

}
