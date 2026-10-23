package com.android.vending.billing.test;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class PurchaseState {

	private long nonce;
	private List<PurchaseStateOrder> orders;
	
	public PurchaseState() {
		orders = new ArrayList<PurchaseStateOrder>();
	}
	
	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}

	public Iterable<PurchaseStateOrder> getOrders() {
		return orders;
	}

	public void addOrder(PurchaseStateOrder order) {
		this.orders.add(order);
	}

	public static PurchaseState fromJson(String json) throws JSONException {
		PurchaseState purchaseState = new PurchaseState();
		JSONTokener jsonTokener = new JSONTokener(json);
		JSONObject jsonPurchaseState = (JSONObject)jsonTokener.nextValue();
		purchaseState.setNonce(jsonPurchaseState.getLong("nonce"));

		JSONArray jsonOrders = jsonPurchaseState.getJSONArray("orders");
		for (int i = 0; i < jsonOrders.length(); i++) {
			JSONObject jsonOrder = (JSONObject)jsonOrders.get(i);
			PurchaseStateOrder order = PurchaseStateOrder.fromJsonObject(jsonOrder);
			purchaseState.addOrder(order);
		}
		return purchaseState;
	}
	
	public String toJson() {
		JSONObject jsonPurchaseState = new JSONObject();
		try {
			jsonPurchaseState.put("nonce", getNonce());
			JSONArray jsonOrders = new JSONArray();
			for (PurchaseStateOrder order : getOrders())				
				jsonOrders.put(order.toJsonObject());
			jsonPurchaseState.put("orders", jsonOrders);
			return jsonPurchaseState.toString(4);
		} catch (JSONException ex) {
			return "null";
		}
	}
}
