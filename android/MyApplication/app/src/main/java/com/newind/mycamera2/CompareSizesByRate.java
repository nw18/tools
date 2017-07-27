package com.newind.mycamera2;

import android.util.Size;

import java.util.Comparator;

/**
 * Created by yuyidong on 15-1-8.
 */
public class CompareSizesByRate implements Comparator<Size> {
    double rt;
    double st;
    CompareSizesByRate(double rate,double area){
        this.rt = rate;
        this.st = area;
    }
    @Override
    public int compare(Size o1, Size o2) {

        double r1 = o1.getWidth() * 1.0 / o1.getHeight();
        double r2 = o2.getWidth() * 1.0 / o2.getHeight();
        double s1 = o1.getWidth() * o1.getHeight();
        double s2 = o2.getWidth() * o2.getHeight();
        double v1 = rt * 1000000 / (rt + Math.abs(r1 - rt)) + st * 1000 / (st + Math.abs(s1 - st));
        double v2 = rt * 1000000 / (rt + Math.abs(r2 - rt)) + st * 1000 / (st + Math.abs(s2 - st));
        double diff = v1 - v2;
        return diff == 0.0 ? 0 : (int) (diff / Math.abs(diff));
    }

}
