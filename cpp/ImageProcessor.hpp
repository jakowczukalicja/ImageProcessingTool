#pragma once
#include <vector>
#include <string>
#include <memory>
#include <stdexcept>
#include "ImageFilter.hpp"

class ImageProcessor {

private:
    std::vector<std::string> filterNames;
    std::vector<std::unique_ptr<ImageFilter>> filters; //Polymorphism and smart pointers


    std::unique_ptr<ImageFilter> createFilter(const std::string& name) {
        if (name == "Gray") return std::make_unique<GrayFilter>();
        if (name == "Blur") return std::make_unique<BlurFilter>();
        if (name == "Edge") return std::make_unique<EdgeFilter>();
        if (name == "RoseBlush") return std::make_unique<RoseBlushFilter>();
        if (name == "Pink") return std::make_unique<PinkFilter>();
        if (name == "Rainbow") return std::make_unique<RainbowFilter>('r');
        if (name == "Heart") return std::make_unique<HeartFilter>();

        throw std::runtime_error("Unknown filter: " + name);
    }

public:
    ImageProcessor() = default;

    void addFilter(const std::string& name) {
        filterNames.push_back(name);
        filters.push_back(createFilter(name));
    }


    void process(cv::Mat& img) {
        
        for (auto& f : filters) {

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
        }
    }
};
