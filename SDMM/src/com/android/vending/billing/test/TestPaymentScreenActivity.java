package com.android.vending.billing.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class TestPaymentScreenActivity extends Activity {
	public static final String DEVELOPER_PAYLOAD_INTENT_KEY = "DEVELOPER_PAYLOAD";
	public static final String ITEM_ID_INTENT_KEY = "ITEM_ID";
	public static final String PACKAGE_NAME_INTENT_KEY = "PACKAGE_NAME";
	public static final String REQUEST_ID_INTENT_KEY = "REQUEST_ID";
	
	private ResponseIntentFactory responseIntentFactory = new ResponseIntentFactory();
	
	private Button createTestButton(String text, OnClickListener listener) {
        Button testButton = new Button(this);
        testButton.setText(text);
        testButton.setOnClickListener(listener);
        return testButton;
	}
	
	private void addTestButton(LinearLayout layout, String text, OnClickListener listener) {
		layout.addView(createTestButton(text, listener));
	}
	
	private String getDeveloperPayload() {
		return getIntent().getExtras().getString(DEVELOPER_PAYLOAD_INTENT_KEY);
	}
	private String getItemId() {
		return getIntent().getExtras().getString(ITEM_ID_INTENT_KEY);
	}
	private String getCallerPackageName() {
		return getIntent().getExtras().getString(PACKAGE_NAME_INTENT_KEY);
	}
	private long getRequestId() {
		return getIntent().getExtras().getLong(REQUEST_ID_INTENT_KEY);
	}
	
	private PurchaseStateOrder createOrder() {
		PurchaseStateOrder order = new PurchaseStateOrder();
		order.setDeveloperPayload(getDeveloperPayload());
		order.setPackageName(getCallerPackageName());
		order.setProductId(getItemId());
		return order;
	}
	
	private void notifyAboutOrder(PurchaseStateOrder order) {
		// request has reached the server
		sendBroadcastDelayed(responseIntentFactory.createResponseCodeIntent(getRequestId(), ResponseCode.RESULT_OK));
		
		// order notification
		String notificationId = OrderNotificationRepository.getInstance().add(order);
		sendBroadcastDelayed(responseIntentFactory.createAppNotificationIntent(notificationId));	
	}
	
	private void sendBroadcastDelayed(final Intent intent) {
		Handler currentHandler = new Handler();
		currentHandler.postDelayed(new Runnable() {					
			public void run() {
				TestPaymentScreenActivity.this.sendBroadcast(intent);						
			}
		}, 3000);		
	}
	
	private AlertDialog createOrderSelectionDialog(android.content.DialogInterface.OnClickListener listener) {
		PurchaseStateOrder[] orders = PurchaseStateOrderRepository.getInstance().getAllOrders();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("select an order");
		
		String[] orderIds = new String[orders.length];
		for (int i = 0; i < orders.length; i++) {
			orderIds[i] = orders[i].getOrderId() + " - " + orders[i].getProductId();
		}
		builder.setSingleChoiceItems(orderIds, -1, listener);		
		return builder.create();
	}	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams layoutParams = new LinearLayout.LayoutParams(
        	LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        
        addTestButton(layout, "buy item", new OnClickListener() {
			public void onClick(View v) {
				PurchaseStateOrder order = createOrder();
				order.setPurchaseState(PurchaseStateOrder.STATE_PURCHASED);
				PurchaseStateOrderRepository.getInstance().add(order);
				notifyAboutOrder(order);
				TestPaymentScreenActivity.this.finish();
			}
		});        
        addTestButton(layout, "cancel the order", new OnClickListener() {
			public void onClick(View v) {
				PurchaseStateOrder order = createOrder();
				order.setPurchaseState(PurchaseStateOrder.STATE_CANCELED);
				PurchaseStateOrderRepository.getInstance().add(order);
				notifyAboutOrder(order);
				TestPaymentScreenActivity.this.finish();
			}
		});        
        addTestButton(layout, "refund the order", new OnClickListener() {
			public void onClick(View v) {
				AlertDialog dialog = createOrderSelectionDialog(new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {						
						PurchaseStateOrder[] orders = PurchaseStateOrderRepository.getInstance().getAllOrders();
						PurchaseStateOrder order = orders[which];
						if (order != null) {
							order.setPurchaseState(PurchaseStateOrder.STATE_REFUNDED);				
							notifyAboutOrder(order);
							TestPaymentScreenActivity.this.finish();
						}
					}
				});
				dialog.show();
			}
		});        
        addTestButton(layout, "service unavailable", new OnClickListener() {
			public void onClick(View v) {
				sendBroadcastDelayed(responseIntentFactory.createResponseCodeIntent(getRequestId(), ResponseCode.RESULT_SERVICE_UNAVAILABLE));
				TestPaymentScreenActivity.this.finish();
			}
		});        
        addTestButton(layout, "abandon checkout", new OnClickListener() {
			public void onClick(View v) {
				sendBroadcastDelayed(responseIntentFactory.createResponseCodeIntent(getRequestId(), ResponseCode.RESULT_USER_CANCELED));
				TestPaymentScreenActivity.this.finish();
			}
		});        
        
        this.setContentView(layout, layoutParams);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    }
    
   
}
