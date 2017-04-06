package com.newind.core;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by newind on 17-4-1.
 */

public class SurfaceRender {
    private boolean isSurfaceReady = false;
    private boolean isSurfaceRending = true;
    private SurfaceHolder mHolder;
    private SurfaceHolder.Callback myCallback = new SurfaceHolder.Callback(){

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            isSurfaceReady = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            isSurfaceReady = false;
        }
    };

    Thread myThread = new Thread()
    {
        @Override
        public void run() {
            while(isSurfaceRending){
                Canvas canvas = null;
                if (isSurfaceReady) {
                    canvas = mHolder.lockCanvas();
                }
                if(canvas == null){
                    try { Thread.sleep(100); }catch (Exception e){ }
                    return;
                }
                render(canvas);
                mHolder.unlockCanvasAndPost(canvas);
            }
        }
    };

    protected void render(Canvas canvas){

    }

    public void attach(SurfaceView surfaceView){
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(myCallback);
    }

    public void setup(){
        myThread.start();
        myThread.setPriority(Thread.MAX_PRIORITY);
    }

    public void release(){
        try {
            isSurfaceRending = false;
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
