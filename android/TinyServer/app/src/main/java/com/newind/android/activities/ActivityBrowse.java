package com.newind.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.newind.android.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class ActivityBrowse extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String KEY_ROOT_PATH = "root_path";
    public static final String KEY_SELECT = "select";
    private ListView lv_dir;
    private File rootDirectory;
    private File currDirectory;
    private DirAdapter dirAdapter;
    public static void start(Activity activity,String rootPath,int reqCode){
        Intent it = new Intent(activity,ActivityBrowse.class);
        it.putExtra(KEY_ROOT_PATH,rootPath);
        activity.startActivityForResult(it,reqCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String rootPath = getIntent().getStringExtra(KEY_ROOT_PATH);
        currDirectory = rootDirectory = new File(rootPath);
        if (!rootDirectory.exists() || !rootDirectory.canRead()){
            finish();
            return;
        }
        setContentView(R.layout.activity_browse);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        lv_dir = (ListView) findViewById(R.id.lv_dir);
        findViewById(R.id.bt_dir_select).setOnClickListener(this);
        dirAdapter = new DirAdapter();
        lv_dir.setAdapter(dirAdapter);
        dirAdapter.update(currDirectory);
        lv_dir.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bt_dir_select:
                Intent it = new Intent();
                it.putExtra(KEY_SELECT,currDirectory.getAbsolutePath());
                setResult(RESULT_OK,it);
                finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        dirAdapter.update((File) dirAdapter.getItem(i));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private class DirAdapter extends BaseAdapter{
        private List<File> fileList = new ArrayList<>();
        public void update(final File dir){
            currDirectory = dir;
            new AsyncTask<Integer,Integer,File[]>(){
                @Override
                protected File[] doInBackground(Integer... integers) {
                    return dir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {
                            return file.isDirectory() && !s.startsWith(".") && file.canRead();
                        }
                    });
                }

                @Override
                protected void onPostExecute(File[] fileArray) {
                    fileList.clear();
                    if (!TextUtils.equals(dir.getAbsolutePath(),rootDirectory.getAbsolutePath())) {
                        fileList.add(dir.getParentFile());
                    }
                    if (fileArray != null){
                        for (File dir:fileArray){
                            fileList.add(dir);
                        }
                    }
                    dirAdapter.notifyDataSetChanged();
                }
            }.execute();
        }

        @Override
        public int getCount() {
            return fileList.size();
        }

        @Override
        public Object getItem(int i) {
            return fileList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView = (TextView) view;
            if (textView == null){
                textView = new TextView(ActivityBrowse.this);
                textView.setTextColor(ContextCompat.getColor(getBaseContext(),R.color.colorTextBlack));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setPadding(5,3,5,3);
            }
            if (i == 0 && !fileList.get(0).getAbsolutePath().startsWith(currDirectory.getAbsolutePath())){
                textView.setText("..");
            }else {
                textView.setText(fileList.get(i).getName());
            }
            return textView;
        }
    }
}
