package com.messagingappsllc.superdupermms.mms.transaction;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.droidprism.APN;
import com.droidprism.Carrier;

public class TransactionAPN {

	public static APN getAPN(String mcc, String mnc) {
		Carrier c = Carrier.getCarrier(mcc, mnc);
		if (c != null) {
		    APN a = c.getAPN();
		    if (a != null) {
		        String mmsc = a.mmsc;
		        String mmsproxy = a.proxy; //"" if none
		        int mmsport = a.port; //0 if none
		        return a;
		    }
		}
		return null;
	}
	
	public static APN getAPN(Context context) {
		TelephonyManager tel = (TelephonyManager) 
				context.getSystemService(Context.TELEPHONY_SERVICE);
		String networkOperator = tel.getNetworkOperator();

		if (networkOperator != null) {
			int mcc = Integer.parseInt(
					networkOperator.substring(0, 3));
			String s = networkOperator.substring(3);
			//remove leading zeroes
			int mnc = Integer.parseInt(
					s.replaceFirst("^0{1,2}",""));
			Carrier c = Carrier.getCarrier(mcc, mnc);
			c.getsmsemail();
			APN a = c.getAPN();
			return a;
		}
		return null;
	}
}
