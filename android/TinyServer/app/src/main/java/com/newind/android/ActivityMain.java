package com.newind.android;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.StringBuilderPrinter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;


public class ActivityMain extends AppCompatActivity {
    private static final String CONFIG = "config";
    private String[] config = new String[] {
            "ip","0.0.0.0",
            "http_port","8080",
            "ftp_port","2121",
            "http_on","true",
            "ftp_on","false",
            "writable","false",
            "root","D:\\",
            "json_mode","true",
            "user_name","admin",
            "pass_word","123456",
            "thread_count","64"
    };

    private void loadLast(){
        SharedPreferences sp = getSharedPreferences(getClass().getName(),MODE_PRIVATE);
        String configString = sp.getString(CONFIG,null);
        if (!TextUtils.isEmpty(configString)){
            String[] lastConfig = configString.split("\n");
            for (int i = 0; i < config.length; i+=2){
                if (!TextUtils.equals(config[i],lastConfig[i])){
                    return;
                }
            }
            for (int i = 0; i < config.length - 1; i+=2){
                config[i+1] = lastConfig[i+1];
            }
        }
    }

    private void saveLast(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < config.length; i++){
            sb.append(config[i]);
        }
        SharedPreferences sp = getSharedPreferences(getClass().getName(),MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONFIG,sb.toString());
        editor.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setIcon(R.mipmap.logo);
            actionBar.setTitle(R.string.title_main);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        loadLast();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_about){
            // TODO: 17-2-8 show about.
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
