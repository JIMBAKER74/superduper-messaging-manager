package com.sdmmllc.superdupermm.comms;

public abstract class CommStatusAdapter implements Runnable, CommStatusListener {
	
	private String TAG = "CommStatusAdapter";
	public static int 
		GENERIC_ERROR = 1 << 1,
		NO_CONNECTION = 1 << 2,
		COMM_ERROR = 1 << 3,
		NETWORK_ERROR = 1 << 4,
		SERVER_ERROR = 1 << 5, 
		PARSE_ERROR = 1 << 6,
		HAS_RESULT = 1 << 7,
		RESULT_OK = 1 << 8;
	
	private int mResultType = 0;
	private String mMessage = "", mTitle = "";
	
	@Override
	public void run() {
		if ((mResultType & RESULT_OK) == RESULT_OK) resultOK(mTitle, mMessage);
		else {
			if ((mResultType & HAS_RESULT) == HAS_RESULT) hasResult(mTitle, mMessage);
			else if ((mResultType & PARSE_ERROR) == PARSE_ERROR) parseError(mTitle, mMessage);
			else if ((mResultType & SERVER_ERROR) == SERVER_ERROR) serverError(mTitle, mMessage);
			else if ((mResultType & NETWORK_ERROR) == NETWORK_ERROR) networkError(mTitle, mMessage);
			else if ((mResultType & COMM_ERROR) == COMM_ERROR) commError(mTitle, mMessage);
			else if ((mResultType & NO_CONNECTION) == NO_CONNECTION) noNetwork(mTitle, mMessage);
			genericError(mTitle, mMessage);
		}
	}
	
	public void result(int resultType, String title, String message) {
		mMessage = message;
		mTitle = title;
		mResultType = resultType;
	}
}
