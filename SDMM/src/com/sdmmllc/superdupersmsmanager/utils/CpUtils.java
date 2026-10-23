package com.sdmmllc.superdupersmsmanager.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.ContentProvider;
import android.util.Log;

public class CpUtils {
	public static String TAG = "CpUtils";

	public static void setAppOps(ContentProvider cp, int ops1, int ops2) {
        try {
			Method m = cp.getClass().getDeclaredMethod("setAppOps", int.class, int.class);
			m.setAccessible(true);
			try {
				m.invoke(cp, ops1, ops2);
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method ContentProvider.setAppOps");
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method ContentProvider.setAppOps");
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Log.e(TAG, "error with return value of reflected method ContentProvider.setAppOps");
			}
		} catch (NoSuchMethodException e1) {

			e1.printStackTrace();
			Log.e(TAG, ".setAppOps could not invoke method ContentProvider.setAppOps");
		}
	}
	
}
