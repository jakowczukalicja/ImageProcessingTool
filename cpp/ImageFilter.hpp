#pragma once

#include <string>
#include <vector>
#include <algorithm>
#include <cmath>
#include <cstdlib>
#include <opencv2/opencv.hpp>
#include "FileLogger.hpp"

//abstract class
class ImageFilter {
public:
    virtual ~ImageFilter() = default;
    virtual void process(cv::Mat& image) = 0;
};

//inheritance
class GrayFilter : public ImageFilter {
public:
    void process(cv::Mat& image) override;
};

class BlurFilter : public ImageFilter {
private:
    int kernelSize;
    float sigma;

public:
    BlurFilter(int kSize = 21, float s = 5);
    void process(cv::Mat& image) override;
};

class EdgeFilter : public ImageFilter {
private:
    int threshold1;
    int threshold2;

public:
    EdgeFilter(int t1 = 50, int t2 = 150);
    void process(cv::Mat& image) override;
};

class RoseBlushFilter : public ImageFilter {
public:
    void process(cv::Mat& image) override;
};

class SingleColourFilter : public ImageFilter {
private:
    float r;
    float g;
    float b;

public:
    SingleColourFilter(int red = -1, int green = -1, int blue = -1);
    void process(cv::Mat& image) override;
};

class RainbowFilter : public ImageFilter {
private:
    char k;

    float weight(int middle, int current, int all);
    std::vector<float> rainbowColours(int num);
    std::vector<float> colour(int all, int current, std::vector<int> middles);

public:
    RainbowFilter();
    RainbowFilter(char a);
    void process(cv::Mat& image) override;
};

class HeartFilter : public ImageFilter {
private:
    std::vector<std::vector<int>> heart(int width, int height);
    std::vector<float> heartColours(int num);

public:
    void process(cv::Mat& image) override;
};