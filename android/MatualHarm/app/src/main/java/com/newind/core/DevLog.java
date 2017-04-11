package com.newind.core;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.newind.core.Util;

/**
 * Created by newind on 17-4-10.
 */

public class DevLog {
    private static final int TEXT_SIZE = 12;
    private static final int LINE_SIZE = 16;
    private Paint paint = new Paint();

    private Canvas canvas;
    private int startX = Util.dpToPx(TEXT_SIZE);
    private int startY = Util.dpToPx(TEXT_SIZE + LINE_SIZE);

    public DevLog(Canvas canvas){
        this.canvas = canvas;
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(Util.dpToPx(TEXT_SIZE));
        paint.setColor(0x7FFF0000);
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
        this.startY =  Util.dpToPx(TEXT_SIZE + LINE_SIZE);;
    }

    public void println(String line){
        canvas.drawText(line,startX,startY, paint);
        startY +=  Util.dpToPx(LINE_SIZE);;
    }
}
