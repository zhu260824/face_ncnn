package com.zl.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zl.face.MTCNNGPU;

import java.io.File;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private ImageView ivT1, ivT2;
    private Button btnT1, btnT2, btnAll;
    private TextView tvT1, tvT2, tvAll;
    private MTCNNGPU mtcnn;
    private String imgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "mnn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivT1 = findViewById(R.id.iv_t1);
        btnT1 = findViewById(R.id.btn_t1);
        tvT1 = findViewById(R.id.tv_t1);
        ivT2 = findViewById(R.id.iv_t2);
        btnT2 = findViewById(R.id.btn_t2);
        tvT2 = findViewById(R.id.tv_t2);
        btnAll = findViewById(R.id.btn_all);
        tvAll = findViewById(R.id.tv_all);
        ivT1.post(() -> ivT1.setImageBitmap(ImageUtil.getBitmapByAssets(ivT1.getContext(), "test.jpg")));
        ivT2.post(() -> ivT2.setImageBitmap(ImageUtil.getBitmapByAssets(ivT2.getContext(), "test2.jpg")));
        btnT1.setOnClickListener(v -> {
            String msg = detect(v.getContext(), "test.jpg", ivT1);
            tvT1.setText(msg);
        });
        btnT2.setOnClickListener(v -> {
            String msg = detect(v.getContext(), "test2.jpg", ivT2);
            tvT2.setText(msg);
        });
//        btnAll.setOnClickListener(v -> );
        mtcnn = new MTCNNGPU();
        mtcnn.initPath(MainActivity.this);
        imgPath = ImageUtil.copyImage2SD(MainActivity.this, imgPath);
    }

    private String detect(Context mContext, String assetsPath, ImageView imageView) {
        String msg = "";
        Bitmap bitmap = ImageUtil.getBitmapByAssets(mContext, assetsPath);
        msg = msg + "image size = " + bitmap.getWidth() + "x" + bitmap.getHeight() + "\n";
//        Bitmap cb = bitmap.copy(bitmap.getConfig(), true);
//        Canvas canvas = new Canvas(cb);
//        //图像上画矩形
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);//不填充
//        paint.setStrokeWidth(10);  //线的宽度


        long startTime = System.currentTimeMillis();
//        byte[] data = ImageUtil.bitmapToNv21(bitmap, bitmap.getWidth(), bitmap.getHeight());
        float[] faces = mtcnn.detectFace(imgPath + File.separator + assetsPath);
        Log.i("canshu", "耗时：" + (System.currentTimeMillis() - startTime));
        Log.i("canshu", Arrays.toString(faces));
//        List<Face> faces = hyper.getTrackingInfo();
//        msg = msg + "face num = " + faces.size() + "\n";
//        msg = msg + "detectTime = " + (System.currentTimeMillis() - startTime) + "ms";
//        for (Face face : faces) {
//            Log.i("canshu", face.toString());
//            canvas.drawRect(face.getLeft(), face.getTop(), face.getRight(), face.getBottom(), paint);
//        }
//        imageView.post(() -> imageView.setImageBitmap(cb));
        return msg;
    }
}
