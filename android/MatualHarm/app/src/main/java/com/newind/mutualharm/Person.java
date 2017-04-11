package com.newind.mutualharm;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.newind.core.IRendObject;
import com.newind.core.ShapeRound;
import com.newind.core.SurfaceRender;
import com.newind.core.WorldCoordinate;

/**
 * Created by newind on 17-4-10.
 */

public class Person extends ShapeRound implements IRendObject{
    private boolean isFemale;
    private float blood;

    public boolean isFemale() {
        return isFemale;
    }

    public void setFemale(boolean female) {
        isFemale = female;
    }

    public void setBlood(float blood) {
        this.blood = blood;
    }

    public float getBlood() {
        return blood;
    }

    @Override
    public void paint(SurfaceRender render, Canvas canvas) {
        WorldCoordinate coordinate = render.getWorldCoordinate();

    }
}
