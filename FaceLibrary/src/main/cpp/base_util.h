//
// Created by 朱林 on 2020/5/9.
//

#ifndef FACE_NCNN_BASE_UTIL_H
#define FACE_NCNN_BASE_UTIL_H

#include <string>
#include <vector>
#include <jni.h>
#include "ncnn/net.h"

typedef struct FaceInfo {
    float x1;
    float y1;
    float x2;
    float y2;
    float score;
} FaceInfo;

ncnn::Mat resize(ncnn::Mat src, int w, int h);

ncnn::Mat bgr2rgb(ncnn::Mat src);

ncnn::Mat rgb2bgr(ncnn::Mat src);

void getAffineMatrix(float *src_5pts, const float *dst_5pts, float *M);

void warpAffineMatrix(ncnn::Mat src, ncnn::Mat &dst, float *M, int dst_w, int dst_h);

std::string getDirPath(JNIEnv *env, jstring dirPath);

std::string getFilePath(JNIEnv *env, jstring filePath);

jobjectArray native2JavaFacInfo(JNIEnv *env, std::vector<FaceInfo> face_info,
                                       jobjectArray faceArgs, int32_t num_face);

#endif //FACE_NCNN_BASE_UTIL_H
