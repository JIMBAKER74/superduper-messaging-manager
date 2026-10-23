package com.android.vending.billing.test;

public enum ResponseCode {
	RESULT_OK(0),	
	RESULT_USER_CANCELED(1),	
	RESULT_SERVICE_UNAVAILABLE(2),	
	RESULT_BILLING_UNAVAILABLE(3),	
	RESULT_ITEM_UNAVAILABLE(4),	
	RESULT_DEVELOPER_ERROR(5),	
	RESULT_ERROR(6);	
	
	private final int value;  

	ResponseCode(int value) {
        this.value = value;
    }

    public int getValue() { 
        return value; 
    }
    public static ResponseCode createByValue(int value) {
    	for (ResponseCode code : values())
            if (code.getValue() == value)
                return code;
        throw new Error("unknown ResponseCode value");
    }
}
