//
// Created by 朱林 on 2019-08-19.
//

#include <deque>
#include <vector>
#include "mtcnn_gpu.h"

static bool CompareFaceScore(FaceInfo lsh, FaceInfo rsh) {
    return lsh.score < rsh.score;
}

void MTCNN_GPU::generateBox(ncnn::Mat score, ncnn::Mat location,
                            std::vector<FaceInfo> &boundingBox_, float scale) {
    const int stride = 2;
    const int cellsize = 12;
    float *p = score.channel(1);
    FaceInfo faceInfo = {0};
    float inv_scale = 1.0f / scale;
    for (int row = 0; row < score.h; row++) {
        for (int col = 0; col < score.w; col++) {
            if (*p > threshold[0]) {
                faceInfo.score = *p;
                faceInfo.xmin = lround((stride * col + 1) * inv_scale);
                faceInfo.ymin = lround((stride * row + 1) * inv_scale);
                faceInfo.xmax = lround((stride * col + 1 + cellsize) * inv_scale);
                faceInfo.ymax = lround((stride * row + 1 + cellsize) * inv_scale);
                faceInfo.area = (faceInfo.xmax - faceInfo.xmin) * (faceInfo.ymax - faceInfo.ymin);
                const int index = row * score.w + col;
                for (int channel = 0; channel < 4; channel++) {
                    faceInfo.bbox_reg[channel] = location.channel(channel)[index];
                }
                boundingBox_.push_back(faceInfo);
            }
            p++;
        }
    }
}

void MTCNN_GPU::nms(vector<FaceInfo> &boundingBox_,
                    const float overlap_threshold, string modelname) {
    if (boundingBox_.empty()) {
        return;
    }
    sort(boundingBox_.begin(), boundingBox_.end(), CompareFaceScore);
    float IOU = 0;
    float maxX = 0;
    float maxY = 0;
    float minX = 0;
    float minY = 0;
    std::vector<int> vecPick;
    size_t pickCount = 0;
    std::multimap<float, int> vScores;
    const size_t num_boxes = boundingBox_.size();
    vecPick.resize(num_boxes);
    for (int i = 0; i < num_boxes; ++i) {
        vScores.insert(std::pair<float, int>(boundingBox_[i].score, i));
    }
    while (!vScores.empty()) {
        int last = vScores.rbegin()->second;
        vecPick[pickCount] = last;
        pickCount += 1;
        for (auto it = vScores.begin(); it != vScores.end();) {
            int it_idx = it->second;
            maxX = std::max(boundingBox_.at(it_idx).xmin, boundingBox_.at(last).xmin);
            maxY = std::max(boundingBox_.at(it_idx).ymin, boundingBox_.at(last).ymin);
            minX = std::min(boundingBox_.at(it_idx).xmax, boundingBox_.at(last).xmax);
            minY = std::min(boundingBox_.at(it_idx).ymax, boundingBox_.at(last).ymax);
            //maxX1 and maxY1 reuse
            maxX = ((minX - maxX + 1) > 0) ? (minX - maxX + 1) : 0;
            maxY = ((minY - maxY + 1) > 0) ? (minY - maxY + 1) : 0;
            //IOU reuse for the area of two bbox
            IOU = maxX * maxY;
            if (modelname == ("Union"))
                IOU = IOU / (boundingBox_.at(it_idx).area + boundingBox_.at(last).area - IOU);
            else if (modelname == ("Min")) {
                IOU = IOU / ((boundingBox_.at(it_idx).area < boundingBox_.at(last).area) ?
                             boundingBox_.at(it_idx).area : boundingBox_.at(last).area);
            }
            if (IOU > overlap_threshold) {
                it = vScores.erase(it);
            } else {
                it++;
            }
        }
    }
    vecPick.resize(pickCount);
    std::vector<FaceInfo> tmp_;
    tmp_.resize(pickCount);
    for (int i = 0; i < pickCount; i++) {
        tmp_[i] = boundingBox_[vecPick[i]];
    }
    boundingBox_ = tmp_;
}

void MTCNN_GPU::refine(vector<FaceInfo> &vecBbox, const int &height, const int &width,
                       bool square) {
    if (vecBbox.empty()) {
        cout << "Face is empty!!" << endl;
        return;
    }
    float bbw = 0, bbh = 0, maxSide = 0;
    float h = 0, w = 0;
    float xmin = 0, ymin = 0, xmax = 0, ymax = 0;
    for (auto &it : vecBbox) {
        bbw = it.xmax - it.xmin + 1;
        bbh = it.ymax - it.ymin + 1;
        xmin = it.xmin + it.bbox_reg[0] * bbw;
        ymin = it.ymin + it.bbox_reg[1] * bbh;
        xmax = it.xmax + it.bbox_reg[2] * bbw;
        ymax = it.ymax + it.bbox_reg[3] * bbh;
        if (square) {
            w = xmax - xmin + 1;
            h = ymax - ymin + 1;
            maxSide = (h > w) ? h : w;
            xmin += (w - maxSide) * 0.5f;
            ymin += (h - maxSide) * 0.5f;
            it.xmax = lround(xmin + maxSide - 1);
            it.ymax = lround(ymin + maxSide - 1);
            it.xmin = lround(xmin);
            it.ymin = lround(ymin);
        }
        //boundary check
        if (it.xmin < 0)it.xmin = 0;
        if (it.ymin < 0)it.ymin = 0;
        if (it.xmax > width)it.xmax = width - 1;
        if (it.ymax > height)it.ymax = height - 1;
        it.area = (it.xmax - it.xmin) * (it.ymax - it.ymin);
    }
}

MTCNN_GPU::MTCNN_GPU() {
    ncnn::create_gpu_instance();
}

MTCNN_GPU::~MTCNN_GPU() {
    ncnn::destroy_gpu_instance();
}

void MTCNN_GPU::setThreadNum(int threads) {
    num_threads = threads;
}

void MTCNN_GPU::setMinSize(int minSize) {
    min_size = minSize;
}

bool MTCNN_GPU::init(ncnn::Mat pnet_param, ncnn::Mat rnet_param, ncnn::Mat onet_param,
                     ncnn::Mat pnet_bin, ncnn::Mat rnet_bin, ncnn::Mat onet_bin) {
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = num_threads;
    if (ncnn::get_gpu_count() != 0)
        opt.use_vulkan_compute = true;
    pNet.opt = opt;
    rNet.opt = opt;
    oNet.opt = opt;
    int ret = pNet.load_param((const unsigned char *) pnet_param);
//    if (ret != 0) {
//        return false;
//    }
    ret = pNet.load_model((const unsigned char *) pnet_bin);
//    if (ret != 0) {
//        return false;
//    }
    ret = rNet.load_param((const unsigned char *) rnet_param);
//    if (ret != 0) {
//        return false;
//    }
    ret = rNet.load_model((const unsigned char *) rnet_bin);
//    if (ret != 0) {
//        return false;
//    }
    ret = oNet.load_param((const unsigned char *) onet_param);
//    if (ret != 0) {
//        return false;
//    }
    ret = oNet.load_model((const unsigned char *) onet_bin);
//    if (ret != 0) {
//        return false;
//    }
    isInit = true;
    return true;
}

bool MTCNN_GPU::init(const string &model_path) {
    std::vector<std::string> param_files = {
            model_path + "det1.param",
            model_path + "det2.param",
            model_path + "det3.param"
    };

    std::vector<std::string> bin_files = {
            model_path + "det1.bin",
            model_path + "det2.bin",
            model_path + "det3.bin"
    };
    int ret = pNet.load_param(param_files[0].data());
    if (ret != 0) {
        return false;
    }
    ret = pNet.load_model(bin_files[0].data());
    if (ret != 0) {
        return false;
    }
    ret = rNet.load_param(param_files[1].data());
    if (ret != 0) {
        return false;
    }
    ret = rNet.load_model(bin_files[1].data());
    if (ret != 0) {
        return false;
    }
    ret = oNet.load_param(param_files[2].data());
    if (ret != 0) {
        return false;
    }
    ret = oNet.load_model(bin_files[2].data());
    if (ret != 0) {
        return false;
    }
    isInit = true;
    return true;
}

vector<FaceInfo> MTCNN_GPU::detect(cv::Mat &img_) {
    Mat img = Mat::from_pixels(img_.data, Mat::PIXEL_BGR2RGB, img_.cols, img_.rows);
    int img_w = img.w;
    int img_h = img.h;
    img.substract_mean_normalize(mean_val, norm_val);
    PNet(img);
    if (pnetInfos.empty()) return pnetInfos;
    nms(pnetInfos, nms_threshold[0]);
    refine(pnetInfos, img_h, img_w, true);
    RNet(img);
    if (rnetInfos.empty())return rnetInfos;
    nms(rnetInfos, nms_threshold[1]);
    refine(rnetInfos, img_h, img_w, true);
    ONet(img);
    if (onetInfos.empty())return onetInfos;
    refine(onetInfos, img_h, img_w, true);
    nms(onetInfos, nms_threshold[2], "Min");
    return onetInfos;
}

void MTCNN_GPU::PNet(ncnn::Mat img) {
    pnetInfos.clear();
    int img_w = img.w;
    int img_h = img.h;
    float minl = img_w < img_h ? img_w : img_h;
    float m = (float) MIN_DET_SIZE / min_size;
    minl *= m;
    float factor = pre_facetor;
    std::vector<float> scales;
    while (minl > MIN_DET_SIZE) {
        scales.push_back(m);
        minl *= factor;
        m = m * factor;
    }
    for (float scale : scales) {
        int hs = (int) ceil(img_h * scale);
        int ws = (int) ceil(img_w * scale);
        ncnn::Mat in;
        resize_bilinear(img, in, ws, hs);
        ncnn::Extractor ex = pNet.create_extractor();
        if (ncnn::get_gpu_count() != 0) {
            ex.set_vulkan_compute(true);
        }
        ex.set_num_threads(num_threads);
        ex.set_light_mode(true);
        ex.input("data", in);
        ncnn::Mat score, location;
        ex.extract("prob1", score);
        ex.extract("conv4-2", location);
        std::vector<FaceInfo> boundingBox;
        generateBox(score, location, boundingBox, scale);
        nms(boundingBox, nms_threshold[0]);
        pnetInfos.insert(pnetInfos.end(), boundingBox.begin(), boundingBox.end());
        boundingBox.clear();
    }
}

void MTCNN_GPU::RNet(ncnn::Mat img) {
    rnetInfos.clear();
    int img_w = img.w;
    int img_h = img.h;
    for (auto &it : pnetInfos) {
        ncnn::Mat tempIm;
        copy_cut_border(img, tempIm, it.ymin, img_h - it.ymax, it.xmin, img_w - it.xmax);
        ncnn::Mat in;
        resize_bilinear(tempIm, in, 24, 24);
        ncnn::Extractor ex = rNet.create_extractor();
        if (ncnn::get_gpu_count() != 0) {
            ex.set_vulkan_compute(true);
        }
        ex.set_num_threads(num_threads);
        ex.set_light_mode(true);
        ex.input("data", in);
        ncnn::Mat score, bbox;
        ex.extract("prob1", score);
        ex.extract("conv5-2", bbox);
        if ((float) score[1] > threshold[1]) {
            for (int channel = 0; channel < 4; channel++) {
                it.bbox_reg[channel] = (float) bbox[channel];
            }
            it.area = (it.xmax - it.xmin) * (it.ymax - it.ymin);
            it.score = score.channel(1)[0];
            rnetInfos.push_back(it);
        }
    }
}

void MTCNN_GPU::ONet(ncnn::Mat img) {
    onetInfos.clear();
    int img_w = img.w;
    int img_h = img.h;
    for (auto &it : rnetInfos) {
        ncnn::Mat tempIm;
        copy_cut_border(img, tempIm, it.ymin, img_h - it.ymax, it.xmin, img_w - it.xmax);
        ncnn::Mat in;
        resize_bilinear(tempIm, in, 48, 48);
        ncnn::Extractor ex = oNet.create_extractor();
        if (ncnn::get_gpu_count() != 0) {
            ex.set_vulkan_compute(true);
        }
        ex.set_num_threads(num_threads);
        ex.set_light_mode(true);
        ex.input("data", in);
        ncnn::Mat score, bbox, keyPoint;
        ex.extract("prob1", score);
        ex.extract("conv6-2", bbox);
        ex.extract("conv6-3", keyPoint);
        if ((float) score[1] > threshold[2]) {
            for (int channel = 0; channel < 4; channel++) {
                it.bbox_reg[channel] = (float) bbox[channel];
            }
            it.area = (it.xmax - it.xmin) * (it.ymax - it.ymin);
            it.score = score.channel(1)[0];
            for (int num = 0; num < 5; num++) {
                (it.landmark)[num] = it.xmin + (it.xmax - it.xmin) * keyPoint[num];
                (it.landmark)[num + 5] = it.ymin + (it.ymax - it.ymin) * keyPoint[num + 5];
            }
            onetInfos.push_back(it);
        }
    }
}
