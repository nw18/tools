package com.newind.mutualharm;

import android.graphics.Paint;

/**
 * Created by newind on 17-4-10.
 */

public class PaintConst {
    public static Paint PERSON = new Paint();

    static {
        PERSON.setStyle(Paint.Style.FILL_AND_STROKE);
        PERSON.setColor(0xFF00FF00);
    }
}
