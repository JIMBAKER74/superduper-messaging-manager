package com.android.vending.billing.test;

import java.util.HashMap;
import java.util.Map;

public class OrderNotificationRepository {
	private static OrderNotificationRepository instance;
	
	public static OrderNotificationRepository getInstance() {
		if (instance == null)
			instance = new OrderNotificationRepository();
		return instance;
	}
	
	private int notificationIdCounter = 0;
	private Map<String, PurchaseStateOrder[]> notifications = new HashMap<String, PurchaseStateOrder[]>();
	
	private OrderNotificationRepository() {		
	}
	
	public String add(PurchaseStateOrder order) {
		return add(new PurchaseStateOrder[] { order });
	}
	
	public String add(PurchaseStateOrder[] orders) {
		String notificationId = ++notificationIdCounter + "";
		notifications.put(notificationId, orders);	
		return notificationId;
	}
	
	public PurchaseStateOrder[] get(String notificationId) {
		PurchaseStateOrder[] orders = notifications.get(notificationId);
		if (orders == null)
			return new PurchaseStateOrder[0];
		PurchaseStateOrder[] ordersCopy = new PurchaseStateOrder[orders.length];
		for (int i = 0; i < orders.length; i++) {
			ordersCopy[i] = (PurchaseStateOrder)orders[i].clone();
			ordersCopy[i].setNotificationId(notificationId);
		}
		return ordersCopy;
	}
	
	public void remove(String notificationId) {
		notifications.remove(notificationId);
	}

}
