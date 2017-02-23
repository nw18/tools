package com.newind.android.activities;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Administrator on 2017/2/23.
 */

public class ActivityNull extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
