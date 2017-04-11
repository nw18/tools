package com.newind.mutualharm;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;

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
        frameRateCounter.countFrame();
        surfaceRender.getDevLog().println(String.format("fps:%d",(int)(frameRateCounter.getFrameRate()+0.5f)));
    }
}
