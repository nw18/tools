package com.newind.ftpclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;


public class FtpUploadService extends IntentService {
    private static final String ACTION_SETUP = "com.newind.ftpclient.ACTION_SETUP";
    private static final String ACTION_DELETE = "com.newind.ftpclient.ACTION_DELETE";
    private static final String ACTION_ADD_FILE = "com.newind.ftpclient.ACTION_ADD_FILE";

    private static final String EXTRA_ID = "com.newind.ftpclient.extra.EXTRA_ID";
    private static final String EXTRA_FILE_PATH = "com.newind.ftpclient.extra.EXTRA_FILE_PATH";

    public FtpUploadService() {
        super("FtpUploadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SETUP.equals(action)) {

            } else if (ACTION_ADD_FILE.equals(action)) {
                String fileID = intent.getStringExtra(EXTRA_ID);
                String filePath = intent.getStringExtra(EXTRA_FILE_PATH);

            }
        }
    }


}
