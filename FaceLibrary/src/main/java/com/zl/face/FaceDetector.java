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

    /**
     * 初始化SDK的模型
     *
     * @param mContext 上下文环境
     * @param gpu      推理算法是否运行在GPU上
     * @return 初始话结果
     */
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

    /**
     * 设置算法推理线程数
     *
     * @param threads 线程数
     */
    public native void setThreadNum(int threads);

    /**
     * 设置人脸检测的置信度
     *
     * @param score 置信度 默认0.7
     */
    public native void setScoreThreshold(int score);

    /**
     * 是置推理算法是否运行在GPU上
     */
    public native void setRunGpu(boolean run);

    /**
     * 设置人脸检测是图片的大小
     */
    public native void setReSize(int w, int h);

    /**
     * 初始化人脸检测模型
     *
     * @param binPath   模型bin文件的地址
     * @param paramPath 模型param文件的地址
     * @param reWidth   人脸检测是图片的大小
     * @param reHeight  人脸检测是图片的大小
     * @param gpu       推理算法是否运行在GPU上
     */
    public native boolean initDetector(String binPath, String paramPath, int reWidth,
                                       int reHeight, boolean gpu);

    /**
     * 解析人脸信息
     *
     * @param imgPath 图片地址
     * @return 人脸信息
     */
    public native FaceInfo[] detect(String imgPath);

    /**
     * 解析人脸信息
     *
     * @param yuv    摄像图输出的预览帧
     * @param width  帧的宽度
     * @param height 帧的高度
     * @deprecated 使用这个方法，需要将摄像图帧旋转至0度
     */
    public native FaceInfo[] detectYuv(byte[] yuv, int width, int height);

    /**
     * 初始化人脸识别
     *
     * @param binPath   模型bin文件的地址
     * @param paramPath 模型param文件的地址
     * @param gpu       推理算法是否运行在GPU上
     */
    public native boolean initRecognize(String binPath, String paramPath, boolean gpu);

    /**
     * 获取图片特征值
     *
     * @param imgPath 图片地址
     * @return 特征值
     */
    public native float[] getFeature(String imgPath);

    /**
     * 获取图片特征值
     *
     * @param yuv    摄像图输出的预览帧
     * @param width  帧的宽度
     * @param height 帧的高度
     * @deprecated 使用这个方法，需要将摄像图帧旋转至0度
     */
    public native float[] getFeatureYuv(byte[] yuv, int width, int height);

    /**
     * 特征值比对
     *
     * @param feature1 特征值
     * @param feature2 特征值
     * @return 特征值相似度
     */
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
