package com.newind.ftpclient;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class FtpUploadService extends IntentService implements Runnable {

    public static final String ACTION_SETUP = "com.newind.ftpclient.ACTION_SETUP";
    public static final String ACTION_DELETE = "com.newind.ftpclient.ACTION_DELETE";
    public static final String ACTION_ADD = "com.newind.ftpclient.ACTION_ADD";
    public static final String ACTION_CLEAR = "com.newind.ftpclient.ACTION_CLEAR";

    public static final String EXTRA_ID = "com.newind.ftpclient.extra.EXTRA_ID";

    public FtpUploadService() {
        super("FtpUploadService");
    }

    private List<DBManager.FileUploadInfo> downloadList = new ArrayList<>();
    private Thread mUploadingThread = null;
    private SimpleDateFormat  SDF_DATE = new SimpleDateFormat("yyyMMdd");

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (this) {
            if (null == mUploadingThread) {
                mUploadingThread = new Thread(this);
                mUploadingThread.start();
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_SETUP.equals(action)) {
                synchronized (downloadList) {
                    TaskManager.getInstance().getAll(downloadList);
                    for (int i = downloadList.size() - 1; i >= 0; i--) {
                        DBManager.FileUploadInfo info = downloadList.get(i);
                        if (info.status > DBManager.FileUploadInfo.STAT_UPLOADING) {
                            downloadList.remove(i);
                        }
                    }
                }
                synchronized (this) {
                    if (null == mUploadingThread) {
                        mUploadingThread = new Thread(this);
                        mUploadingThread.start();
                    }
                }
            } else if (ACTION_ADD.equals(action)) {
                String fileID = intent.getStringExtra(EXTRA_ID);
                int id = Integer.parseInt(fileID);
                DBManager.FileUploadInfo info = TaskManager.getInstance().findByID(id);
                if (info != null) {
                    synchronized (downloadList) {
                        downloadList.add(info);
                    }
                }
            } else if (ACTION_DELETE.equals(action)) {
                String fileID = intent.getStringExtra(EXTRA_ID);
                int id = Integer.parseInt(fileID);
                DBManager.FileUploadInfo info = TaskManager.getInstance().findByID(id);
                if (info != null) {
                    synchronized (downloadList) {
                        if (downloadList.contains(info)) {
                            downloadList.remove(info);
                        }else {
                            //try to stop the uploading task.
                            tryDeleteTask(info);
                        }
                    }
                }
            }else if (ACTION_CLEAR.equals(action)) {
                synchronized (downloadList) {
                    downloadList.clear();
                }
            }
        }
    }


    @Override
    public void run() {
        while (true) {
            DBManager.FileUploadInfo info = null;
            synchronized (downloadList) {
                if (!downloadList.isEmpty()) {
                    info = downloadList.remove(0);
                }
            }
            if (info == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                continue;
            }

            handleTask(info);
        }
        synchronized (this) {
            mUploadingThread = null;
        }
        stopSelf();
    }

    private boolean ftpContains(String name,FTPClient ftpClient) throws IOException {
        String[] files = ftpClient.listNames();
        if (files == null) {
            return false;
        }

        for (String file : files) {
            if(TextUtils.equals(name,file)) {
                return true;
            }
        }
        return false;
    }

    private void handleTask(DBManager.FileUploadInfo info) {
        File localFile = new File(info.file_path);
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(10 * 1000);
        ftpClient.setControlEncoding("UTF-8");
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        String address = preference.getString(getString(R.string.pref_key_address),"");
        int port = Integer.parseInt(preference.getString(getString(R.string.pref_key_port),"21"));
        String userName = preference.getString(getString(R.string.pref_key_user_name),"");
        String passWord = preference.getString(getString(R.string.pref_key_pass_word),"");
        String rootDir = preference.getString(getString(R.string.pref_key_collect_root),"");
        boolean is_in_time = preference.getBoolean(getString(R.string.pref_key_collect_in_time),true);
        boolean is_in_ip = preference.getBoolean(getString(R.string.pref_key_collect_in_ip),true);
        boolean is_pasv_mode = preference.getBoolean(getString(R.string.pref_key_pasv_mode),true);
        boolean is_store_okay = false;
        long read_sum = 0;
        long file_length = localFile.length();
        try (FileInputStream stream = new FileInputStream(localFile)){
            while (true) {
                ftpClient.connect(address, port);
                if (!ftpClient.isConnected()) {
                    Log.e("XXX", "ftp connect fail.");
                    break;
                }
                if(!ftpClient.setFileType(FTP.BINARY_FILE_TYPE)){
                    Log.e("XXX","setFileType(FTP.BINARY_FILE_TYPE) fail.");
                }
                if(!ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE)) {
                    Log.e("XXX","setFileTransferMode(FTP.STREAM_TRANSFER_MODE) fail.");
                }
                if (is_pasv_mode) {
                    ftpClient.enterLocalPassiveMode();
                }else {
                    ftpClient.enterLocalActiveMode();
                }
                if(!ftpClient.login(userName,passWord)) {
                    Log.e("XXX", "ftp connect login.");
                    break;
                }
                if (!TextUtils.isEmpty(rootDir)) {
                    if (!ftpContains(rootDir, ftpClient)) {
                        ftpClient.makeDirectory(rootDir);
                    }
                    if (!ftpClient.changeWorkingDirectory(rootDir)) {
                        Log.e("XXX", "cwd fail " + rootDir);
                        break;
                    }
                }
                if (is_in_time) {
                    String timeStr = SDF_DATE.format(System.currentTimeMillis());
                    if (!ftpContains(timeStr, ftpClient)) {
                        ftpClient.makeDirectory(timeStr);
                    }
                    if (!ftpClient.changeWorkingDirectory(timeStr)) {
                        Log.e("XXX", "cwd fail " + timeStr);
                        break;
                    }
                }
                if (is_in_ip) {
                    String ipAddres = ftpClient.getLocalAddress().getHostAddress();
                    if (!ftpContains(ipAddres, ftpClient)) {
                        ftpClient.makeDirectory(ipAddres);
                    }
                    if (!ftpClient.changeWorkingDirectory(ipAddres)) {
                        Log.e("XXX", "cwd fail " + ipAddres);
                        break;
                    }
                }
                OutputStream target = ftpClient.storeFileStream(localFile.getName());
                int read = 0;
                byte[] buffer = new byte[4 * 1024];
                while ((read = stream.read(buffer)) > 0) {
                    read_sum += read;
                    target.write(buffer, 0, read);
                    float percent = (read_sum * 1024L / file_length) / 1024f;
                    if (percent != info.progress) {
                        TaskManager.getInstance().updateItem(info.id, percent, DBManager.FileUploadInfo.STAT_UPLOADING);
                    }
                }
                target.close();
                is_store_okay = true;
                //is_store_okay = ftpClient.storeFile(localFile.getName(),stream);
                break;
            }
            ftpClient.disconnect();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (is_store_okay) {
                TaskManager.getInstance().updateItem(info.id,1,DBManager.FileUploadInfo.STAT_FINISHED);
            }else {
                TaskManager.getInstance().updateItem(info.id,1,DBManager.FileUploadInfo.STAT_ERROR);
            }
        }
    }

    private void tryDeleteTask(DBManager.FileUploadInfo info) {
        // TODO: 17-4-21  
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
