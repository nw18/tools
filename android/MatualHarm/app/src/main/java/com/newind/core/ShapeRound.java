package com.newind.core;

import android.graphics.PointF;

/**
 * Created by newind on 17-4-11.
 */

public class ShapeRound {
    protected float radius;
    protected PointF position = new PointF();

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public PointF getPosition() {
        return position;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getRadius() {
        return radius;
    }
}
