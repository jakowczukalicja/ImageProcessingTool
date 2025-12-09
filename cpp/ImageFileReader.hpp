#pragma once

#include <string>
#include <opencv2/opencv.hpp>


class ImageFileReader {
public:
    
    //Function that loads image
    static cv::Mat load(const std::string& path) {
        return cv::imread(path);
    }

    //Function that saves image at the given path
    static bool save(const std::string& path, const cv::Mat& img) {

        return cv::imwrite(path, img);
    }
};
