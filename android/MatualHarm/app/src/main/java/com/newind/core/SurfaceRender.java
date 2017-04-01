package com.newind.core;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by newind on 17-4-1.
 */

public class SurfaceRender {

    SurfaceHolder.Callback myCallback = new SurfaceHolder.Callback(){
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        }
    };

    Thread myThread = new Thread()
    {
        @Override
        public void run() {
            while(true){
                render();
            }
        }
    };

    protected void render(){

    }

    public void attach(SurfaceView surfaceView){
        surfaceView.getHolder().addCallback(myCallback);
    }

    public void setup(){
        myThread.start();
        myThread.setPriority(Thread.MAX_PRIORITY);
    }

    public void release(){
        try {
            // TODO: 17-4-1 make the thread stop.
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
