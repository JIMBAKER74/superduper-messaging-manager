package com.messagingappsllc.superdupermms;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

import com.messagingappsllc.superdupermms.mms.R;
import com.sdmmllc.superdupersmsmanager.sdk.SDSmsManager;

public class CopyOfAppConsts {

	// Templates Provider
    public static final String TemplatesProvider_TYPE = 
    		SDSmsManager.getContext().getResources().getString(R.string.templateProviderType);
    public static final int TEMPLATES = 1;
    public static final int TEMPLATE_ID = 2;

    public static class Template implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.parse(
        		SDSmsManager.getContext().getResources().getString(R.string.templateUri));
        public static final String TEXT = "text";
    }

    public static UriMatcher sMatcher_TemplatesProvider;
    static {
    	sMatcher_TemplatesProvider = new UriMatcher(UriMatcher.NO_MATCH);
    	sMatcher_TemplatesProvider.addURI(
    			SDSmsManager.getContext().getResources().getString(R.string.templateAuthority),
    			"templates", TEMPLATES);
    	sMatcher_TemplatesProvider.addURI(
    			SDSmsManager.getContext().getResources().getString(R.string.templateAuthority),
    			"templates/#", TEMPLATE_ID);
    }

    // SuggestionsProvider
    final static String SuggestionsProvider_AUTHORITY = 
    		SDSmsManager.getContext().getResources().getString(R.string.suggestionsAuthority);

    // TempFileProvider
    public static final Uri TempFileProvider_SCRAP_CONTENT_URI = Uri.parse(
    		SDSmsManager.getContext().getResources().getString(R.string.scrapContentUri));

    public static final int MMS_SCRAP_SPACE = 1;
    public static final UriMatcher sURLMatcher_TempFileProvider = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURLMatcher_TempFileProvider.addURI(
        		SDSmsManager.getContext().getResources().getString(R.string.scrapContentAuthority),
        		"scrapSpace", MMS_SCRAP_SPACE);
    }

	public static final String THEME_PACKAGE_PREFIX = 
			SDSmsManager.getContext().getResources().getString(R.string.themePackagePrefix);
	
	public static final boolean
		DEBUG_SETUP = false;

}
