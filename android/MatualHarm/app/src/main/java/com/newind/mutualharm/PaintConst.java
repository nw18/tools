package com.newind.mutualharm;

import android.graphics.Paint;

/**
 * Created by newind on 17-4-10.
 */

public class PaintConst {
    public static Paint LOG_TEXT = new Paint();
    public static Paint PERSON = new Paint();

    static {
        LOG_TEXT.setStyle(Paint.Style.FILL_AND_STROKE);
        LOG_TEXT.setTextSize(14);
        LOG_TEXT.setColor(0x7FFF0000);

        PERSON.setStyle(Paint.Style.FILL_AND_STROKE);
        PERSON.setColor(0xFF00FF00);
    }
}
