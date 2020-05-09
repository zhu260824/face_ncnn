//
// Created by 朱林 on 2020/5/8.
//

#ifndef FACE_NCNN_NDK_UTIL_H
#define FACE_NCNN_NDK_UTIL_H

#include <string>
#include <vector>
#include <android/log.h>
#include <jni.h>
#include "ultra_face.h"

static std::string getDirPath(JNIEnv *env, jstring dirPath) {
    if (NULL == dirPath) {
        return nullptr;
    }
    const char *file_path = env->GetStringUTFChars(dirPath, 0);
    if (NULL == file_path) {
        return nullptr;
    }
    std::string t_file_path = file_path;
    std::string tLastChar = t_file_path.substr(t_file_path.length() - 1, 1);
    //目录补齐/
    if ("\\" == tLastChar) {
        t_file_path = t_file_path.substr(0, t_file_path.length() - 1) + "/";
    } else if (tLastChar != "/") {
        t_file_path += "/";
    }
    return t_file_path;
}

static std::string getFilePath(JNIEnv *env, jstring filePath) {
    if (NULL == filePath) {
        return nullptr;
    }
    const char *file_path = env->GetStringUTFChars(filePath, 0);
    if (NULL == file_path) {
        return nullptr;
    }
    return file_path;
}

static jobjectArray native2JavaFacInfo(JNIEnv *env, std::vector<FaceInfo> face_info,
                                       jobjectArray faceArgs, int32_t num_face) {
    jclass faceClass = env->FindClass("com/zl/face/FaceInfo");
    jmethodID faceClassInitID = (env)->GetMethodID(faceClass, "<init>", "()V");
    jfieldID faceScore = env->GetFieldID(faceClass, "score", "F");
    jfieldID faceRect = env->GetFieldID(faceClass, "faceRect", "Landroid/graphics/Rect;");
    jclass rectClass = env->FindClass("android/graphics/Rect");
    jmethodID rectClassInitID = (env)->GetMethodID(rectClass, "<init>", "()V");
    jfieldID rect_left = env->GetFieldID(rectClass, "left", "I");
    jfieldID rect_top = env->GetFieldID(rectClass, "top", "I");
    jfieldID rect_right = env->GetFieldID(rectClass, "right", "I");
    jfieldID rect_bottom = env->GetFieldID(rectClass, "bottom", "I");
    faceArgs = (env)->NewObjectArray(num_face, faceClass, 0);
    for (int i = 0; i < num_face; i++) {
        float score = face_info[i].score;
        int row1 = face_info[i].x1;
        int col1 = face_info[i].y1;
        int row2 = face_info[i].x2;
        int col2 = face_info[i].y2;
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
    return faceArgs;
}


#endif //FACE_NCNN_NDK_UTIL_H
