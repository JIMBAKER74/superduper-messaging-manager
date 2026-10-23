package com.android.vending.billing.test;

import java.util.HashMap;
import java.util.Map;

public class PurchaseStateOrderRepository {
	private static PurchaseStateOrderRepository instance;
	
	public static PurchaseStateOrderRepository getInstance() {
		if (instance == null)
			instance = new PurchaseStateOrderRepository();
		return instance;
	}
	
	private Map<String, PurchaseStateOrder> orders = new HashMap<String, PurchaseStateOrder>();
	
	private PurchaseStateOrderRepository() {		
	}
	
	public void add(PurchaseStateOrder order) {
		if (order.getOrderId() == null) {
			String newOrderId = (orders.size() + 1) + "";
			order.setOrderId(newOrderId);
		}
		orders.put(order.getOrderId(), order);		
	}
	
	public PurchaseStateOrder get(String orderId) {
		return orders.get(orderId);
	}

	public PurchaseStateOrder[] getAllOrders() {
		return orders.values().toArray(new PurchaseStateOrder[orders.size()]);		
	}
	
	public PurchaseStateOrder[] getUserManagedPurchases() {
		return getAllOrders();
	}
}
