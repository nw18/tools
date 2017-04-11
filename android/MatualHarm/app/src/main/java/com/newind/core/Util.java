package com.newind.core;

import android.app.Application;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by newind on 17-4-11.
 */

public class Util {
    public static int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                theApplication.getResources().getDisplayMetrics());
    }

    public static Application theApplication;
}
