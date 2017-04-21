package com.newind.ftpclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements TaskManager.IListener {
    ListView listView;
    TaskManager taskManager;
    List<DBManager.FileUploadInfo> fileUploadInfoList = new ArrayList<>();
    private String mExternalPath;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if(imageUri != null) {
               addFileFromUri(imageUri);
            }
        }else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (imageUris != null) {
                for (Uri imageUri : imageUris) {
                    addFileFromUri(imageUri);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list_view);
        taskManager = TaskManager.getInstance();
        taskManager.getAll(fileUploadInfoList);
        listView.setAdapter(mAdapter);
        mExternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        taskManager.addListener(this);
        onNewIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        taskManager.rmvListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                break;
            case R.id.menu_clear_all:
                taskManager.clearAll();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addFileFromUri(Uri uri) {
        String filePath = MainApplication.getInstance().getFilePathFromContentUri(uri);
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        Log.e("XXX","addFileFromUri " + filePath);
        taskManager.addItem(filePath);
    }

    private static class UploadItemHolder {
        int position;
        TextView tv_file_path,tv_status;
        ProgressBar pb_process;
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return fileUploadInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return fileUploadInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return fileUploadInfoList.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            UploadItemHolder holder;
            if (null == view) {
                view = getLayoutInflater().inflate(R.layout.list_item_file_upload,null);
                holder = new UploadItemHolder();
                view.setTag(holder);
                holder.tv_file_path = (TextView) view.findViewById(R.id.tv_file_path);
                holder.tv_status = (TextView) view.findViewById(R.id.tv_status);
                holder.pb_process = (ProgressBar) view.findViewById(R.id.pb_upload_percent);
            }else {
                holder = (UploadItemHolder) view.getTag();
            }
            holder.position = position;
            DBManager.FileUploadInfo info = fileUploadInfoList.get(position);
            if (info.file_path.startsWith(mExternalPath)) {
                holder.tv_file_path.setText(info.file_path.substring(mExternalPath.length()));
            }else {
                holder.tv_file_path.setText(info.file_path);
            }
            holder.pb_process.setProgress((int) (info.progress * holder.pb_process.getMax()));
            holder.tv_status.setText(DBManager.STAT_VALUES[info.status]);
            if (info.status == DBManager.FileUploadInfo.STAT_UPLOADING) {
                holder.tv_status.setTextColor(DBManager.STAT_COLORS[DBManager.FileUploadInfo.STAT_FINISHED] | ((int)(255 * (1 - info.progress)) << 16));
            }else {
                holder.tv_status.setTextColor(DBManager.STAT_COLORS[info.status]);
            }
            return view;
        }
    };

    @Override
    public void onAdd(final DBManager.FileUploadInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                taskManager.getAll(fileUploadInfoList);
                mAdapter.notifyDataSetChanged();
                Intent it = new Intent(MainActivity.this,FtpUploadService.class);
                it.setAction(FtpUploadService.ACTION_ADD);
                it.putExtra(FtpUploadService.EXTRA_ID,String.valueOf(info.id));
                startService(it);
            }
        });
    }

    @Override
    public void onDelete(final DBManager.FileUploadInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                taskManager.getAll(fileUploadInfoList);
                mAdapter.notifyDataSetChanged();
                Intent it = new Intent(MainActivity.this,FtpUploadService.class);
                it.setAction(FtpUploadService.ACTION_DELETE);
                it.putExtra(FtpUploadService.EXTRA_ID,String.valueOf(info.id));
                startService(it);
            }
        });
    }

    @Override
    public void onUpdate(DBManager.FileUploadInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClear() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                taskManager.getAll(fileUploadInfoList);
                mAdapter.notifyDataSetChanged();
                Intent it = new Intent(MainActivity.this,FtpUploadService.class);
                it.setAction(FtpUploadService.ACTION_CLEAR);
                startService(it);
            }
        });
    }
}
