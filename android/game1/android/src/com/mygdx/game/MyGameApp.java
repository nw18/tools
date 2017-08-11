package com.mygdx.game;

import android.app.Application;

/**
 * Created by newind on 17-8-11.
 */

public class MyGameApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidBus.Init();
    }
}
