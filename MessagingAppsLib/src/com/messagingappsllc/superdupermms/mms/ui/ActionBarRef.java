package com.messagingappsllc.superdupermms.mms.ui;

import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;

import com.messagingappsllc.superdupermms.mms.R;

public class ActionBarRef {
	
	private static boolean needMenuButton(Context act) {
	    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
	    } else {
	        return ViewConfiguration.get(act).hasPermanentMenuKey();
	    }
	}


	public static void mainActActionBar(WeakReference<Activity> act) {
		ActionBar actionBar = act.get().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setLogo(R.drawable.icon);
		actionBar.setBackgroundDrawable(
				act.get().getResources().getDrawable(R.drawable.icon));

		if (needMenuButton(act.get().getApplicationContext())) {
			LayoutInflater inflator = (LayoutInflater) act.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(R.layout.blank_actionbar, null);
			actionBar.setCustomView(v);
		} else {
			LayoutInflater inflator = (LayoutInflater) act.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(R.layout.blank_actionbar, null);
			actionBar.setCustomView(v);
		}
	}
	
	public static void mainListActActionBar(WeakReference<ListActivity> act) {
		ActionBar actionBar = act.get().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setLogo(R.drawable.icon);
		actionBar.setBackgroundDrawable(
				act.get().getResources().getDrawable(R.drawable.icon));

		if (needMenuButton(act.get().getApplicationContext())) {
			LayoutInflater inflator = (LayoutInflater) act.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(R.layout.blank_actionbar, null);
			actionBar.setCustomView(v);
		} else {
			LayoutInflater inflator = (LayoutInflater) act.get().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(R.layout.blank_actionbar, null);
			actionBar.setCustomView(v);
		}
	}
	
}
						
