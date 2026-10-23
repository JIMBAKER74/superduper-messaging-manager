package com.android.vending.billing.test;

import android.content.Intent;
import android.os.Bundle;

public class ResponseIntentFactory {
	public Intent createResponseCodeIntent(Bundle response) {
		return createResponseCodeIntent(response.getLong("REQUEST_ID"),
			ResponseCode.createByValue(response.getInt("RESPONSE_CODE")));
	}
	
	public Intent createResponseCodeIntent(long requestId, ResponseCode responseCode) {		
		Intent intent = new Intent("com.android.vending.billing.RESPONSE_CODE");
		intent.putExtra("request_id", requestId);
		intent.putExtra("response_code", responseCode.getValue());
		return intent;
	}
	
	public Intent createAppNotificationIntent(String notificationId) {
		Intent intent = new Intent("com.android.vending.billing.IN_APP_NOTIFY");
		intent.putExtra("notification_id", notificationId);
		return intent;		
	}

}
