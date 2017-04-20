package com.newind.ftpclient;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by newind on 17-4-20.
 */

public class TaskManager {
    private static final int MSG_NEW = 0;
    private static final int MSG_DEL = 1;
    private static final int MSG_UPDATE = 2;
    private static final int MSG_CLEAR = 3;

    private static TaskManager _instance_ = null;
    private DBManager dbManager;
    private List<DBManager.FileUploadInfo> uploadInfoList;
    private List<IListener> listenerList = new ArrayList<>();
    private int maxID = -1;

    public static TaskManager getInstance() {
        if (_instance_ == null) {
            _instance_ = new TaskManager();
        }
        return _instance_;
    }

    private TaskManager(){
        threadDispatcher.start();
        dbManager = DBManager.instance(MainApplication.getInstance());
        uploadInfoList = dbManager.getFileUploadDB().fetchList(-1);
        for (DBManager.FileUploadInfo info : uploadInfoList) {
            if (info.id > maxID) {
                maxID = info.id;
            }
        }
        while (mHandler == null) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }


    private Handler mHandler = null;

    private Thread threadDispatcher = new Thread() {

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    onMessage(msg);
                }
            };

            Looper.myLooper().loop();
        }
    };

    private void onMessage(Message msg) {
        synchronized (listenerList) {
            for (IListener listener : listenerList) {
                switch (msg.what) {
                    case MSG_NEW:
                        synchronized (msg.obj) {
                            listener.onAdd((DBManager.FileUploadInfo) msg.obj);
                        }
                        break;
                    case MSG_DEL:
                        synchronized (msg.obj) {
                            listener.onDelete((DBManager.FileUploadInfo) msg.obj);
                        }
                        break;
                    case MSG_UPDATE:
                        synchronized (msg.obj) {
                            listener.onUpdate((DBManager.FileUploadInfo) msg.obj);
                        }
                        break;
                    case MSG_CLEAR:
                        listener.onClear();
                }
            }
        }
    }

    private void sendMessage(int what, Object object) {
        if (mHandler == null) {
            Log.e("XXX","handler not init now.");
        }
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = object;
        msg.sendToTarget();
    }

    public TaskManager instance() {
        if (null == _instance_) {
            _instance_ = new TaskManager();
        }
        return _instance_;
    }

    private DBManager.FileUploadInfo findByID(int id) {
        for (DBManager.FileUploadInfo info : uploadInfoList) {
            if (info.id == id) {
                return info;
            }
        }
        return null;
    }

    public synchronized void addItem(String filePath) {
        dbManager.getFileUploadDB().addItem(filePath);
        List<DBManager.FileUploadInfo> list = dbManager.getFileUploadDB().fetchList(maxID);
        if (list.size() != 0) {
            DBManager.FileUploadInfo info = list.get(list.size() - 1);
            uploadInfoList.add(info);
            maxID = info.id;
            sendMessage(MSG_NEW,info);
        }
    }

    public synchronized void delItem(int id) {
        DBManager.FileUploadInfo info = findByID(id);
        if (info != null) {
            dbManager.getFileUploadDB().deleteItem(id);
            uploadInfoList.remove(info);
            sendMessage(MSG_DEL,info);
        }
    }

    public synchronized void getAll(List<DBManager.FileUploadInfo> list) {
        list.clear();
        list.addAll(uploadInfoList);
    }

    public synchronized void updateItem(int id,float process,int status) {
        DBManager.FileUploadInfo info = findByID(id);
        if (info != null) {
            dbManager.getFileUploadDB().updateItem(id,process,status);
            synchronized (info) {
                info.progress = process;
                info.status = status;
            }
            sendMessage(MSG_UPDATE,info);
        }
    }

    public synchronized void clearAll() {
        dbManager.getFileUploadDB().clearAll();
        uploadInfoList.clear();
        sendMessage(MSG_CLEAR,null);
    }

    interface IListener {
        void onAdd(DBManager.FileUploadInfo info);
        void onDelete(DBManager.FileUploadInfo info);
        void onUpdate(DBManager.FileUploadInfo info);
        void onClear();
    }

    public void addListener(IListener listener) {
        synchronized (listenerList) {
            if (!listenerList.contains(listener)) {
                listenerList.add(listener);
            }
        }
    }

    public void rmvListener(IListener listener) {
        synchronized (listenerList) {
            listenerList.remove(listener);
        }
    }
}
