package com.android.vending.billing.test;

import org.json.JSONException;
import org.json.JSONObject;

public class PurchaseStateOrder implements Cloneable {
	public static final int STATE_PURCHASED = 0;
	public static final int STATE_CANCELED = 1;
	public static final int STATE_REFUNDED = 2;

	private String notificationId;
	private String orderId;
	private String packageName;
	private String productId;
	private long purchaseTime;
	private int purchaseState;
	private String developerPayload;
	
	public PurchaseStateOrder() {
		purchaseTime = System.currentTimeMillis();
	}
	
	public String getNotificationId() {
		return notificationId;
	}
	public void setNotificationId(String notificationId) {
		this.notificationId = notificationId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public long getPurchaseTime() {
		return purchaseTime;
	}
	public void setPurchaseTime(long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}
	public int getPurchaseState() {
		return purchaseState;
	}
	public void setPurchaseState(int purchaseState) {
		this.purchaseState = purchaseState;
	}
	public String getDeveloperPayload() {
		return developerPayload;
	}
	public void setDeveloperPayload(String developerPayload) {
		this.developerPayload = developerPayload;
	}
	
	public static PurchaseStateOrder fromJsonObject(JSONObject jsonOrder) throws JSONException {
		PurchaseStateOrder order = new PurchaseStateOrder();
		order.setDeveloperPayload(jsonOrder.getString("developerPayload"));
		order.setNotificationId(jsonOrder.getString("notificationId"));
		order.setOrderId(jsonOrder.getString("orderId"));
		order.setPackageName(jsonOrder.getString("packageName"));
		order.setProductId(jsonOrder.getString("productId"));
		order.setPurchaseState(jsonOrder.getInt("purchaseState"));
		order.setPurchaseTime(jsonOrder.getLong("purchaseTime"));
		return order;
	}
	
	public JSONObject toJsonObject() {
		JSONObject jsonOrder = new JSONObject();
		try {		
			jsonOrder.put("developerPayload", developerPayload);
			jsonOrder.put("notificationId", notificationId);
			jsonOrder.put("orderId", orderId);
			jsonOrder.put("packageName", packageName);
			jsonOrder.put("productId", productId);
			jsonOrder.put("purchaseState", purchaseState);
			jsonOrder.put("purchaseTime", purchaseTime);
		} catch (JSONException ex) {}
		return jsonOrder;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return new PurchaseStateOrder();
		}
	}
}
