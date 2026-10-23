package com.sdmmllc.superdupermm.comms;

public interface CommStatusListener {
	
	public void genericError(String title, String message);
	public void noNetwork(String title, String message);
	public void commError(String title, String message);
	public void networkError(String title, String message);
	public void serverError(String title, String message);
	public void parseError(String title, String message);
	public void hasResult(String title, String message);
	public void resultOK(String title, String message);

}
