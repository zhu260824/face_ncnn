//
// Created by ZL on 2019-06-17.
//

#ifndef NCNNDEMO_MOBILEFACENET_H
#define NCNNDEMO_MOBILEFACENET_H

#include <string>
#include "net.h"
#include "opencv2/opencv.hpp"

class Recognize {
public:
    Recognize(const std::string &model_path);

    ~Recognize();

    void extractFeature(const cv::Mat &img, std::vector<float> &feature);

    double calculSimilar(std::vector<float> &v1, std::vector<float> &v2);

private:
    void RunNet(ncnn::Mat &img_, std::vector<float> &feature);

    ncnn::Net Recognet;
};

#endif //NCNNDEMO_MOBILEFACENET_H
