package com.newind.core;

/**
 * Created by newind on 17-4-10.
 */

public class FrameRateCounter {
    private int frameCount = 0;
    private long lastTime = 0;
    private float frameRate = 0;
    public FrameRateCounter(){
        lastTime = System.currentTimeMillis();
    }

    public void countFrame(){
        frameCount ++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 1000L){
            synchronized (this){
                frameRate = frameCount * 1000f / (currentTime - lastTime);
            }
            lastTime = currentTime;
            frameCount = 0;
        }
    }

    public synchronized float getFrameRate() {
        return frameRate;
    }
}
