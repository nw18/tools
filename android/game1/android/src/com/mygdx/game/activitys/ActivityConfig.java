package com.mygdx.game.activitys;

import android.app.Activity;
import android.os.Bundle;

import com.mygdx.game.R;

public class ActivityConfig extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
    }
}
