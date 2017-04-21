package com.newind.ftpclient;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by newind on 17-4-20.
 */

public class DBManager {
    private static DBManager _manager_ = null;
    public static DBManager instance(Context context) {
        if(_manager_ == null) {
            _manager_ = new DBManager(context);
        }
        return _manager_;
    }

    private SoftReference<FileUploadDataBase> mConnection;

    private DBManager(Context context){
        mConnection = new SoftReference<>(new FileUploadDataBase(context));
        if (mConnection.get() == null) {
            mConnection = new SoftReference<>(new FileUploadDataBase(context));
        }
    }

    public FileUploadDataBase getFileUploadDB() {
        return mConnection.get();
    }


    public static final String[] STAT_VALUES  = {
            "等待","上传","完成","出错"
    };

    public static final int[] STAT_COLORS = {
            0xFF000000,0xFF00FF00,0xFF00FF00,0xFFFF0000
    };

    public static class FileUploadInfo {
        public static final int STAT_NEW = 0;
        public static final int STAT_UPLOADING = 1;
        public static final int STAT_FINISHED = 2;
        public static final int STAT_ERROR = 3;

        public int id;
        public String file_path;
        public float progress; // 0~1
        public int status;
    }

    public static class FileUploadDataBase extends SQLiteOpenHelper {
        private static final int DB_VERSION = 0x00010001;
        private static final int DB_VERSION_MASK_HIGH = 0xFFFF0000;
        private static final int DB_VERSION_MASK_LOW = 0x0000FFFF;
        private static final String DB_NAME = FileUploadDataBase.class.getSimpleName();
        private static final String TABLE_NAME = "update_list";

        private FileUploadDataBase(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(String.format("Create Table %s (id integer primary key autoincrement," +
                    "file_path text," +
                    "progress float," +
                    "status integer);", TABLE_NAME));
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if ((oldVersion&DB_VERSION_MASK_HIGH) != (DB_VERSION_MASK_HIGH&newVersion)) {

            }else if ((oldVersion&DB_VERSION_MASK_LOW) != (DB_VERSION_MASK_LOW&newVersion)) {

            }
        }

        public List<FileUploadInfo> fetchList(int fromID) {
            List<FileUploadInfo> list = new ArrayList<>();
            SQLiteDatabase sqLiteDatabase = getReadableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery("select * from " + TABLE_NAME + " where id > ?" , new String[] {String.valueOf(fromID)});
            while (cursor.moveToNext()) {
                FileUploadInfo info = new FileUploadInfo();
                info.id = cursor.getInt(0);
                info.file_path = cursor.getString(1);
                info.progress = cursor.getFloat(2);
                info.status = cursor.getInt(3);
                list.add(info);
            }
            return list;
        }

        public void addItem(String filePath) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.execSQL("insert into " + TABLE_NAME + " (file_path,progress,status) values (?, ? ,?)",new Object[] {filePath,"0",String.valueOf(FileUploadInfo.STAT_NEW) });
        }

        public void updateItem(int id,float process,int state) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.execSQL("update " + TABLE_NAME + " set progress=? ,status=? where id=?",
                    new Object[] {String.valueOf(process),String.valueOf(state),String.valueOf(id)});
        }

        public void deleteItem(int id) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.delete(TABLE_NAME,"id=" + id,null);
        }

        public void clearAll() {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.delete(TABLE_NAME,"id>=0",null);
        }
    }


}
