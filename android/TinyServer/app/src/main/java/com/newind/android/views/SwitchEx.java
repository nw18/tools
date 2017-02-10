package com.newind.android.views;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by newind on 17-2-8.
 */

public class SwitchEx extends Switch implements CompoundButton.OnCheckedChangeListener {
    public SwitchEx(Context context) {
        super(context);
        init();
    }

    public SwitchEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwitchEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        setOnCheckedChangeListener(this);
        onCheckedChanged(this,isChecked());
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b){
            setText(getTextOn());
        }else {
            setText(getTextOff());
        }
    }
}
