package com.sdmmllc.superdupersmsmanager.sdk.utils;

public class Consts {

	public static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";
	
	public static final String SUB_ID = "sub_id";
	
	public static final String PROPERTY_MMS_TRANSACTION = "mms.transaction";
	
	public static final String FEATURE_ENABLE_MMS = "enableMMS";
	
	public static final String APN_TYPE_MMS = "mms";
	
	public static final String APN_TYPE_ALL = "*";
	
	public static final String PROPERTY_OPERATOR_ISROAMING = "gsm.operator.isroaming";
	
	public static final String REASON_VOICE_CALL_ENDED = "2GVoiceCallEnded";
	
	public static final String KIND_MESSAGE = "android.message";
	
	public static final String DISPLAY_ORDER = "android.contacts.DISPLAY_ORDER";
	public static final int DISPLAY_ORDER_PRIMARY = 1;
	public static final int DISPLAY_ORDER_ALTERNATIVE = 2;
	
	public static final String ADDRESS_BOOK_INDEX_EXTRAS = "address_book_index_extras";
	public static final String EXTRA_ADDRESS_BOOK_INDEX_TITLES = "address_book_index_titles";
	public static final String EXTRA_ADDRESS_BOOK_INDEX_COUNTS = "address_book_index_counts";
	
	public static final int ENCODING_7BIT = 1;
	public static final int MAX_USER_DATA_SEPTETS = 160;
	public static final int MAX_USER_DATA_SEPTETS_WITH_HEADER = 153;
	
	public static final int APN_ALREADY_ACTIVE     = 0;
	public static final int APN_REQUEST_STARTED    = 1;
	
    /** Message type: all messages. */
    public static final int MESSAGE_TYPE_ALL    = 0;
    /** Message type: inbox. */
    public static final int MESSAGE_TYPE_INBOX  = 1;
    /** Message type: sent messages. */
    public static final int MESSAGE_TYPE_SENT   = 2;
    /** Message type: drafts. */
    public static final int MESSAGE_TYPE_DRAFT  = 3;
    /** Message type: outbox. */
    public static final int MESSAGE_TYPE_OUTBOX = 4;
    /** Message type: failed outgoing message. */
    public static final int MESSAGE_TYPE_FAILED = 5;
    /** Message type: queued to send later. */
    public static final int MESSAGE_TYPE_QUEUED = 6;
    /**
     * The type of message.
     * <P>Type: INTEGER</P>
     */
    public static final String TYPE = "type";
    /**
     * Has the message been read?
     * <P>Type: INTEGER (boolean)</P>
     */
    public static final String READ = "read";
    /**
     * Error code associated with sending or receiving this message.
     * <P>Type: INTEGER</P>
     */
    public static final String ERROR_CODE = "error_code";

}
