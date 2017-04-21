package com.newind.ftpclient;

import android.app.Application;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

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

    public String getFilePathFromContentUri(Uri selectedVideoUri) {
        String filePath = selectedVideoUri.toString();
        String filePrefix = "file://";
        if (filePath.startsWith(filePrefix)) {
            filePath = filePath.substring(filePrefix.length());
        }else {
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            Cursor cursor = getContentResolver().query(selectedVideoUri, filePathColumn, null, null, null);
            if (null != cursor) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        return filePath;
    }
}
