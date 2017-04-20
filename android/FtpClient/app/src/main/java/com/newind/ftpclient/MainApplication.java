package com.newind.ftpclient;

import android.app.Application;

/**
 * Created by newind on 17-4-20.
 */

public class MainApplication extends Application {
    private static MainApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static MainApplication getInstance() {
        return instance;
    }
}
