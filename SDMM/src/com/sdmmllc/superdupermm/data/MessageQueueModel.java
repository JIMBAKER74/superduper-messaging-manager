package com.sdmmllc.superdupermm.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

public class MessageQueueModel {
	
	public static final String TAG = "MessageQueueModel";

	private List<Intent> messages = new ArrayList<Intent>();
	private Context mContext;
	public static final String 
		MESSAGE_QUEUE = "message_queue";
	
	public MessageQueueModel(Context context) {
		mContext = context;
		getMessages();
	}
	
	public boolean addMessage(Intent newMessage) {
		Log.i(TAG, "new message in queue");
		messages.add(newMessage);
		saveMessages();
		return false;
	}
	
	public void removeMessage(Intent message) {
		messages.remove(message);
		saveMessages();
	}
	
	public int size() {
		return messages.size();
	}
	
	private void saveMessages() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Editor prefsEditor = appSharedPrefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(messages);
		prefsEditor.putString(MESSAGE_QUEUE, json);
		prefsEditor.commit();
	}
	
	@SuppressWarnings("unchecked")
	public List<Intent> getMessages() {
		SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		Gson gson = new Gson();
		String json = appSharedPrefs.getString(MESSAGE_QUEUE, "");
		messages = (List<Intent>)gson.fromJson(json, List.class);
		if (messages == null) messages = new ArrayList<Intent>();
    	
		return messages;
	}
}
