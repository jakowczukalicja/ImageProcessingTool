#pragma once
#include <vector>
#include <string>
#include <memory>
#include "ImageFilter.hpp"
#include "FilterType.hpp"
#include "FileLogger.hpp"

class ImageProcessor {

private:
    std::vector<std::unique_ptr<ImageFilter>> filters;
    std::unique_ptr<ImageFilter> createFilter(FilterType name, const std::vector<std::string>& params = {});

public:
    ImageProcessor() = default;
    ImageProcessor(FilterType initialFilter);
    ImageProcessor(const std::vector<FilterType> initialFilters);
    
    void addFilter(FilterType name, const std::vector<std::string>& params = {});
    void process(cv::Mat& img);
};
