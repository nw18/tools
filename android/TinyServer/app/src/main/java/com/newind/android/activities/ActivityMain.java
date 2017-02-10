package com.newind.android.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.newind.android.ApplicationMain;
import com.newind.android.R;
import com.newind.android.views.SwitchEx;
import com.newind.util.InputUtil;
import com.newind.util.TextUtil;
import com.newind.util.InputUtil.ParameterException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ActivityMain extends AppCompatActivity implements View.OnClickListener{
    private static final String CONFIG = "config";
    private String[] config = new String[]{
            "ip", "0.0.0.0",
            "http_port", "8080",
            "ftp_port", "2121",
            "http_on", "true",
            "ftp_on", "false",
            "writable", "false",
            "root", "/",
            "json_mode", "true",
            "user_name", "admin",
            "pass_word", "123456",
            "thread_count", "64"
    };

    private Map<String,View> tag2view = new HashMap<>();
    private int[] IDS = new int[] { R.id.bt_browse_ip,R.id.bt_browse_path,R.id.bt_start };

    private void setValue(String key, String value) {
        for (int i = 0; i < config.length - 1; i += 2) {
            if (TextUtil.equal(config[i], key)) {
                config[i + 1] = value;
                break;
            }
        }
    }

    private String getValue(String key) {
        for (int i = 0; i < config.length - 1; i += 2) {
            if (TextUtil.equal(config[i], key)) {
                return config[i + 1];
            }
        }
        return "";
    }

    private void loadLast() {
        SharedPreferences sp = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        String configString = sp.getString(CONFIG, null);
        if (!TextUtils.isEmpty(configString)) {
            String[] lastConfig = configString.split("\n");
            if (lastConfig == null){
                return;
            }
            for (int i = 0; i < config.length - 1 && i < lastConfig.length - 1; i += 2) {
                if (!TextUtils.equals(config[i], lastConfig[i])) {
                    return;
                }
            }
            for (int i = 0; i < config.length - 1 && i < lastConfig.length - 1; i += 2) {
                config[i + 1] = lastConfig[i + 1];
            }
        }
    }

    private void loadConfig2Frame(){
        for (int i = 0; i < config.length - 1; i += 2) {
            View view = tag2view.get(config[i]);
            if (null == view){
                continue;
            }
            if (EditText.class.isInstance(view)){
                ((EditText)view).setText(config[i+1]);
            }else if (SwitchEx.class.isInstance(view)){
                ((SwitchEx)view).setChecked(Boolean.parseBoolean(config[i+1]));
            }
        }
    }

    private void saveLast() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < config.length; i++) {
            sb.append(config[i]);
            sb.append("\n");
        }
        SharedPreferences sp = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CONFIG, sb.toString());
        editor.commit();
    }

    private void saveFrame2Config(){
        for (int i = 0; i < config.length - 1; i += 2) {
            View view = tag2view.get(config[i]);
            if (null == view) {
                continue;
            }
            if (EditText.class.isInstance(view)){
                setValue(view.getTag().toString(), ((EditText)view).getText().toString());
            }else if (SwitchEx.class.isInstance(view)){
                setValue(view.getTag().toString(), String.valueOf(((SwitchEx)view).isChecked()));
            }
        }
    }

    private void findAll(ViewGroup root){
        for (int i = 0; i < root.getChildCount(); i++){
            View view = root.getChildAt(i);
            if (ViewGroup.class.isInstance(view)){
                findAll((ViewGroup)view);
            }else if (SwitchEx.class.isInstance(view)
                    || EditText.class.isInstance(view)){
                tag2view.put(view.getTag().toString(),view);
            }
        }
    }

    private void checkConfigFrame() throws ParameterException {
        String ip = ((EditText)findViewById(R.id.et_ip)).getText().toString();
        if (!InputUtil.isIp(ip)) {
            throw new ParameterException("bad ipv4 address.");
        }
        String http_port = ((EditText)findViewById(R.id.et_http_port)).getText().toString();;
        if (!InputUtil.isPort(http_port)) {
            throw new ParameterException("bad http port.");
        }
        String ftp_port = ((EditText)findViewById(R.id.et_ftp_port)).getText().toString();
        if (!InputUtil.isPort(ftp_port)) {
            throw new ParameterException("bad ftp port.");
        }
        String rootPath = ((EditText)findViewById(R.id.et_path)).getText().toString();
        File rootFile = new File(rootPath);
        if (!rootFile.exists() || !rootFile.isDirectory() || !TextUtil.equal(rootFile.getAbsolutePath(), rootPath)) {
            throw new ParameterException("bad root path.");
        }
        String user_name = ((EditText)findViewById(R.id.et_user)).getText().toString();
        if (!InputUtil.isUserName(user_name)) {
            throw new ParameterException("bad user name.");
        }
        String pass_word = ((EditText)findViewById(R.id.et_pass)).getText().toString();
        if (!InputUtil.isUserName(pass_word)) {
            throw new ParameterException("bad pass word.");
        }
        String thread_count = ((EditText)findViewById(R.id.et_thread)).getText().toString();
        if (!InputUtil.isPort(thread_count) || Integer.parseInt(thread_count) < 4 || Integer.parseInt(thread_count) > 128) {
            throw new ParameterException("thread count [4~128]");
        }
        if (!((SwitchEx)findViewById(R.id.sw_ftp_on)).isChecked() && !((SwitchEx)findViewById(R.id.sw_http_on)).isChecked()){
            throw new ParameterException("nothing to start.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_main);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        for (int id : IDS){
            findViewById(id).setOnClickListener(this);
        }
        File dir = Environment.getExternalStorageDirectory();
        if (dir != null && dir.exists()){
            setValue("root",dir.getAbsolutePath());
        }
        findAll((ViewGroup)findViewById(R.id.ll_content));
        loadLast();
        loadConfig2Frame();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_id_about) {
            Intent it = new Intent(this,ActivityAbout.class);
            startActivity(it);
        }else if (item.getItemId() == R.id.menu_id_reset){
            loadConfig2Frame();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){
            ((EditText)findViewById(R.id.et_path)).setText(data.getStringExtra(ActivityBrowse.KEY_SELECT));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_browse_ip:
                List<String> ipList = InputUtil.getAllIP();
                ipList.remove("127.0.0.1");
                if (ipList.size() == 1){
                    ((EditText)findViewById(R.id.et_ip)).setText(ipList.get(0));
                }else if (ipList.size() > 1){
                    PopupMenu menu = new PopupMenu(this,v, Gravity.BOTTOM|Gravity.LEFT);
                    for (String ip:ipList){
                        menu.getMenu().add(ip);
                    }
                    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            ((EditText)findViewById(R.id.et_ip)).setText(item.getTitle());
                            return true;
                        }
                    });
                    menu.show();
                }
                break;
            case R.id.bt_browse_path:
                File file = Environment.getExternalStorageDirectory();
                if (file == null || !file.exists()){
                    Toast.makeText(this,"存储卡不可用",Toast.LENGTH_SHORT).show();
                    break;
                }
                ActivityBrowse.start(this,file.getAbsolutePath(),1);
                break;
            case R.id.bt_start:
                try{
                    checkConfigFrame();
                    saveFrame2Config();
                    ApplicationMain.getServer().startServer(config);
                } catch (Exception e) {
                    Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    break;
                }
                saveLast();
                Intent it = new Intent(this,ActivityLogCat.class);
                startActivity(it);
                break;
        }
    }
}
