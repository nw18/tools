package com.newind.android;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.newind.base.LogManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class ActivityLogCat extends AppCompatActivity {
    private static final int MAX_LENGTH = 512;
    private ListView lv_log_cat;
    private LogAdapter logAdapter;
    private LinkedList<String> listData = new LinkedList<>();
    private static LinkedList<String> listCache = new LinkedList<>();
    private static boolean hasNewData;
    private Timer refreshTimer;
    public static void init(){
        LogManager.getLogger().addHandler(new Handler() {
            Date date = new Date(0);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);
            @Override
            public void publish(LogRecord record) {
                synchronized (listCache){
                    date.setTime(record.getMillis());
                    listCache.add(String.format("%06d %s %s",record.getThreadID(),dateFormat.format(date),record.getMessage()));
                    while(listCache.size() > MAX_LENGTH){
                        listCache.removeFirst();
                    }
                    hasNewData = true;
                }
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_cat);
        lv_log_cat = ((ListView) findViewById(R.id.lv_log_cat));
        logAdapter = new LogAdapter();
        lv_log_cat.setAdapter(logAdapter);
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean changed = false;
                synchronized (listData){
                    synchronized (listCache){
                        if (hasNewData){
                            listData.clear();
                            listData.addAll(listCache);
                            hasNewData = false;
                            changed = true;
                        }
                    }
                }
                if (changed){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logAdapter.notifyDataSetChanged();
                            lv_log_cat.setSelection(logAdapter.getCount() - 1);
                        }
                    });
                }
            }
        },100,40);
    }

    @Override
    protected void onDestroy() {
        refreshTimer.cancel();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log_cat,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_stop){
            new StopTask().execute();
        }
        return true;
    }

    private class LogAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            synchronized (listData) {
                return listData.size();
            }
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            TextView textView = (TextView) view;
            if (textView == null){
                textView = new TextView(ActivityLogCat.this);
                textView.setTextColor(getResources().getColor(R.color.colorTextBlack));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setPadding(5,3,5,3);
            }
            synchronized (listData) {
                textView.setText(listData.get(i));
            }
            return textView;
        }
    }

    private class StopTask extends AsyncTask<Integer,Integer,Integer>{
        DialogProcessing dialogProcessing = new DialogProcessing(ActivityLogCat.this);
        @Override
        protected void onPreExecute() {
            dialogProcessing.show();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            ApplicationMain.getServer().closeServer();
            try {
                ApplicationMain.getServer().waitServer();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            dialogProcessing.dismiss();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (ApplicationMain.getServer().isRunning()){
            new StopTask().execute();
            return;
        }
        super.onBackPressed();
    }
}
