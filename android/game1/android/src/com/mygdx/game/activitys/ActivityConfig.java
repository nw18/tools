package com.mygdx.game.activitys;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;

import com.mygdx.game.R;

public class ActivityConfig extends Wrapper {
    EditText et_name;
    Switch sw_sex;
    public ActivityConfig() {
        super(R.layout.activity_config);
    }

    @Override
    protected void onCreateFinish(Bundle savedInstanceState) {
        et_name = find(R.id.et_name);
        sw_sex = find(R.id.sw_sex);
    }
}
