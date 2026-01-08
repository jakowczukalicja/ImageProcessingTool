#include "ImageFileReader.hpp"

cv::Mat ImageFileReader::load(const std::string& path) {
    cv::Mat img = cv::imread(path);

    if (img.empty()) {
        throw std::string("Cannot load image: " + path);
    }

    return img;
}

void ImageFileReader::save(const std::string& path, const cv::Mat& img) {
    if (img.empty()) {
        throw std::string("Image is empty");
    }
    
    if (!cv::imwrite(path, img)) {
        throw std::string("Cannot save image at path: " + path);
    }
}
