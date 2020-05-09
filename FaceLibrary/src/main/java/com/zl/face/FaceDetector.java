package com.zl.face;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceDetector {

    static {
        System.loadLibrary("faceLibrary");
    }

    public boolean init(Context mContext, boolean gpu) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ncnn";
        String modelPath = copyModel2SD(mContext, "ultra", path);
        String binPath = modelPath + File.separator + "RFB-320.bin";
        String paramPath = modelPath + File.separator + "RFB-320.param";
        boolean ret = initDetector(binPath, paramPath, 320, 240, gpu);
        if (!ret) {
            return false;
        }
        modelPath = copyModel2SD(mContext, "facenet", path);
        binPath = modelPath + File.separator + "mobilefacenet.bin";
        paramPath = modelPath + File.separator + "mobilefacenet.param";
        ret = initRecognize(binPath, paramPath, gpu);
        return ret;
    }

    public native void setThreadNum(int threads);

    public native void setScoreThreshold(int score);

    public native void setRunGpu(boolean run);

    public native void setReSize(int w, int h);

    public native boolean initDetector(String binPath, String paramPath, int reWidth,
                                       int reHeight, boolean gpu);

    public native FaceInfo[] detect(String imgPath);

    public native FaceInfo[] detectYuv(byte[] yuv, int width, int height);

    public native boolean initRecognize(String binPath, String paramPath, boolean gpu);

    public native float[] getFeature(String imgPath);

    public native float[] getFeatureYuv(byte[] yuv, int width, int height);

    public native double featureCompare(float[] feature1, float[] feature2);

    private String copyModel2SD(Context mContext, String model, String path) {
        String modelPath = path + File.separator + model;
        File file = new File(modelPath);
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            copyAssets(mContext, model, modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modelPath;
    }

    /**
     * 复制asset文件到指定目录
     *
     * @param oldPath asset下的路径
     * @param newPath SD卡下保存路径
     */
    public static void copyAssets(Context mContext, String oldPath, String newPath) throws IOException {
        String fileNames[] = mContext.getAssets().list(oldPath);
        if (fileNames.length > 0) {
            File file = new File(newPath);
            file.mkdirs();
            for (String fileName : fileNames) {
                copyAssets(mContext, oldPath + File.separator + fileName, newPath + File.separator + fileName);
            }
        } else {
            InputStream is = mContext.getAssets().open(oldPath);
            FileOutputStream fos = new FileOutputStream(new File(newPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
        }
    }
}
