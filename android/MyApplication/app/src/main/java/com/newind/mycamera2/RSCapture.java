package com.newind.mycamera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Created by newind on 17-7-25.
 */

public class RSCapture implements Allocation.OnBufferAvailableListener {
    private RenderScript mRS;
    private Allocation mInput,mOutput;
    private ScriptC_yuv2rgb mScript;
    public RSCapture(Context context,Size dimensions) {
        mRS = RenderScript.create(context);
        Type.Builder yuvTypeBuilder = new Type.Builder(mRS, Element.YUV(mRS));
        yuvTypeBuilder.setX(dimensions.getWidth());
        yuvTypeBuilder.setY(dimensions.getHeight());
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888);
        mInput = Allocation.createTyped(mRS, yuvTypeBuilder.create(),
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);
        Type.Builder rgbTypeBuilder = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        rgbTypeBuilder.setX(dimensions.getWidth());
        rgbTypeBuilder.setY(dimensions.getHeight());
        mOutput = Allocation.createTyped(mRS, rgbTypeBuilder.create(),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);
        mInput.setOnBufferAvailableListener(this);
        mScript = new ScriptC_yuv2rgb(mRS);
        mScript.set_width(dimensions.getWidth());
        mScript.set_height(dimensions.getHeight());
    }

    public void bindOutput(Surface surface,int width,int height) {
        mScript.set_dst_width(width);
        mScript.set_dst_height(height);
        mOutput.setSurface(surface);
    }

    public Surface getInputSurface() {
        return mInput.getSurface();
    }

    @Override
    public void onBufferAvailable(Allocation a) {
        mInput.ioReceive();
        mScript.set_gCurrentFrame(mInput);
        mScript.forEach_yuv2rgbFrames(mOutput,mOutput);
        mOutput.ioSend();
    }
}
