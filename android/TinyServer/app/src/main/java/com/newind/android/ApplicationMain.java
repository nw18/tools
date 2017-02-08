package com.newind.android;

import android.app.Application;
import android.os.Environment;

import com.newind.base.LogManager;

/**
 * Created by Administrator on 2017/1/30.
 */

public class ApplicationMain extends Application {
    private static com.newind.Application server;
    @Override
    public void onCreate() {
        LogManager.disableLogFile();
        server = new com.newind.Application();
        try {
            server.startServer(new String[]{
                    "root" , Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "json" , "true"
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static com.newind.Application getServer(){
        return  server;
    }
}
