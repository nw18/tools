package com.newind.core;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ThreadFactory;

/**
 * Created by newind on 17-4-1.
 */

public class SurfaceRender {
    private boolean isSurfaceReady = false;
    private boolean isSurfaceRending = true;
    private SurfaceHolder mHolder;
    private int mFormat;
    private int mWidth;
    private int mHeight;
    private IRendObject mRender;
    private Thread mThread;
    private WorldCoordinate worldCoordinate = new WorldCoordinate();

    private SurfaceHolder.Callback myCallback = new SurfaceHolder.Callback(){

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            synchronized (SurfaceRender.this) {
                isSurfaceReady = true;
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            synchronized (SurfaceRender.this){
                mFormat = format;
                mWidth = width;
                mHeight = height;
                worldCoordinate.setRectReal(new RectF(0,0,width,height));
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            synchronized (SurfaceRender.this) {
                isSurfaceReady = false;
            }
        }
    };

    private Runnable mRendRunnable = new Thread()
    {
        @Override
        public void run() {
            while(isSurfaceRending){
                Canvas canvas = null;
                synchronized (SurfaceRender.this){
                    if (isSurfaceReady) {
                        canvas = mHolder.lockCanvas();
                    }
                }
                if(canvas == null){
                    try { Thread.sleep(40); }catch (Exception e){ }
                    continue;
                }
                render(canvas);
                mHolder.unlockCanvasAndPost(canvas);
            }
        }
    };

    public SurfaceRender(IRendObject render){
        mRender = render;
    }

    protected void render(Canvas canvas){
        if (mRender != null){
            mRender.paint(this,canvas);
        }
    }

    public void attach(SurfaceView surfaceView){
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(myCallback);
    }

    public void setup(){
        mThread = new Thread(mRendRunnable);
        mThread.start();
        mThread.setPriority(Thread.MAX_PRIORITY);
    }

    public void release(){
        try {
            isSurfaceRending = false;
            mThread.join();
            mThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WorldCoordinate getWorldCoordinate() {
        return worldCoordinate;
    }

    public synchronized int getFormat(){
        return mFormat;
    }

    public synchronized int getWidth(){
        return mWidth;
    }

    public synchronized int getHeight(){
        return mHeight;
    }
}
