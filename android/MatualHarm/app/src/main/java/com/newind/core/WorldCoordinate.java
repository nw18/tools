package com.newind.core;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * Created by newind on 17-4-10.
 */

public class WorldCoordinate {
    private RectF rectReal = new RectF();
    private RectF rectVirtual = new RectF();
    private float xScaleV2R;
    private float yScaleV2R;

    private void initScale(){
        if (rectReal.width() != 0
                && rectReal.height() != 0
                && rectVirtual.width() != 0
                && rectVirtual.height() != 0){
            xScaleV2R = rectReal.width() / rectVirtual.width() ;
            yScaleV2R = rectReal.height() / rectVirtual.height();
        }
    }

    public WorldCoordinate(){

    }

    public WorldCoordinate(RectF rectReal,RectF rectVirtual){
        this.rectReal.set(rectReal);
        this.rectVirtual.set(rectVirtual);
        initScale();
    }

    public void setRectReal(RectF rectReal) {
        this.rectReal.set(rectReal);
        initScale();
    }

    public void setRectVirtual(RectF rectVirtual) {
        this.rectVirtual.set(rectVirtual);
        initScale();
    }

    public float r2v_x(float x){
        x -= rectReal.left;
        return rectVirtual.left + x / xScaleV2R;
    }

    public float r2v_y(float y){
        y -= rectReal.bottom;
        return rectVirtual.bottom + y / yScaleV2R;
    }

    public float v2r_x(float x){
        x -= rectVirtual.left;
        return rectReal.left + x * xScaleV2R;
    }

    public float v2r_y(float y){
        y -= rectVirtual.bottom;
        return rectReal.bottom + y * yScaleV2R;
    }

    public float dis_v2r_x(float x){
        return x * Math.abs(xScaleV2R);
    }

    public float dis_v2r_y(float y){
        return y * Math.abs(yScaleV2R);
    }

    //for action at a object.
    public void real2virtual(PointF from,PointF to){
        to.x = r2v_x(from.x);
        to.y = r2v_y(from.y);
    }

    //for paint a object
    public void virtual2real(PointF from,PointF to){
        to.x = v2r_x(from.x );
        to.y = v2r_y(from.y);
    }

    //for paint a object
    public void virtual2real(RectF from,RectF to){
        to.left = v2r_x(from.left);
        to.right = v2r_x(from.right);
        to.top = v2r_y(from.top);
        to.bottom = v2r_y(from.bottom);
    }
}
