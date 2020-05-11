//
// Created by 朱林 on 2020/5/8.
//

#ifndef FACE_NCNN_ULTRA_FACE_H
#define FACE_NCNN_ULTRA_FACE_H

#pragma once

#include "ncnn/gpu.h"
#include "ncnn/net.h"
#include <algorithm>
#include <iostream>
#include <string>
#include <vector>
#include "base_util.h"

#define num_featuremap 4
#define hard_nms 1
#define blending_nms 2


class UltraFace {
public:
    UltraFace();

    ~UltraFace();

    void setThreadNum(int threads);

    void setScoreThreshold(int score);

    void setRunGpu(bool run);

    void setReSize(int w, int h);

    bool init(const std::string &bin_path, const std::string &param_path, int resize_w = 320,
              int resize_h = 240, bool run_gpu = false);

    std::vector<FaceInfo> detect(ncnn::Mat &img);

private:
    ncnn::Net ultraNet;

    int num_thread = 4;

    float score_threshold = 0.7;

    bool run_gpu = false;

    float iou_threshold = 0.3;

    int resize_w = 320;

    int resize_h = 240;

    std::vector<std::vector<float>> priors = {};

    int num_anchors;

    const float mean_vals[3] = {127, 127, 127};

    const float norm_vals[3] = {1.0 / 128, 1.0 / 128, 1.0 / 128};

    const float center_variance = 0.1;

    const float size_variance = 0.2;

    const std::vector<std::vector<float>> min_boxes = {
            {10.0f,  16.0f,  24.0f},
            {32.0f,  48.0f},
            {64.0f,  96.0f},
            {128.0f, 192.0f, 256.0f}};

    const std::vector<float> strides = {8.0, 16.0, 32.0, 64.0};

    void generateBBox(std::vector<FaceInfo> &bbox_collection, ncnn::Mat scores, ncnn::Mat boxes,
                      float score_threshold, int num_anchors, int image_w, int image_h);

    void nms(std::vector<FaceInfo> &input, std::vector<FaceInfo> &output, int type = blending_nms);

};


#endif //FACE_NCNN_ULTRA_FACE_H
