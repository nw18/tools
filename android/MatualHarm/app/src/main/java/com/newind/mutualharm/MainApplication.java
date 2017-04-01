package com.newind.mutualharm;

import android.app.Application;

/**
 * Created by newind on 17-4-1.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        System.loadLibrary("native-lib");
    }
}
