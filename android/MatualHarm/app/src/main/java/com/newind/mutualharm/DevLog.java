package com.newind.mutualharm;

import android.graphics.Canvas;

/**
 * Created by newind on 17-4-10.
 */

public class DevLog {
    private Canvas canvas;
    private int startX = 10;
    private int startY = 20;
    public DevLog(Canvas canvas){
        this.canvas = canvas;
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.startY = 20;
    }

    public void println(String line){
        canvas.drawText(line,startX,startY,PaintConst.LOG_TEXT);
        startY += 20;
    }
}
