package com.zl.demo;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

public class CameraPreView extends TextureView {
    private static final String TAG = "CameraTextureView";
    private static final int PREVIEW_MSG = 1;
    private Camera mCamera;
    private int cameraAngle = 0;
    private int previewWidth = 640;
    private int previewHeight = 480;
    private Handler mCameraHandler;
    private RectF previewRect;
    private boolean stopPreview = true;
    private float scaleWidth;
    private float scaleHeight;
    private PreviewFrameListener previewFrameListener;

    public CameraPreView(Context context) {
        super(context);
    }

    public CameraPreView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraPreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setKeepScreenOn(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isAvailable() || null == mCamera) {
            setSurfaceTextureListener(surfaceTextureListener);
        } else {
            startPreview();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            stopPreview();
        } else {
            if (mCamera != null && stopPreview) {
                startPreview();
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopPreview();
        closeCamera();
    }

    public void setCameraAngle(int cameraAngle) {
        this.cameraAngle = cameraAngle;
    }

    public void setPrevieSize(int previewWidth, int previewHeight) {
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void setPreviewFrameListener(PreviewFrameListener previewFrameListener) {
        this.previewFrameListener = previewFrameListener;
    }

    private SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
            setPreviewTexture(surface);
            setDisplayOrientation(cameraAngle);
            setCameraParameters();
            setPreviewSize();
            transformTextureView();
            startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            transformTextureView();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void setPreviewSize() {
        if (null == mCamera) return;
        Camera.Parameters mParameters = mCamera.getParameters();
//        List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
//        if (null != sizes && sizes.size() > 0) {
//            for (Camera.Size size : sizes) {
//                Log.i(TAG, "Camera.Size:width" + size.width + ",height" + size.height);
//            }
//        }
        mParameters.setPreviewSize(previewWidth, previewHeight);
        mCamera.setParameters(mParameters);
    }

    private void transformTextureView() {
        int tmpWidth = previewWidth;
        int tmpHeight = previewHeight;
        if (cameraAngle == 90 || cameraAngle == 270) {
            tmpWidth = previewHeight;
            tmpHeight = previewWidth;
        }
        float hScale = (float) getHeight() / tmpHeight;
        float vScale = (float) getWidth() / tmpWidth;
        float scale = Math.max(hScale, vScale);
        float sx = (float) tmpWidth * scale / (float) getWidth();
        float sy = (float) tmpHeight * scale / (float) getHeight();
        Matrix matrix = new Matrix();
        float px = getWidth() / 2;
        float py = getHeight() / 2;
        matrix.setScale(sx, sy, px, py);
        setTransform(matrix);
        RectF rectF = new RectF(0, 0, getWidth(), getHeight());
        matrix.mapRect(rectF);
        float previewLeft = 0 - rectF.left;
        float previewTop = 0 - rectF.top;
        previewRect = new RectF(previewLeft, previewTop, previewLeft + getWidth(), previewTop + getHeight());
        RectF scaleRect = new RectF(0, 0, rectF.right - rectF.left, rectF.bottom - rectF.top);
        scaleWidth = scaleRect.width();
        scaleHeight = scaleRect.height();
        Log.i(TAG, "previewRect:" + previewRect.toString());
        Log.i(TAG, "scaleRect:" + scaleRect.toString());
    }

    public boolean rectInPreview(Rect rect) {
        int tmpWidth = previewWidth;
        int tmpHeight = previewHeight;
        if (cameraAngle == 90 || cameraAngle == 270) {
            tmpWidth = previewHeight;
            tmpHeight = previewWidth;
        }
        float left = rect.left * scaleWidth / tmpWidth;
        float top = rect.top * scaleHeight / tmpHeight;
        float right = rect.right * scaleWidth / tmpWidth;
        float bottom = rect.bottom * scaleHeight / tmpHeight;
        RectF rectF = new RectF(left, top, right, bottom);
        return previewRect.contains(rectF);
    }

    public Rect getRectInPreview(Rect rect) {
        int tmpWidth = previewWidth;
        int tmpHeight = previewHeight;
        if (cameraAngle == 90 || cameraAngle == 270) {
            tmpWidth = previewHeight;
            tmpHeight = previewWidth;
        }
        float sLeft = rect.left * scaleWidth / tmpWidth;
        float sTop = rect.top * scaleHeight / tmpHeight;
        float sRight = rect.right * scaleWidth / tmpWidth;
        float sBottom = rect.bottom * scaleHeight / tmpHeight;
        int left = (int) (sLeft - previewRect.left);
        int top = (int) (sTop - previewRect.top);
        int right = (int) (left + sRight - sLeft);
        int bottom = (int) (top + sBottom - sTop);
        Rect prerect = new Rect(left, top, right, bottom);
        return prerect;
    }

    public Rect getPreviewRect() {
        return new Rect((int) previewRect.left, (int) previewRect.top, (int) previewRect.right, (int) previewRect.bottom);
    }

    private void setCameraParameters() {
        if (null == mCamera) return;
        Camera.Parameters mParameters = mCamera.getParameters();
        List<String> focusModes = mParameters.getSupportedFocusModes();
        if (null != focusModes && focusModes.size() > 0) {
//        for (String focusMode : focusModes) {
//            Log.i(TAG,"FocusMode:"+focusMode);
//        }
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }
        List<String> sceneModes = mParameters.getSupportedSceneModes();
        if (null != sceneModes && sceneModes.size() > 0) {
//            for (String focusMode : sceneModes) {
//                Log.i(TAG, "SceneMode:" + focusMode);
//            }
            if (sceneModes.contains(Camera.Parameters.SCENE_MODE_SPORTS)) {
                mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_SPORTS);
            } else if (sceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION)) {
                mParameters.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
            }
        }
        List<Integer> previewFormats = mParameters.getSupportedPreviewFormats();
        if (null != previewFormats && previewFormats.size() > 0) {
//            for (Integer previewFormat : previewFormats) {
//                Log.i(TAG, "PreviewFormat:" + previewFormat);
//            }
            if (previewFormats.contains(ImageFormat.NV21)) {
                mParameters.setPreviewFormat(ImageFormat.NV21);
            }
        }
        mParameters.setRecordingHint(true);
        mParameters.setVideoStabilization(true);
        mCamera.setParameters(mParameters);
    }

    private void openCamera() {
        if (null == mCamera) {
            mCamera = Camera.open(getCameraId());
        }
    }

    private int getCameraId() {
//        int numberOfCameras = Camera.getNumberOfCameras();
//        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//        for (int i = 0; i < numberOfCameras; i++) {
//            Camera.getCameraInfo(i, cameraInfo);
//            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
//                return i;
//            }
//        }
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }


    private void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setDisplayOrientation(int degree) {
        if (mCamera != null)
            mCamera.setDisplayOrientation(degree);
    }

    private void startPreview() {
        if (mCamera != null) {
            mCamera.setErrorCallback(errorCallback);
            if (previewFrameListener != null) {
                setPreviewCallbackWithBuffer();
            }
            mCamera.startPreview();
            stopPreview = false;
        }
    }


    private Handler getCameraHandler() {
        if (null == mCameraHandler) {
            synchronized (CameraPreView.class) {
                if (null == mCameraHandler) {
                    HandlerThread mCameraThread = new HandlerThread("CameraThread");
                    mCameraThread.setPriority(Thread.MAX_PRIORITY);
                    mCameraThread.start();
                    mCameraHandler = new Handler(mCameraThread.getLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == PREVIEW_MSG && previewFrameListener != null) {
                                previewFrameListener.onPreviewFrame((byte[]) msg.obj);
                            }
                        }
                    };
                }
            }
        }
        return mCameraHandler;
    }

    private void setPreviewCallbackWithBuffer() {
        if (mCamera != null) {
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = ((previewSize.width * previewSize.height) * ImageFormat.getBitsPerPixel(ImageFormat.NV21)) / 8;
            byte[] callbackBuffer = new byte[size];
            mCamera.setPreviewCallbackWithBuffer((data, camera) -> {
                camera.addCallbackBuffer(data);
                if (data != null) {
                    getCameraHandler().sendMessage(getCameraHandler().obtainMessage(1, data));
                }
            });
            mCamera.addCallbackBuffer(callbackBuffer);
        }
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.stopPreview();
            stopPreview = true;
        }
    }

    private void closeCamera() {
        if (mCamera != null) {
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
            Log.i(TAG, "Camera has closed!");
        }
    }

    private Camera.ErrorCallback errorCallback = (error, camera) -> Log.e(TAG, "Camera,onError:" + error);

    public interface PreviewFrameListener {
        void onPreviewFrame(byte[] data);
    }
}
