package com.newind.mutualharm;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.provider.Settings;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;

import com.newind.core.FrameRateCounter;
import com.newind.core.SurfaceRender;

public class MainActivity extends Activity implements SurfaceRender.IRend {
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
    PointF point = new PointF();
    @Override
    public void paint(SurfaceRender render, Canvas canvas) {
        canvas.drawColor(0xFF000000);
        frameRateCounter.countFrame();
        surfaceRender.getDevLog().println(String.format("fps:%d",(int)(frameRateCounter.getFrameRate()+0.5f)));
        point.set((System.currentTimeMillis() % 1000L) / 1000f,0.1f);
        render.getWorldCoordinate().virtual2real(point,point);
        canvas.drawCircle(point.x,point.y,render.getWorldCoordinate().dis_v2r_y(0.05f),PaintConst.PERSON);
    }
}
