package com.newind.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

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
    private LinkedList<String> listCache = new LinkedList<>(),listData = new LinkedList<>();
    private boolean hasNewData;
    private Timer refreshTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_cat);
        lv_log_cat = ((ListView) findViewById(R.id.lv_log_cat));
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
                    // TODO: 2017/2/8 notify data set change.
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
            // TODO: 17-2-8 stop the server
        }
        return true;
    }
}
