#pragma once

#include <string>
#include <opencv2/opencv.hpp>


class ImageFileReader {
public:
    static cv::Mat load(const std::string& path);
    static void save(const std::string& path, const cv::Mat& img);
};
