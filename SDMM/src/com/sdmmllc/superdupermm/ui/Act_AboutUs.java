package com.sdmmllc.superdupermm.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.sdmmllc.superdupermessagingmanager.R;
import com.sdmmllc.superdupersmsmanager.utils.ErrorReporting;

public class Act_AboutUs extends Activity {
    /** Called when the activity is first created. */
	private Context spaContext;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spaContext = this;
        setContentView(R.layout.act_aboutus);
        TextView info = (TextView)findViewById(R.id.aboutUs3Lbl);
        Linkify.addLinks(info, Linkify.WEB_URLS);
        TextView info2 = (TextView)findViewById(R.id.aboutUs4Lbl);
        Linkify.addLinks(info2, Linkify.EMAIL_ADDRESSES);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(spaContext, ErrorReporting.FLURRY_APP_KEY);
        FlurryAgent.onPageView();
    }

    @Override
    protected void onStop(){
        FlurryAgent.onEndSession(spaContext);
        super.onStop();
    }
}
