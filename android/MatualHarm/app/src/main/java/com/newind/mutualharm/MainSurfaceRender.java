package com.newind.mutualharm;

import android.graphics.Canvas;

import com.newind.core.DevLog;
import com.newind.core.IRendObject;
import com.newind.core.SurfaceRender;

/**
 * Created by newind on 17-4-10.
 */

public class MainSurfaceRender extends SurfaceRender {
    private DevLog devLog = new DevLog(null);

    public MainSurfaceRender(IRendObject render) {
        super(render);
    }

    @Override
    protected void render(Canvas canvas) {
        devLog.setCanvas(canvas);
        super.render(canvas);
    }

    public DevLog getDevLog() {
        return devLog;
    }
}
