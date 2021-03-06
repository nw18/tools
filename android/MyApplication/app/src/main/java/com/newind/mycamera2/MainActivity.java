package com.newind.mycamera2;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static abstract class ParamRunnable<T> implements Runnable{
        protected T target;
        ParamRunnable(T param) {
            this.target = param;
        }
    }

    private HandlerThread mDataProcessor,mDataProcessor2;
    private Handler mDataHandler,mDataHandler2;
    private TextureView texCamera,texPreview;
    private TextView tvResult;
    private Runnable toClose;
    private ImageReader mImageReader;
    private BallManager ballManager = new BallManager();
    private ClipboardManager clipboardManager;
    private RSCapture mRSCapture;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        texCamera = (TextureView) findViewById(R.id.texCamera);
        texPreview = (TextureView) findViewById(R.id.texPreview);
        tvResult = (TextView) findViewById(R.id.tvResult);
        findViewById(R.id.btGetIt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("text", tvResult.getText().toString()));
            }
        });

        mDataProcessor = new HandlerThread("DataProcessor");
        mDataProcessor.start();
        mDataProcessor2 = new HandlerThread("DataProcessor2");
        mDataProcessor2.start();
        mDataHandler = new Handler(mDataProcessor.getLooper());
        mDataHandler2 = new Handler(mDataProcessor2.getLooper());
        TextureView.SurfaceTextureListener listener = new TextureView.SurfaceTextureListener() {
            int count = 0;
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                count++;
                if (count == 2) {
                    mDataHandler.post(new ParamRunnable<SurfaceTexture>(surface) {
                        @Override
                        public void run() {
                            InitCamera(target);
                        }
                    });
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        };

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                Toast.makeText(MainActivity.this, "copped", Toast.LENGTH_SHORT).show();
                clipboardManager.removePrimaryClipChangedListener(this);
            }
        });
        texCamera.setSurfaceTextureListener(listener);
        texPreview.setSurfaceTextureListener(listener);
    }

    @Override
    protected void onDestroy() {
        mDataHandler.post(new Runnable() {
            @Override
            public void run() {
                mDataProcessor.getLooper().quit();
            }
        });
        try {
            mDataProcessor.join(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mDataHandler2.post(new Runnable() {
            @Override
            public void run() {
                mDataProcessor2.getLooper().quit();
            }
        });
        try {
            mDataProcessor2.join(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mDataHandler.post(new ParamRunnable<SurfaceTexture>(texCamera.getSurfaceTexture()) {
                @Override
                public void run() {
                    InitCamera(target);
                }
            });
        } else {
            Log.e("onRPResult", "fail");
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        MessageDigest digest = null;
        @Override
        public void onImageAvailable(ImageReader reader) {
            MyDebug.invoked();
            Image img = reader.acquireLatestImage();
            if (img == null) {
                return;
            }
            try {
                MyDebug.e(String.format("frame size %d %d",img.getWidth(),img.getHeight()));
                if (null == digest) {
                    digest = MessageDigest.getInstance("MD5");
                }
                for (int i = 0; i < img.getPlanes().length; i++) {
                    Image.Plane plane = img.getPlanes()[i];
                    digest.update(plane.getBuffer());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ballManager.randomMove(new BigInteger(1, digest.digest()));
                        tvResult.setText(ballManager.randomSelect());
                    }
                });
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            img.close();
        }
    };

    private void InitCamera(SurfaceTexture surfaceTexture) {
        MyDebug.invoked();
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String[] CameraIdList = cameraManager.getCameraIdList();
            //获取可用相机设备列表
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(CameraIdList[0]);
            //在这里可以通过CameraCharacteristics设置相机的功能,当然必须检查是否支持
            characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            List<Size> sizeList = Arrays.asList(map.getOutputSizes(ImageFormat.JPEG));
            for (Size size : sizeList) {
                MyDebug.i(String.format("support size:%d %d",size.getWidth(),size.getHeight()));
            }
            Size largest = Collections.max(sizeList, new CompareSizesByRate(4.0/3,4000*3000));
            SurfaceTexture texture = texCamera.getSurfaceTexture();
            sizeList = Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888));
            Size middle = Collections.max(sizeList, new CompareSizesByRate(4.0/3,1200*1600));
            texture.setDefaultBufferSize(middle.getHeight(),middle.getWidth());
            mImageReader = ImageReader.newInstance(middle.getHeight(),middle.getWidth(),ImageFormat.YUV_420_888,4);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,mDataHandler2);
            mRSCapture = new RSCapture(this,middle);
            MyDebug.e(String.format("selected size midle %d %d large %d %d preview %d %d",
                    middle.getWidth(),middle.getHeight(),largest.getWidth(),largest.getHeight(),
                    texCamera.getWidth(),texCamera.getHeight()));
            //就像这样
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1001);
                return;
            }

            cameraManager.openCamera(CameraIdList[0], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    startPreview(camera, texCamera);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, mDataHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startPreview(CameraDevice camera, TextureView mPreviewView) {
        MyDebug.invoked();
        toClose = new ParamRunnable<CameraDevice>(camera) {
            @Override
            public void run() {
                target.close();
            }
        };
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        Surface surface = new Surface(texture);
        Surface surfacePreview = new Surface(texPreview.getSurfaceTexture());
        try {

//            final CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            final CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            mRSCapture.bindOutput(surface,mPreviewView.getWidth(),mPreviewView.getHeight());
            surface = mRSCapture.getInputSurface();
            captureBuilder.addTarget(surface);
            //captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.addTarget(surfacePreview);
            camera.createCaptureSession(Arrays.asList(surface,mImageReader.getSurface(),surfacePreview), new CameraCaptureSession.StateCallback() {
            //camera.createCaptureSession(Arrays.asList(mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
            //captureBuilder.addTarget(mAllocInput.getSurface());
            //camera.createCaptureSession(Arrays.asList(surface, mAllocInput.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    MyDebug.invoked();
                    try {
                        session.setRepeatingRequest(captureBuilder.build(), mSessionCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("startPreview", "onConfigureFailed");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                           TotalCaptureResult result) {
                MyDebug.invoked();
            }

            @Override
            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                            CaptureResult partialResult) {
                MyDebug.invoked();
            }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (toClose != null) {
            toClose.run();
            toClose = null;
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
