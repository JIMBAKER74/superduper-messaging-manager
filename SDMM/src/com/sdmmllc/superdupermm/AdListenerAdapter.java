package com.sdmmllc.superdupermm;

import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdView.MMAdListener;

public abstract class AdListenerAdapter implements MMAdListener {
	
	public void MMAdCachingCompleted(MMAdView arg0, boolean arg1) {
	}

	public void MMAdClickedToNewBrowser(MMAdView arg0) {
	}

	public void MMAdClickedToOverlay(MMAdView arg0) {
	}

	public void MMAdFailed(MMAdView arg0) {
	}

	public void MMAdOverlayLaunched(MMAdView arg0) {
	}

	public void MMAdRequestIsCaching(MMAdView arg0) {
	}

	public void MMAdReturned(MMAdView arg0) {
	}
}
