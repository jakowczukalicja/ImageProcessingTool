#include "ImageProcessor.hpp"

std::unique_ptr<ImageFilter> ImageProcessor::createFilter(FilterType name, const std::vector<std::string>& params) {
    switch (name){
        case FilterType::Gray: 
            return std::make_unique<GrayFilter>();
        case FilterType::Blur: {
            int kSize = params.size() > 0 ? std::stoi(params[0]) : 9;
            float sigma = params.size() > 1 ? std::stof(params[1]) : 13.0f;
            return std::make_unique<BlurFilter>(kSize, sigma);
        }
        case FilterType::Edge: {
            int t1 = params.size() > 0 ? std::stoi(params[0]) : 80;
            int t2 = params.size() > 1 ? std::stoi(params[1]) : 300;
            return std::make_unique<EdgeFilter>(t1, t2);
        }
        case FilterType::RoseBlush: 
            return std::make_unique<RoseBlushFilter>();
        case FilterType::SingleColourFilter: {
            int r = params.size() > 0 ? std::stoi(params[0]) : 255;
            int g = params.size() > 1 ? std::stoi(params[1]) : 192;
            int b = params.size() > 2 ? std::stoi(params[2]) : 203;
            return std::make_unique<SingleColourFilter>(r, g, b);
        }
        case FilterType::Rainbow: {
            char dir = params.size() > 0 ? params[0][0] : 'r';
            return std::make_unique<RainbowFilter>(dir);
        }
        case FilterType::Heart: 
            return std::make_unique<HeartFilter>();
    }
    throw std::string("Unknown filter type");
}

ImageProcessor::ImageProcessor(FilterType initialFilter) {
    addFilter(initialFilter);
}

ImageProcessor::ImageProcessor(const std::vector<FilterType> initialFilters) {
    for (auto type : initialFilters) {
        addFilter(type);
    }
}

void ImageProcessor::addFilter(FilterType name, const std::vector<std::string>& params) {
    try {
        filters.push_back(createFilter(name, params));
    } catch (const std::string& e) {
        throw;
    }
}

void ImageProcessor::process(cv::Mat& img) {
    try {
        for (auto& f : filters) {
            if (img.empty()) {
                FileLogger::getInstance() << "ERROR: process() received empty image";
                throw std::string("Cannot process empty image");
            }

            if (img.channels() == 1) {
                cv::cvtColor(img, img, cv::COLOR_GRAY2BGR);
            }
            if (img.type() != CV_8UC3) {
                img.convertTo(img, CV_8UC3, 255.0);
            }

            f->process(img);

            if (img.channels() == 1) {
                cv::cvtColor(img, img, cv::COLOR_GRAY2BGR);
            }
            if (img.type() != CV_8UC3) {
                img.convertTo(img, CV_8UC3);
            }

            if (img.empty()) {
                throw std::string("Filter produced empty image");
            }
        }
    } catch (const std::string& e) {
        FileLogger::getInstance() << "ERROR: Image processing failed - " + e;
        throw;
    }
}
