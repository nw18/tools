package com.newind.mutualharm;

import android.app.Application;

import com.newind.core.Util;

/**
 * Created by newind on 17-4-1.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Util.theApplication = this;
        System.loadLibrary("native-lib");
    }
}
