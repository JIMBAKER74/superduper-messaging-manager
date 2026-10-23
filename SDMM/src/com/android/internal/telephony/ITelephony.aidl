package com.android.internal.telephony;

interface ITelephony {
	boolean endCall();
	void answerRingingCall();
	void silenceRinger();
	void cancelMissedCallsNotification();
	void toggleRadioOnOff();
	boolean isRadioOn();
	boolean setRadio(boolean turnOn);
}

