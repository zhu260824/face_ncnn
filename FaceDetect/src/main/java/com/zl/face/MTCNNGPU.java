package com.zl.face;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MTCNNGPU {

    static {
        System.loadLibrary("faceDetect");
    }


    public void init(Context mContext) throws IOException {
        byte[] param1, param2, param3, bin1, bin2, bin3;
        {
            InputStream is = mContext.getAssets().open("mtcnn/det1.param");
            int available = is.available();
            param1 = new byte[available];
            is.read(param1);
            is.close();
        }
        {
            InputStream is = mContext.getAssets().open("mtcnn/det2.param");
            int available = is.available();
            param2 = new byte[available];
            is.read(param2);
            is.close();
        }
        {
            InputStream is = mContext.getAssets().open("mtcnn/det3.param");
            int available = is.available();
            param3 = new byte[available];
            is.read(param3);
            is.close();
        }
        {
            InputStream is = mContext.getAssets().open("mtcnn/det1.bin");
            int available = is.available();
            bin1 = new byte[available];
            is.read(bin1);
            is.close();
        }
        {
            InputStream is = mContext.getAssets().open("mtcnn/det2.bin");
            int available = is.available();
            bin2 = new byte[available];
            is.read(bin2);
            is.close();
        }
        {
            InputStream is = mContext.getAssets().open("mtcnn/det3.bin");
            int available = is.available();
            bin3 = new byte[available];
            is.read(bin3);
            is.close();
        }
        initModel(param1, param2, param3, bin1, bin2, bin3);
    }

    public void initPath(Context mContext) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ncnn";
        String modelPath = copyModel2SD(mContext, path);
        initModelPath(modelPath);
    }

    public native boolean initModel(byte[] param1, byte[] param2, byte[] param3, byte[] bin1, byte[] bin2, byte[] bin3);

    public native boolean initModelPath(String modelPath);


    public native float[] detectFace(String imgPath);

    public native FaceInfo[] detect(byte[] img, int width, int height);

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
