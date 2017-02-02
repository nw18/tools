package com.newind.tinyserver;

import android.app.Application;
import android.os.Environment;

import com.newind.ApplicationConfig;
import com.newind.base.LogManager;

import java.io.File;

/**
 * Created by Administrator on 2017/1/30.
 */

public class MainApplication extends Application {
    private static com.newind.Application server;
    @Override
    public void onCreate() {
        server = new com.newind.Application();
        LogManager.LOG_FILE_PATH  = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LogManager.LOG_FILE_PATH;
        ApplicationConfig.instance().setRoot(Environment.getExternalStorageDirectory().getAbsolutePath());
        server = new com.newind.Application();
        // TODO: 2017/1/30 test code.
        try {
            server.startServer(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static com.newind.Application getServer(){
        return  server;
    }
}
