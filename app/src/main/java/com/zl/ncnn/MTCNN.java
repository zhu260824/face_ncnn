package com.zl.ncnn;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MTCNN {
    static {
        System.loadLibrary("mtcnn");
    }

    public void init(Context mContext) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ncnn";
        String modelPath = copyModel2SD(mContext, path);
        initModelPath(modelPath);
    }

    public native boolean initModelPath(String modelPath);

    public native float[] detectFace(String imgPath);

    public native FaceInfo[] detect(byte[] img, int width, int height);

    public native float[] extractFeature(String imgPath);

    public native double similar(float[] feature1, float[] feature2);

    private String copyModel2SD(Context mContext, String path) {
        String modelPath = path + File.separator + "mtcnn";
        File file = new File(modelPath);
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            copyAssets(mContext, "mtcnn", modelPath);
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
