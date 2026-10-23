package com.messagingappsllc.superdupermms.mms.ui;

import android.content.Context;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class ActSetupCheckBoxPreference extends CheckBoxPreference {
 
    public ActSetupCheckBoxPreference(Context context) {
        super(context);
    }
 
    public ActSetupCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public ActSetupCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    /**
     * We catch the view after its creation, and before the activity will use it, in order to make our changes
     * @param parent
     * @return
     */
    @Override
    protected View onCreateView(ViewGroup parent) {
        // And it's just a TextView!
        CheckBox box =  (CheckBox)super.onCreateView(parent);
        box.setTextColor(Color.WHITE);
        
        return box;
    }
}
