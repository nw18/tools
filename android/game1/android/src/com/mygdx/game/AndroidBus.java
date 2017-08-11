package com.mygdx.game;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Pair;

import com.mygdx.game.events.CommandBus;

/**
 * Created by newind on 17-8-11.
 */

public class AndroidBus extends CommandBus {

    private HandlerThread handlerThread;
    private Handler commandHandler;

    protected AndroidBus() {
        handlerThread = new HandlerThread(getClass().getName());
        handlerThread.start();
        commandHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Pair<String,Object[]> pair = (Pair<String, Object[]>) msg.obj;
                doDispatch(msg.what,pair.first,pair.second);
            }
        };
    }

    @Override
    public void dispatchAny(String cmd, Object... args) {
        Message msg = commandHandler.obtainMessage(DISP_ANY);
        msg.obj = new Pair<>(cmd,args);
        msg.sendToTarget();
    }

    @Override
    public void dispatchAll(String cmd, Object... args) {
        Message msg = commandHandler.obtainMessage(DISP_ALL);
        msg.obj = new Pair<>(cmd,args);
        msg.sendToTarget();
    }

    public static void Init() {
        theBus = new AndroidBus();
    }
}
