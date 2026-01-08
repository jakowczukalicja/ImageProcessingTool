#include "ImageFilter.hpp"

// GrayFilter implementation
void GrayFilter::process(cv::Mat& image) {
    cv::cvtColor(image, image, cv::COLOR_BGR2GRAY);
    auto& log = FileLogger::getInstance();
    log << "GrayFilter completed";
}

// BlurFilter implementation
BlurFilter::BlurFilter(int kSize, float s) : kernelSize(kSize), sigma(s) {
    if (kernelSize % 2 == 0 || kernelSize <= 0) {
        throw std::string("Invalid kernel size for BlurFilter, make sure it's positive and odd.");
    }
    if (sigma <= 0) {
        throw std::string("Invalid sigma for BlurFilter, make sure it's positive.");
    }
}

void BlurFilter::process(cv::Mat& image) {
    cv::GaussianBlur(image, image, cv::Size(kernelSize, kernelSize), sigma, 0);
    auto& log = FileLogger::getInstance();
    log << "BlurFilter completed";
}

// EdgeFilter implementation
EdgeFilter::EdgeFilter(int t1, int t2) : threshold1(t1), threshold2(t2) {
    if (threshold1 < 0 || threshold2 < 0) {
        throw std::string("Thresholds for EdgeFilter must be non-negative.");
    }
    if (threshold1 >= threshold2) {
        throw std::string("Threshold1 must be less than threshold2 for EdgeFilter.");
    }
}

void EdgeFilter::process(cv::Mat& image) {
    cv::Mat imageBlur;
    cv::GaussianBlur(image, imageBlur, cv::Size(3, 3), 5, 0);
    cv::Canny(imageBlur, image, threshold1, threshold2);
    auto& log = FileLogger::getInstance();
    log << "CannyFilter completed";
}

// RoseBlushFilter implementation
void RoseBlushFilter::process(cv::Mat& image) {
    cv::Mat img;
    image.convertTo(img, CV_32F, 1.0 / 255.0);

    for (int y = 0; y < img.rows; y++) {
        for (int x = 0; x < img.cols; x++) {
            cv::Vec3f& pixel = img.at<cv::Vec3f>(y, x);
            float B = pixel[0];
            float G = pixel[1];
            float R = pixel[2];

            R = std::min(1.0f, R * 1.4f + 0.1f);
            B = std::min(1.0f, B * 1.2f + 0.05f);
            G = std::max(0.0f, G * 0.7f - 0.05f);

            pixel[0] = B;
            pixel[1] = G;
            pixel[2] = R;
        }
    }

    cv::Mat blur;
    cv::GaussianBlur(img, blur, cv::Size(25, 25), 8);
    cv::addWeighted(img, 0.85, blur, 0.35, 0, img);

    img.convertTo(image, CV_8UC3, 255.0);
    auto& log = FileLogger::getInstance();
    log << "RoseBlushFilter completed";
}

// SingleColourFilter implementation
SingleColourFilter::SingleColourFilter(int red, int green, int blue) {
    if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
        throw std::string("RGB values must be in range 0-255 for SingleColourFilter.");
    }

    r = red / 255.0f;
    g = green / 255.0f;
    b = blue / 255.0f;
}

void SingleColourFilter::process(cv::Mat& image) {
    cv::Mat img;
    image.convertTo(img, CV_32F, 1.0 / 255.0);

    for (int y = 0; y < img.rows; y++) {
        for (int x = 0; x < img.cols; x++) {
            cv::Vec3f& pixel = img.at<cv::Vec3f>(y, x);
            float B = pixel[0];
            float G = pixel[1];
            float R = pixel[2];

            float brightness = 0.299 * R + 0.587 * G + 0.114 * B;

            G = std::min(1.0f, brightness * g);
            R = std::min(1.0f, brightness * r);
            B = std::min(1.0f, brightness * b);

            pixel[0] = B;
            pixel[1] = G;
            pixel[2] = R;
        }
    }

    cv::Mat blur;
    cv::GaussianBlur(img, blur, cv::Size(25, 25), 8);
    cv::addWeighted(img, 0.85, blur, 0.35, 0, img);

    img.convertTo(image, CV_8UC3, 255.0);
    auto& log = FileLogger::getInstance();
    log << "SingleColourFilter completed";
}

// RainbowFilter implementation
float RainbowFilter::weight(int middle, int current, int all) {
    float dis = abs(middle - current);
    return std::pow(all - dis, 3);
}

std::vector<float> RainbowFilter::rainbowColours(int num) {
    switch (num) {
        case 0: return {1, 0, 0};        // red
        case 1: return {1, 0.5, 0};      // orange
        case 2: return {1, 1, 0};        // yellow
        case 3: return {0, 1, 0};        // green
        case 4: return {0, 0, 1};        // blue
        case 5: return {1, 0, 1};        // magenta
    }
    return {0, 0, 0};
}

std::vector<float> RainbowFilter::colour(int all, int current, std::vector<int> middles) {
    float weight_sum = 0;
    std::vector<float> colour = {0, 0, 0};

    for (int i = 0; i <= 5; i++) {
        std::vector<float> rainbow = rainbowColours(i);
        float w = weight(middles[i], current, all);
        weight_sum += w;

        colour[0] += w * rainbow[0];
        colour[1] += w * rainbow[1];
        colour[2] += w * rainbow[2];
    }

    colour[0] /= weight_sum;
    colour[1] /= weight_sum;
    colour[2] /= weight_sum;

    return colour;
}

RainbowFilter::RainbowFilter() {
    throw std::string("RainbowFilter requires a character argument 'r' or 'c'.");
}

RainbowFilter::RainbowFilter(char a) : k(a) {
    if (k != 'r' && k != 'c') {
        throw std::string("RainbowFilter argument must be 'r' or 'c'.");
    }
}

void RainbowFilter::process(cv::Mat& image) {
    cv::Mat img;
    image.convertTo(img, CV_32F, 1.0 / 255.0);

    int numOfPixels;
    if (k == 'r') {
        numOfPixels = img.rows;
    } else {
        numOfPixels = img.cols;
    }
    
    std::vector<int> middles;
    int lengthOfInterval = numOfPixels / 5;
    for (int i = 0; i <= 5; i++) {
        middles.push_back(lengthOfInterval * i);
    }

    for (int y = 0; y < img.rows; y++) {
        for (int x = 0; x < img.cols; x++) {
            cv::Vec3f& pixel = img.at<cv::Vec3f>(y, x);

            std::vector<float> colourOfPixel;

            if (k == 'r') {
                std::vector<float> other = colour(numOfPixels, y, middles);
                colourOfPixel.insert(colourOfPixel.end(), other.begin(), other.end());
            } else {
                std::vector<float> other = colour(numOfPixels, x, middles);
                colourOfPixel.insert(colourOfPixel.end(), other.begin(), other.end());
            }

            float B = pixel[0];
            float G = pixel[1];
            float R = pixel[2];

            float brightness = 0.299 * R + 0.587 * G + 0.114 * B;
            brightness *= 1.2;

            R = std::min(1.0f, brightness * colourOfPixel[0]);
            G = std::min(1.0f, brightness * colourOfPixel[1]);
            B = std::min(1.0f, brightness * colourOfPixel[2]);

            pixel[0] = B;
            pixel[1] = G;
            pixel[2] = R;
        }
    }

    img.convertTo(image, CV_8UC3, 255.0);
    auto& log = FileLogger::getInstance();
    log << "RainbowFilter completed";
}

// HeartFilter implementation
std::vector<std::vector<int>> HeartFilter::heart(int width, int height) {
    std::vector<std::vector<int>> grid(height, std::vector<int>(width, 0));

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            float nx = ((2.0f * x / width) - 1.0f) * (-1.5f);
            float ny = ((2.0f * y / height) - 1.0f) * (-1.5f) + 0.3f;

            float a = 5.0f / 4.0f * ny - std::sqrt(std::abs(nx));
            float value = nx * nx + a * a - 1.0f;

            if (value <= 0.01f && value >= -0.01f) {
                grid[y][x] = 3;
            } else if (value <= 0.03f && value >= -0.03f) {
                grid[y][x] = 2;
            } else if (value <= 0.1f && value >= -0.1f) {
                grid[y][x] = 1;
            }
        }
    }

    return grid;
}

std::vector<float> HeartFilter::heartColours(int num) {
    switch (num) {
        case 3: return {1, 1, 1};
        case 2: return {0.98, 0.345, 0.525};
        case 1: return {0.969, 0.345, 0.71};
        case 0: return {0.49, 0.337, 0.749};
    }
    return {0, 0, 0};
}

void HeartFilter::process(cv::Mat& image) {
    cv::Mat img;
    image.convertTo(img, CV_32F, 1.0 / 255.0);

    std::vector<std::vector<int>> grid = heart(img.cols, img.rows);

    for (int y = 0; y < img.rows; y++) {
        for (int x = 0; x < img.cols; x++) {
            cv::Vec3f& pixel = img.at<cv::Vec3f>(y, x);

            float B = pixel[0];
            float G = pixel[1];
            float R = pixel[2];

            float brightness = 0.299 * R + 0.587 * G + 0.114 * B;

            int num = grid[y][x];

            if (num == 3) {
                brightness *= 5;
            } else if (num == 2) {
                brightness *= 4;
            } else if (num == 1) {
                brightness *= 0.9;
            } else if (num == 0) {
                brightness *= 0.8;
            }

            std::vector<float> colour = heartColours(num);

            if (num == 3 || num == 2) {
                R = std::min(1.0f, brightness * colour[0]);
                G = std::min(1.0f, brightness * colour[1]);
                B = std::min(1.0f, brightness * colour[2]);
            } else if (num == 1) {
                R = std::min(1.0f, brightness * colour[0] + R * 0.1f);
                G = std::min(1.0f, brightness * colour[1] + G * 0.1f);
                B = std::min(1.0f, brightness * colour[2] + B * 0.1f);
            } else {
                R = std::min(1.0f, brightness * colour[0] + R * 0.2f);
                G = std::min(1.0f, brightness * colour[1] + G * 0.2f);
                B = std::min(1.0f, brightness * colour[2] + B * 0.2f);
            }

            pixel[0] = B;
            pixel[1] = G;
            pixel[2] = R;
        }
    }

    cv::Mat blur;
    cv::GaussianBlur(img, blur, cv::Size(45, 45), 8);
    cv::addWeighted(img, 0.85, blur, 0.35, 0, img);

    img.convertTo(image, CV_8UC3, 255.0);

    auto& log = FileLogger::getInstance();
    log << "HeartFilter completed";
}
