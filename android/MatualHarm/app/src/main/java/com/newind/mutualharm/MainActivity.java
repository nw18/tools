package com.newind.mutualharm;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import com.newind.core.DevLog;
import com.newind.core.FrameRateCounter;
import com.newind.core.IRendObject;
import com.newind.core.SurfaceRender;

public class MainActivity extends Activity implements IRendObject {
    MainSurfaceRender surfaceRender;
    FrameRateCounter frameRateCounter = new FrameRateCounter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceRender = new MainSurfaceRender(this);
        surfaceRender.attach((SurfaceView) findViewById(R.id.sv_main));
        surfaceRender.setup();
        surfaceRender.getWorldCoordinate().setRectVirtual(new RectF(0,1,1,0));
    }

    @Override
    protected void onDestroy() {
        surfaceRender.release();
        super.onDestroy();
    }

    @Override
    public void paint(SurfaceRender render, Canvas canvas) {
        canvas.drawColor(0xFF000000);
        //frameRateCounter.countFrame();
        //surfaceRender.getDevLog().println(String.format("fps:%d",(int)(frameRateCounter.getFrameRate()+0.5f)));
        long begin = System.currentTimeMillis();
        Path path = new Path();
        path.moveTo(0,0);
        for(int i = 0; i < 1000;i++) {
            for (int j = 0; j < 1000; j++) {
                path.lineTo((i+0.5f) * render.getWidth() / 1000f,(j+0.5f) * render.getHeight() / 1000f);
            }
        }
        Log.e("XXX",String.format("create time %d ms",System.currentTimeMillis() - begin));
        begin = System.currentTimeMillis();
        canvas.drawPath(path, PaintConst.PERSON);
        Log.e("XXX",String.format("draw time %d ms",System.currentTimeMillis() - begin));
    }
}
