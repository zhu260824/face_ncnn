//
// Created by 朱林 on 2019-08-19.
//

#include "mtcnn_gpu.h"
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include <opencv2/imgproc/types_c.h>
#include <opencv2/imgcodecs/imgcodecs_c.h>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>

#define TAG "MTCNN_GPU"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)

using namespace std;

static MTCNN_GPU *detector = NULL;

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    detector = new MTCNN_GPU();
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    if (NULL != detector) {
        detector->~MTCNN_GPU();
    }
}

JNIEXPORT jboolean JNICALL
Java_com_zl_face_MTCNNGPU_initModel(JNIEnv *env, jobject thiz,
                                    jbyteArray param1, jbyteArray param2, jbyteArray param3,
                                    jbyteArray bin1, jbyteArray bin2, jbyteArray bin3) {
    ncnn::Mat pnet_param;
    ncnn::Mat pnet_bin;
    ncnn::Mat rnet_param;
    ncnn::Mat rnet_bin;
    ncnn::Mat onet_param;
    ncnn::Mat onet_bin;
    {
        int len = env->GetArrayLength(param1);
        pnet_param.create(len, (size_t) 1u);
        env->GetByteArrayRegion(param1, 0, len, (jbyte *) pnet_param);
    }
    {
        int len = env->GetArrayLength(bin1);
        pnet_bin.create(len, (size_t) 1u);
        env->GetByteArrayRegion(bin1, 0, len, (jbyte *) pnet_bin);
    }
    {
        int len = env->GetArrayLength(param2);
        rnet_param.create(len, (size_t) 1u);
        env->GetByteArrayRegion(param2, 0, len, (jbyte *) rnet_param);
    }
    {
        int len = env->GetArrayLength(bin2);
        rnet_bin.create(len, (size_t) 1u);
        env->GetByteArrayRegion(bin2, 0, len, (jbyte *) rnet_bin);
    }
    {
        int len = env->GetArrayLength(param3);
        onet_param.create(len, (size_t) 1u);
        env->GetByteArrayRegion(param3, 0, len, (jbyte *) onet_param);
    }
    {
        int len = env->GetArrayLength(bin3);
        onet_bin.create(len, (size_t) 1u);
        env->GetByteArrayRegion(bin3, 0, len, (jbyte *) onet_bin);
    }
    if (NULL == detector) {
        detector = new MTCNN_GPU();
    }
    bool ret = detector->init(pnet_param, rnet_param, onet_param, pnet_bin, rnet_bin, onet_bin);
    return ret;
}

JNIEXPORT jboolean JNICALL
Java_com_zl_face_MTCNNGPU_initModelPath(JNIEnv *env, jobject thiz, jstring modelPath_){
    if (NULL == modelPath_) {
        return false;
    }
    //获取MTCNN模型的绝对路径的目录（不是/aaa/bbb.bin这样的路径，是/aaa/)
    const char *modelPath = env->GetStringUTFChars(modelPath_, 0);
    if (NULL == modelPath) {
        return false;
    }
    string tFaceModelDir = modelPath;
    string tLastChar = tFaceModelDir.substr(tFaceModelDir.length() - 1, 1);
    //目录补齐/
    if ("\\" == tLastChar) {
        tFaceModelDir = tFaceModelDir.substr(0, tFaceModelDir.length() - 1) + "/";
    } else if (tLastChar != "/") {
        tFaceModelDir += "/";
    }
    LOGD("init, tFaceModelDir=%s", tFaceModelDir.c_str());
    if (NULL == detector) {
        detector = new MTCNN_GPU();
    }
    bool ret = detector->init(tFaceModelDir);
    return ret;
}

JNIEXPORT jfloatArray JNICALL
Java_com_zl_face_MTCNNGPU_detectFace(JNIEnv *env, jobject thiz, jstring imgPath_) {
    const char *imgPath = env->GetStringUTFChars(imgPath_, 0);
    cv::Mat cv_img = cv::imread(imgPath, CV_LOAD_IMAGE_COLOR);
    if (cv_img.empty()) {
        return nullptr;
    }
    std::vector<FaceInfo> faceInfo = detector->detect(cv_img);
    int32_t num_face = static_cast<int32_t>(faceInfo.size());
    LOGD("检测到的人脸数目：%d\n", num_face);
    int out_size = 1 + num_face * 29;
    float *faces = new float[out_size];
    faces[0] = num_face;
    for (int i = 0; i < num_face; i++) {
        float score = faceInfo[i].score;
        int row1 = faceInfo[i].xmin;
        int col1 = faceInfo[i].ymin;
        int row2 = faceInfo[i].xmax;
        int col2 = faceInfo[i].ymax;
        LOGD("faceInfo:score=%.3f;row1=%d,col1=%d,row2=%d,col2=%d\n", score, row1, col1, row2,
             col2);
    }
    jfloatArray tFaces = env->NewFloatArray(out_size);
    env->SetFloatArrayRegion(tFaces, 0, out_size, faces);
    return tFaces;
}

JNIEXPORT jobjectArray JNICALL
Java_com_zl_face_MTCNNGPU_detect(JNIEnv *env, jobject thiz, jbyteArray yuv, jint width,
                                 jint height) {

    jobjectArray faceArgs = nullptr;
    jbyte *pBuf = (jbyte *) env->GetByteArrayElements(yuv, 0);
    cv::Mat image(height + height / 2, width, CV_8UC1, (unsigned char *) pBuf);
    cv::Mat mBgr;
    cv::cvtColor(image, mBgr, CV_YUV2BGR_NV21);
    std::vector<FaceInfo> faceInfo = detector->detect(mBgr);
    int32_t num_face = static_cast<int32_t>(faceInfo.size());
    /**
     * 获取Face类以及其对于参数的签名
     */
    jclass faceClass = env->FindClass("com/zl/face/FaceInfo");//获取Face类
    jmethodID faceClassInitID = (env)->GetMethodID(faceClass, "<init>", "()V");
    jfieldID faceScore = env->GetFieldID(faceClass, "score",
                                         "F");//获取int类型参数confidence
    jfieldID faceRect = env->GetFieldID(faceClass, "faceRect",
                                        "Landroid/graphics/Rect;");//获取faceRect的签名
    /**
     * 获取RECT类以及对应参数的签名
     */
    jclass rectClass = env->FindClass("android/graphics/Rect");//获取到RECT类
    jmethodID rectClassInitID = (env)->GetMethodID(rectClass, "<init>", "()V");
    jfieldID rect_left = env->GetFieldID(rectClass, "left", "I");//获取x的签名
    jfieldID rect_top = env->GetFieldID(rectClass, "top", "I");//获取y的签名
    jfieldID rect_right = env->GetFieldID(rectClass, "right", "I");//获取width的签名
    jfieldID rect_bottom = env->GetFieldID(rectClass, "bottom", "I");//获取height的签名

    faceArgs = (env)->NewObjectArray(num_face, faceClass, 0);

    LOGD("检测到的人脸数目：%d\n", num_face);
    for (int i = 0; i < num_face; i++) {
        float score = faceInfo[i].score;
        int row1 = faceInfo[i].xmin;
        int col1 = faceInfo[i].ymin;
        int row2 = faceInfo[i].xmax;
        int col2 = faceInfo[i].ymax;
        jobject newFace = (env)->NewObject(faceClass, faceClassInitID);
        jobject newRect = (env)->NewObject(rectClass, rectClassInitID);
        (env)->SetIntField(newRect, rect_left, row1);
        (env)->SetIntField(newRect, rect_top, col1);
        (env)->SetIntField(newRect, rect_right, row2);
        (env)->SetIntField(newRect, rect_bottom, col2);
        (env)->SetObjectField(newFace, faceRect, newRect);
        (env)->SetFloatField(newFace, faceScore, score);
        (env)->SetObjectArrayElement(faceArgs, i, newFace);
    }
//    free(pBuf);
    return faceArgs;

}

}