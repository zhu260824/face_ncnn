//
// Created by ZL on 2019-06-17.
//

#include "mobilefacenet.h"

Recognize::Recognize(const std::string &model_path) {
    std::string param_files = model_path + "mobilefacenet.param";
    std::string bin_files = model_path + "mobilefacenet.bin";
    int pi = Recognet.load_param(param_files.c_str());
    int bi = Recognet.load_model(bin_files.c_str());
}

Recognize::~Recognize() {
    Recognet.clear();
}

void Recognize::RunNet(ncnn::Mat &img_, std::vector<float> &feature) {
    ncnn::Extractor ex = Recognet.create_extractor();
    //ex.set_num_threads(2);
    ex.set_light_mode(true);
    ex.input("data", img_);
    ncnn::Mat out;
    ex.extract("fc1", out);
    feature.resize(128);
    for (int j = 0; j < 128; j++) {
        feature[j] = out[j];
    }
}

void Recognize::extractFeature(const cv::Mat &img, std::vector<float> &feature) {
    ncnn::Mat ncnn_img = ncnn::Mat::from_pixels_resize(img.data, ncnn::Mat::PIXEL_BGR2RGB, img.cols,
                                                       img.rows, 112, 112);
    RunNet(ncnn_img, feature);
}

double Recognize::calculSimilar(std::vector<float> &v1, std::vector<float> &v2) {
    assert(v1.size() == v2.size());
    double ret = 0.0, mod1 = 0.0, mod2 = 0.0;
    for (std::vector<double>::size_type i = 0; i != v1.size(); ++i) {
        ret += v1[i] * v2[i];
        mod1 += v1[i] * v1[i];
        mod2 += v2[i] * v2[i];
    }
    return (ret / sqrt(mod1) / sqrt(mod2) + 1) / 2.0;
}
