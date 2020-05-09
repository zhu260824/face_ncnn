//
// Created by 朱林 on 2020/5/8.
//

#define clip(x, y) (x < 0 ? 0 : (x > y ? y : x))

#include "ultra_face.h"
#include "ncnn/mat.h"

UltraFace::UltraFace() {
}

UltraFace::~UltraFace() {
    ultraNet.clear();
    if (run_gpu) {
        ncnn::destroy_gpu_instance();
    }
}

void UltraFace::setThreadNum(int threads) {
    num_thread = threads;
}

void UltraFace::setScoreThreshold(int score) {
    score_threshold = score;
}

void UltraFace::setRunGpu(bool run) {
    run_gpu = run;
}

void UltraFace::setReSize(int w, int h) {
    resize_w = w;
    resize_h = h;
    std::vector<int> w_h_list = {resize_w, resize_h};
    std::vector<std::vector<float>> featuremap_size;
    std::vector<std::vector<float>> shrinkage_size;
    for (auto size : w_h_list) {
        std::vector<float> fm_item;
        for (float stride : strides) {
            fm_item.push_back(ceil(size / stride));
        }
        featuremap_size.push_back(fm_item);
    }
    for (auto size : w_h_list) {
        shrinkage_size.push_back(strides);
    }
    /* generate prior anchors */
    for (int index = 0; index < num_featuremap; index++) {
        float scale_w = resize_w / shrinkage_size[0][index];
        float scale_h = resize_h / shrinkage_size[1][index];
        for (int j = 0; j < featuremap_size[1][index]; j++) {
            for (int i = 0; i < featuremap_size[0][index]; i++) {
                float x_center = (i + 0.5) / scale_w;
                float y_center = (j + 0.5) / scale_h;
                for (float k : min_boxes[index]) {
                    float w = k / resize_w;
                    float h = k / resize_h;
                    priors.push_back(
                            {clip(x_center, 1), clip(y_center, 1), clip(w, 1), clip(h, 1)});
                }
            }
        }
    }
    num_anchors = priors.size();
}

bool UltraFace::init(const std::string &bin_path, const std::string &param_path, int resize_w,
                     int resize_h, bool run_gpu) {
    setReSize(resize_w, resize_h);
    setRunGpu(run_gpu);
    if (run_gpu) {
        ncnn::create_gpu_instance();
    }
    ncnn::Option opt;
    opt.lightmode = true;
    opt.num_threads = num_thread;
    if (run_gpu && ncnn::get_gpu_count() != 0) {
        opt.use_vulkan_compute = true;
    }
    ultraNet.opt = opt;
    int ret = ultraNet.load_param(param_path.data());
    if (ret != 0) {
        return false;
    }
    ret = ultraNet.load_model(bin_path.data());
    if (ret != 0) {
        return false;
    }
    return true;
}

std::vector<FaceInfo> UltraFace::detect(ncnn::Mat &img) {
    std::vector<FaceInfo> face_list = {};
    if (img.empty()) {
        std::cout << "image is empty ,please check!" << std::endl;
        return face_list;
    }
    int image_h = img.h;
    int image_w = img.w;
    ncnn::Mat in;
    ncnn::resize_bilinear(img, in, resize_w, resize_h);
    ncnn::Mat ncnn_img = in;
    ncnn_img.substract_mean_normalize(mean_vals, norm_vals);
    std::vector<FaceInfo> bbox_collection;
    std::vector<FaceInfo> valid_input;
    ncnn::Extractor ex = ultraNet.create_extractor();
    if (run_gpu && ncnn::get_gpu_count() != 0) {
        ex.set_vulkan_compute(true);
    }
    ex.set_light_mode(true);
    ex.set_num_threads(num_thread);
    ex.input("input", ncnn_img);
    ncnn::Mat scores;
    ncnn::Mat boxes;
    ex.extract("scores", scores);
    ex.extract("boxes", boxes);
    generateBBox(bbox_collection, scores, boxes, score_threshold, num_anchors, image_w, image_h);
    nms(bbox_collection, face_list);
    return face_list;
}

void UltraFace::generateBBox(std::vector<FaceInfo> &bbox_collection, ncnn::Mat scores,
                             ncnn::Mat boxes, float score_threshold, int num_anchors, int image_w,
                             int image_h) {
    for (int i = 0; i < num_anchors; i++) {
        if (scores.channel(0)[i * 2 + 1] > score_threshold) {
            FaceInfo rects;
            float x_center =
                    boxes.channel(0)[i * 4] * center_variance * priors[i][2] + priors[i][0];
            float y_center =
                    boxes.channel(0)[i * 4 + 1] * center_variance * priors[i][3] + priors[i][1];
            float w = exp(boxes.channel(0)[i * 4 + 2] * size_variance) * priors[i][2];
            float h = exp(boxes.channel(0)[i * 4 + 3] * size_variance) * priors[i][3];
            rects.x1 = clip(x_center - w / 2.0, 1) * image_w;
            rects.y1 = clip(y_center - h / 2.0, 1) * image_h;
            rects.x2 = clip(x_center + w / 2.0, 1) * image_w;
            rects.y2 = clip(y_center + h / 2.0, 1) * image_h;
            rects.score = clip(scores.channel(0)[i * 2 + 1], 1);
            bbox_collection.push_back(rects);
        }
    }
}

void UltraFace::nms(std::vector<FaceInfo> &input, std::vector<FaceInfo> &output, int type) {
    std::sort(input.begin(), input.end(),
              [](const FaceInfo &a, const FaceInfo &b) { return a.score > b.score; });
    int box_num = input.size();
    std::vector<int> merged(box_num, 0);
    for (int i = 0; i < box_num; i++) {
        if (merged[i])
            continue;
        std::vector<FaceInfo> buf;
        buf.push_back(input[i]);
        merged[i] = 1;
        float h0 = input[i].y2 - input[i].y1 + 1;
        float w0 = input[i].x2 - input[i].x1 + 1;
        float area0 = h0 * w0;
        for (int j = i + 1; j < box_num; j++) {
            if (merged[j])
                continue;
            float inner_x0 = input[i].x1 > input[j].x1 ? input[i].x1 : input[j].x1;
            float inner_y0 = input[i].y1 > input[j].y1 ? input[i].y1 : input[j].y1;
            float inner_x1 = input[i].x2 < input[j].x2 ? input[i].x2 : input[j].x2;
            float inner_y1 = input[i].y2 < input[j].y2 ? input[i].y2 : input[j].y2;
            float inner_h = inner_y1 - inner_y0 + 1;
            float inner_w = inner_x1 - inner_x0 + 1;
            if (inner_h <= 0 || inner_w <= 0)
                continue;
            float inner_area = inner_h * inner_w;
            float h1 = input[j].y2 - input[j].y1 + 1;
            float w1 = input[j].x2 - input[j].x1 + 1;
            float area1 = h1 * w1;
            float score;
            score = inner_area / (area0 + area1 - inner_area);
            if (score > iou_threshold) {
                merged[j] = 1;
                buf.push_back(input[j]);
            }
        }
        switch (type) {
            case hard_nms: {
                output.push_back(buf[0]);
                break;
            }
            case blending_nms: {
                float total = 0;
                for (int i = 0; i < buf.size(); i++) {
                    total += exp(buf[i].score);
                }
                FaceInfo rects;
                memset(&rects, 0, sizeof(rects));
                for (int i = 0; i < buf.size(); i++) {
                    float rate = exp(buf[i].score) / total;
                    rects.x1 += buf[i].x1 * rate;
                    rects.y1 += buf[i].y1 * rate;
                    rects.x2 += buf[i].x2 * rate;
                    rects.y2 += buf[i].y2 * rate;
                    rects.score += buf[i].score * rate;
                }
                output.push_back(rects);
                break;
            }
            default: {
                printf("wrong type of nms.");
                exit(-1);
            }
        }
    }
}