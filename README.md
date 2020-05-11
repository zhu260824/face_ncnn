# face_ncnn
Android 人脸检测和识别

[![](https://jitpack.io/v/zhu260824/face_ncnn.svg)](https://jitpack.io/#zhu260824/face_ncnn)

#### 开源算法说明
- 推理算法：[ncnn](https://github.com/Tencent/ncnn)
- 检测算法：[Ultra](https://github.com/Linzaer/Ultra-Light-Fast-Generic-Face-Detector-1MB)
- 识别算法：mobilefacenet

#### 使用
-  添加依赖
    1. Add it in your root build.gradle at the end of repositories:
        ```
        allprojects {
        	repositories {
	            	...
	        	maven { url 'https://jitpack.io' }
        	}
        }
        ```
    2. Add the dependency
        ```
        dependencies {
            implementation 'com.github.zhu260824:face_ncnn:2.0.0'
        }
        ```
- 代码中使用
    1. 初始化SDK（默认初始化）
        ```
        /**
        * 初始化SDK的模型
        *
        * @param mContext 上下文环境
        * @param gpu      推理算法是否运行在GPU上
        * @return 初始话结果
        */
        public boolean init(Context mContext, boolean gpu){}
        ```
        单独初始化
        ```
        /**
        * 初始化人脸检测模型
        *
        * @param binPath   模型bin文件的地址
        * @param paramPath 模型param文件的地址
        * @param reWidth   人脸检测是图片的大小
        * @param reHeight  人脸检测是图片的大小
        * @param gpu       推理算法是否运行在GPU上
        */
        public native boolean initDetector(String binPath, String paramPath, int reWidth,int reHeight, boolean gpu);
        ```
        ```
        /**
        * 初始化人脸识别
        *
        * @param binPath   模型bin文件的地址
        * @param paramPath 模型param文件的地址
        * @param gpu       推理算法是否运行在GPU上
        */
        public native boolean initRecognize(String binPath, String paramPath, boolean gpu);
        ```
    2. 使用
        ```
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
        ```
        ```
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
        ```
    3. 设置
        ```
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
        ```
- 根据使用设备减少so包，缩小apk大小
    ```
    android {
        ...
        defaultConfig {
           ...
            ndk {
                abiFilters "armeabi-v7a", "arm64-v8a" //, "x86", "x86_64"
            }
        }
    }
    ```
#### 总结
- 整个是基于开源的编译和整合的,但是经过我这样，使得使用起来更简单，同时减少了应为环境问题导致编译错误都出现。使得大家可以更快的实现人脸检测和识别.
