//
// Created by 朱林 on 2020/5/9.
//
#ifndef FACE_NCNN_MOBILE_FACENET_H
#define FACE_NCNN_MOBILE_FACENET_H
#pragma once

#include <string>
#include "ncnn/net.h"
#include <vector>

class FaceRecognize {
public:
    FaceRecognize();

    ~FaceRecognize();

    void setThreadNum(int threads);

    void setRunGpu(bool run);

    bool init(const std::string &bin_path, const std::string &param_path, bool run_gpu = false);

    float *getFeature(ncnn::Mat &img);

private:
    ncnn::Net recognizeNet;

    int num_thread = 4;

    bool run_gpu = false;

    int resize_w_h = 112;

    float *runNet(ncnn::Mat &img);
};


double calculSimilar(float *feat1, float *feat2);


#endif //FACE_NCNN_MOBILE_FACENET_H
