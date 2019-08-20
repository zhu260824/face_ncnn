package com.zl.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.zl.face.FaceInfo;
import com.zl.face.MTCNNGPU;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CameraActivity extends Activity {
    private CameraPreView cameraPreView;
    private FaceRectView rectView;
    private ExecutorService detectService = Executors.newCachedThreadPool();
    private MTCNNGPU mtcnn;
    private boolean inDetect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        cameraPreView = findViewById(R.id.camera_preview);
        rectView = findViewById(R.id.rectview);
        cameraPreView.setCameraAngle(0);
//        cameraPreView.setCameraAngle(180);
        cameraPreView.setPrevieSize(getCameraWidth(), getCameraHeight());
        cameraPreView.setPreviewFrameListener(previewFrameListener);
        mtcnn = new MTCNNGPU();
        mtcnn.initPath(CameraActivity.this);
    }


    private CameraPreView.PreviewFrameListener previewFrameListener = new CameraPreView.PreviewFrameListener() {
        @Override
        public void onPreviewFrame(byte[] data) {
            try {
                previewFrame(data);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            data = YUVUtil.YUV420spRotate90Clockwise(data, getCameraWidth(), getCameraHeight());
//            data = YUVUtil.YUV420spMirror(data, getCameraHeight(), getCameraWidth());
//            detect(data, getCameraHeight(), getCameraWidth());
//            data = YUVUtil.YUV420spRotate90Anticlockwise(data, getCameraWidth(), getCameraHeight());
//            data = YUVUtil.YUV420spRotate90Anticlockwise(data, getCameraHeight(), getCameraWidth());
//            detect(data, getCameraWidth(), getCameraHeight());
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    private int getCameraWidth() {
        return 640;
    }

    private int getCameraHeight() {
        return 480;
    }


    private void previewFrame(byte[] data) throws ExecutionException, InterruptedException {
        if (inDetect)return;
        FaceDetect faceDetect = new FaceDetect(data);
        Future<FaceInfo> future = detectService.submit(faceDetect);
        FaceInfo faceInfo = future.get();
        if (faceInfo != null) {
            Log.i("canshu", "faceInfo:" + faceInfo.toString());
            rectView.post(() -> rectView.setRect(cameraPreView.getRectInPreview(faceInfo.getFaceRect())));
        }
    }

    class FaceDetect implements Callable<FaceInfo> {
        private byte[] data;

        public FaceDetect(byte[] data) {
            this.data = data;
        }

        @Override
        public FaceInfo call() throws Exception {
            if (null == data || data.length < 1) {
                return null;
            }
            byte[] img = new byte[data.length];
            System.arraycopy(data, 0, img, 0, data.length);
            img = YUVUtil.YUV420spRotate90Clockwise(img, getCameraWidth(), getCameraHeight());
            img = YUVUtil.YUV420spMirror(img, getCameraHeight(), getCameraWidth());
            FaceInfo faceInfo = detectFace(img, getCameraHeight(), getCameraWidth());
            return faceInfo;
        }
    }

    public synchronized FaceInfo detectFace(byte[] data, int width, int height) {
        inDetect = true;
        long startTime = System.currentTimeMillis();
        FaceInfo[] faceInfos = mtcnn.detect(data, width, height);
        inDetect = false;
        Log.i("canshu", "time:" + (System.currentTimeMillis() - startTime));
        if (faceInfos != null && faceInfos.length > 0) {
            return faceInfos[0];
        }
        return null;
    }

}
