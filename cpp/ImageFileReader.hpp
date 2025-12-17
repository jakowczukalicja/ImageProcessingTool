#pragma once

#include <string>
#include <opencv2/opencv.hpp>


class ImageFileReader {
public:
    
    //Function that loads image
    static cv::Mat load(const std::string& path) {
        cv::Mat img = cv::imread(path);

         if (img.empty()) {
            throw std::string("Cannot load image: " + path);
        }

        return img;
    }

    //Function that saves image at the given path
    static void save(const std::string& path, const cv::Mat& img) {
        if (img.empty()) {
            throw std::string("Image is empty");
        }
        
        if (!cv::imwrite(path, img)) {
            throw std::string("Cannot save image at path: " + path);
        }

    }
};
