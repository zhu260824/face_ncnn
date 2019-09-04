//
// Created by 朱林 on 2019-08-19.
//

#ifndef NCNNDEMO_MTCNN_GPU_H
#define NCNNDEMO_MTCNN_GPU_H

#include <ncnn/net.h>
#include "opencv2/opencv.hpp"

using namespace std;
using namespace ncnn;

struct FaceInfo {
    float score;
    float xmin;
    float ymin;
    float xmax;
    float ymax;
    float area;
    float landmark[10];
    float bbox_reg[4];
};

class MTCNN_GPU {

public:
    MTCNN_GPU();

    ~MTCNN_GPU();

    void setThreadNum(int threads);

    void setMinSize(int minSize);

    bool init(ncnn::Mat pnet_param, ncnn::Mat rnet_param, ncnn::Mat onet_param,
              ncnn::Mat pnet_bin, ncnn::Mat rnet_bin, ncnn::Mat onet_bin);

    bool init(const string &model_path);

    vector<FaceInfo> detect(cv::Mat &img_);


private:
    Net pNet, rNet, oNet;

    bool isInit = false;

    int num_threads = 2;

    int min_size = 40;

    const int MIN_DET_SIZE = 12;

    const float pre_facetor = 0.709f;

    const float mean_val[3] = {127.5, 127.5, 127.5};

    const float norm_val[3] = {0.0078125, 0.0078125, 0.0078125};

    const float nms_threshold[3] = {0.5f, 0.7f, 0.7f};

    const float threshold[3] = {0.8f, 0.8f, 0.6f};

    std::vector<FaceInfo> pnetInfos, rnetInfos, onetInfos;

    void generateBox(ncnn::Mat score, ncnn::Mat location, std::vector<FaceInfo> &boundingBox_,
                     float scale);

    void nms(vector<FaceInfo> &boundingBox_,
             const float overlap_threshold, string modelname = "Union");

    void refine(vector<FaceInfo> &vecBbox, const int &height, const int &width, bool square);

    void PNet(ncnn::Mat img);

    void RNet(ncnn::Mat img);

    void ONet(ncnn::Mat img);
};


#endif //NCNNDEMO_MTCNN_GPU_H
