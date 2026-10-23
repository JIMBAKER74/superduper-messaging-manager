package com.android.vending.billing.test;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.android.vending.billing.IMarketBillingService;

public class TestMarketBillingService implements IMarketBillingService {
	enum BillingRequestType {
		CHECK_BILLING_SUPPORTED, 
		REQUEST_PURCHASE, 
		GET_PURCHASE_INFORMATION, 
		CONFIRM_NOTIFICATIONS, 
		RESTORE_TRANSACTIONS,
	}
	
	private Context context;
	private ResponseIntentFactory responseIntentFactory;
	
	public TestMarketBillingService(Context context) {
		this.context = context;
		this.responseIntentFactory = new ResponseIntentFactory();
	}
	
	public Bundle sendBillingRequest(Bundle bundle) {
		if (!bundle.containsKey("BILLING_REQUEST"))
			throw new Error("BILLING_REQUEST is required");
		if (!bundle.containsKey("API_VERSION"))
			throw new Error("API_VERSION is required");
		if (!bundle.containsKey("PACKAGE_NAME"))
			throw new Error("PACKAGE_NAME is required");
		
		Bundle response = null;
		
		BillingRequestType billingRequestType = BillingRequestType.valueOf((String)bundle.get("BILLING_REQUEST"));
		switch (billingRequestType) {
			case CHECK_BILLING_SUPPORTED:
			{
				response = createSyncResponse(ResponseCode.RESULT_OK);
				break;
			}
			case REQUEST_PURCHASE:
			{
				if (!bundle.containsKey("ITEM_ID"))
					throw new Error("ITEM_ID is required");

				String developerPayload = bundle.getString("DEVELOPER_PAYLOAD");
				String itemId = bundle.getString("ITEM_ID");	
				String packageName = bundle.getString("PACKAGE_NAME");
				
				long requestId = System.currentTimeMillis();
				response = createSyncResponse(ResponseCode.RESULT_OK, requestId);
				response.putParcelable("PURCHASE_INTENT", createMarketScreenPendingIntent(itemId, developerPayload, packageName, requestId));
				break;
			}
			case GET_PURCHASE_INFORMATION:
			{
				if (!bundle.containsKey("NONCE"))
					throw new Error("NONCE is required");
				if (!bundle.containsKey("NOTIFY_IDS"))
					throw new Error("NOTIFY_IDS is required");
				
				long nonce = bundle.getLong("NONCE");
				String[] notifyIds = bundle.getStringArray("NOTIFY_IDS");
				
				PurchaseState state = new PurchaseState();
				state.setNonce(nonce);
				for (String notifyId : notifyIds) {
					PurchaseStateOrder[] orders = OrderNotificationRepository.getInstance().get(notifyId);
					for (PurchaseStateOrder order : orders)
						state.addOrder(order);
				}					
				broadcastPurchaseStateChangeIntent(state);					
				
				response = createSyncResponse(ResponseCode.RESULT_OK);
				break;
			}
			case CONFIRM_NOTIFICATIONS:
			{
				if (!bundle.containsKey("NOTIFY_IDS"))
					throw new Error("NOTIFY_IDS is required");
				
				String[] notifyIds = bundle.getStringArray("NOTIFY_IDS");
				for (String notifyId : notifyIds) {
					OrderNotificationRepository.getInstance().remove(notifyId);
				}
				
				response = createSyncResponse(ResponseCode.RESULT_OK);
				break;
			}
			case RESTORE_TRANSACTIONS:
			{
				if (!bundle.containsKey("NONCE"))
					throw new Error("NONCE is required");
				
				PurchaseStateOrder[] orders = PurchaseStateOrderRepository.getInstance().getUserManagedPurchases();
				String notificationId = OrderNotificationRepository.getInstance().add(orders);
				context.sendBroadcast(responseIntentFactory.createAppNotificationIntent(notificationId));
				
				response = createSyncResponse(ResponseCode.RESULT_OK);
				break;
			}
			default:
			{
				response = createSyncResponse(ResponseCode.RESULT_DEVELOPER_ERROR);
				break;
			}
		}
		
		if (billingRequestType != BillingRequestType.REQUEST_PURCHASE)
			context.sendBroadcast(responseIntentFactory.createResponseCodeIntent(response));
		return response;
	}
	
	private Bundle createSyncResponse(ResponseCode code) {
		return createSyncResponse(code, System.currentTimeMillis());
	}
	
	private Bundle createSyncResponse(ResponseCode code, long requestId) {
		Bundle bundle = new Bundle();
		bundle.putInt("RESPONSE_CODE", code.getValue());
		bundle.putLong("REQUEST_ID", requestId);
		return bundle;
	}
	
	private void broadcastPurchaseStateChangeIntent(PurchaseState purchaseState) {
		String purchaseDataJson = purchaseState.toJson();
		Intent intent = new Intent("com.android.vending.billing.PURCHASE_STATE_CHANGED");
		intent.putExtra("inapp_signed_data", purchaseDataJson);
		intent.putExtra("inapp_signature", PurchaseDataSigner.sign(purchaseDataJson));
		context.sendBroadcast(intent);	
	}
	
	private PendingIntent createMarketScreenPendingIntent(String itemId, String developerPayload, String packageName, long requestId) {		
		Intent intent = new Intent(context, TestPaymentScreenActivity.class);
		intent.putExtra(TestPaymentScreenActivity.DEVELOPER_PAYLOAD_INTENT_KEY, developerPayload);
		intent.putExtra(TestPaymentScreenActivity.ITEM_ID_INTENT_KEY, itemId);
		intent.putExtra(TestPaymentScreenActivity.PACKAGE_NAME_INTENT_KEY, packageName);
		intent.putExtra(TestPaymentScreenActivity.REQUEST_ID_INTENT_KEY, requestId);
		return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
	}

	@Override
	public IBinder asBinder() {
		// TODO Auto-generated method stub
		return null;
	}
	
}