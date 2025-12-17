#pragma once
#include <vector>
#include <string>
#include <memory>
#include <stdexcept>
#include "ImageFilter.hpp"
#include "FilterType.hpp"

class ImageProcessor {

private:
    std::vector<std::unique_ptr<ImageFilter>> filters; //Polymorphism and smart pointers


    std::unique_ptr<ImageFilter> createFilter(FilterType name) {

        switch (name){
            case FilterType::Gray: return std::make_unique<GrayFilter>();
            case FilterType::Blur: return std::make_unique<BlurFilter>();
            case FilterType::Edge: return std::make_unique<EdgeFilter>();
            case FilterType::RoseBlush: return std::make_unique<RoseBlushFilter>();
            case FilterType::Pink: return std::make_unique<PinkFilter>();
            case FilterType::Rainbowr: return std::make_unique<RainbowFilter>('r');
            case FilterType::Rainbowc: return std::make_unique<RainbowFilter>('c');
            case FilterType::Heart: return std::make_unique<HeartFilter>();
        }

        throw std::runtime_error("Unknown filter ");
    }

public:
    
    ImageProcessor() = default;

    ImageProcessor(FilterType initialFilter) //constructor (non-empty)
    {
        addFilter(initialFilter);
    
    }

    ImageProcessor(const std::vector<FilterType> initialFilters)
    {
    for (auto type : initialFilters) {
        addFilter(type);
    }

    }

    void addFilter(FilterType name) {
        
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
