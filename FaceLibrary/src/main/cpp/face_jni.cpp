//
// Created by 朱林 on 2020/5/8.
//
#include <android/log.h>
#include <jni.h>
#include <string>
#include <vector>
#include "ultra_face.h"
#include "mobile_facenet.h"
#include "base_util.h"
#include <iostream>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/imgproc.hpp>

#define TAG "FaceLibrary"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)


UltraFace *detector = new UltraFace();
FaceRecognize *recognize = new FaceRecognize();

extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_6;
}

extern "C"
JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    if (NULL != detector) {
        detector->~UltraFace();
    }
    if (NULL != recognize) {
        recognize->~FaceRecognize();
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_zl_face_FaceDetector_setThreadNum(JNIEnv *env, jobject thiz, jint threads) {
    detector->setThreadNum(threads);
    recognize->setThreadNum(threads);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_zl_face_FaceDetector_setScoreThreshold(JNIEnv *env, jobject thiz, jint score) {
    detector->setScoreThreshold(score);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_zl_face_FaceDetector_setRunGpu(JNIEnv *env, jobject thiz, jboolean run) {
    detector->setRunGpu(run);
    recognize->setRunGpu(run);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_zl_face_FaceDetector_setReSize(JNIEnv *env, jobject thiz, jint w, jint h) {
    detector->setReSize(w, h);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_zl_face_FaceDetector_initDetector(JNIEnv *env, jobject thiz, jstring bin_path,
                                           jstring param_path, jint width,
                                           jint height, jboolean gpu) {
    std::string binPath = getFilePath(env, bin_path);
    std::string paramPath = getFilePath(env, param_path);
    return detector->init(binPath, paramPath, width, height, gpu);
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_zl_face_FaceDetector_detect(JNIEnv *env, jobject thiz, jstring img_path) {
    jobjectArray faceArgs = nullptr;
    const char *imgPath = env->GetStringUTFChars(img_path, 0);
    cv::Mat cv_img = cv::imread(imgPath);
    ncnn::Mat inmat = ncnn::Mat::from_pixels(cv_img.data, ncnn::Mat::PIXEL_BGR2RGB,
                                             cv_img.cols, cv_img.rows);
    std::vector<FaceInfo> face_info = detector->detect(inmat);
    int32_t num_face = static_cast<int32_t>(face_info.size());
    LOGD("检测到的人脸数目：%d\n", num_face);
    faceArgs = native2JavaFacInfo(env, face_info, faceArgs, num_face);
    return faceArgs;
}

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_zl_face_FaceDetector_detectYuv(JNIEnv *env, jobject thiz, jbyteArray yuv, jint width,
                                        jint height) {
    jobjectArray faceArgs = nullptr;
    jbyte *pBuf = (jbyte *) env->GetByteArrayElements(yuv, 0);
    cv::Mat image = cv::Mat(height, width, CV_8UC1, (unsigned char *) pBuf);
    ncnn::Mat inmat = ncnn::Mat::from_pixels(image.data, ncnn::Mat::PIXEL_GRAY2RGB,
                                             image.cols, image.rows);
    std::vector<FaceInfo> face_info = detector->detect(inmat);
    int32_t num_face = static_cast<int32_t>(face_info.size());
    LOGD("检测到的人脸数目：%d\n", num_face);
    faceArgs = native2JavaFacInfo(env, face_info, faceArgs, num_face);
    free(pBuf);
    return faceArgs;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_zl_face_FaceDetector_initRecognize(JNIEnv *env, jobject thiz, jstring bin_path,
                                            jstring param_path, jboolean gpu) {
    std::string binPath = getFilePath(env, bin_path);
    std::string paramPath = getFilePath(env, param_path);
    return recognize->init(binPath, paramPath, gpu);
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_zl_face_FaceDetector_getFeature(JNIEnv *env, jobject thiz, jstring img_path) {
    jfloatArray face_feature = nullptr;
    const char *imgPath = env->GetStringUTFChars(img_path, 0);
    cv::Mat cv_img = cv::imread(imgPath);
    ncnn::Mat inmat = ncnn::Mat::from_pixels(cv_img.data, ncnn::Mat::PIXEL_BGR2RGB,
                                             cv_img.cols, cv_img.rows);
    float *feature = recognize->getFeature(inmat);
    face_feature = env->NewFloatArray(feature_dim);
    env->SetFloatArrayRegion(face_feature, 0, feature_dim, feature);
    return face_feature;
}

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_zl_face_FaceDetector_getFeatureYuv(JNIEnv *env, jobject thiz, jbyteArray yuv, jint width,
                                            jint height) {
    jfloatArray face_feature = nullptr;
    jbyte *pBuf = (jbyte *) env->GetByteArrayElements(yuv, 0);
    cv::Mat image = cv::Mat(height, width, CV_8UC1, (unsigned char *) pBuf);
    ncnn::Mat inmat = ncnn::Mat::from_pixels(image.data, ncnn::Mat::PIXEL_GRAY2RGB,
                                             image.cols, image.rows);
    float *feature = recognize->getFeature(inmat);
    face_feature = env->NewFloatArray(feature_dim);
    env->SetFloatArrayRegion(face_feature, 0, feature_dim, feature);
    return face_feature;
}

extern "C"
JNIEXPORT jdouble JNICALL
Java_com_zl_face_FaceDetector_featureCompare(JNIEnv *env, jobject thiz, jfloatArray feature1,
                                             jfloatArray feature2) {
    jfloat *feat1 = env->GetFloatArrayElements(feature1, 0);
    jfloat *feat2 = env->GetFloatArrayElements(feature2, 0);
    return calculSimilar(feat1, feat2);
}