package com.mygdx.game.utils;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;

/**
 * Created by Administrator on 2017/8/11.
 */

public class ViewUtils {
    public static abstract class Tracer<T extends View>{
        void doTrace(View view) {
            onTrace((T) view);
        }

        public abstract void onTrace(T view);
    }

    public static void traceAll(View vRoot,Pair<Class<? extends View>,Tracer> ... processors) {
        LinkedList<View> viewList = new LinkedList<>();
        viewList.add(vRoot);
        while(!viewList.isEmpty()) {
            View view = viewList.removeFirst();
            for (Pair<Class<? extends View>,Tracer> pt : processors) {
                if (pt.first.isInstance(view)) {
                    pt.second.doTrace(view);
                }
            }
            if (ViewGroup.class.isInstance(view)) {
                ViewGroup viewGroup = (ViewGroup) view;
                for(int i =0; i < viewGroup.getChildCount(); i++) {
                    viewList.add(viewGroup.getChildAt(i));
                }
            }
        }
    }
}
