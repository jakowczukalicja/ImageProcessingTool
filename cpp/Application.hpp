#pragma once
#include <vector>
#include <string>

#include "ImageFilter.hpp"
#include "FilterType.hpp"
#include "ImageFileReader.hpp"
#include "ImageProcessor.hpp"
#include "FileLogger.hpp"

class Application {

private:

    std::string inputPath;
    std::string outputPath;
    std::vector<FilterType> filters;

    void toLowercase(std::string& s){
        std::transform(s.begin(), s.end(), s.begin(), 
        [](char c) { return std::tolower(c); } ); //STL
    }

    FilterType parseFilterType(const std::string& s0) {
        std::string s=s0;

        toLowercase(s);

        if (s == "gray")      return FilterType::Gray;
        if (s == "blur")      return FilterType::Blur;
        if (s == "edge")      return FilterType::Edge;
        if (s == "rose")      return FilterType::RoseBlush;
        if (s == "pink")      return FilterType::Pink;
        if (s == "rainbowr")  return FilterType::Rainbowr;
        if (s == "rainbowc")  return FilterType::Rainbowc;
        if (s == "heart")     return FilterType::Heart;

        FileLogger::getInstance() << "ERROR: invalid filter type: " + s;
        throw std::string("Invalid filter type: " + s);
    }
    

public:

    Application(int argc, char* argv[]){
        auto& log = FileLogger::getInstance();

        if (argc < 4) {
            log << "ERROR: invalid arguments count";
            throw std::string("Invalid arguments count, Usage: app <input> <output> <filters...>");
        }

        inputPath = argv[1];
        outputPath  = argv[2];

        log << "Input path: " + inputPath;
        log << "Output path: " + outputPath;

        for (int i = 3; i < argc; i++) {
            filters.push_back(parseFilterType(argv[i]));
        }
    }


    int run(){

        auto& log = FileLogger::getInstance();

        try {
            log << "Loading image";
            cv::Mat img = ImageFileReader::load(inputPath);
            log << "Image loaded";

            log << "Applying filters";
            ImageProcessor im(filters);
            im.process(img);
            log <<"Filters applied";

            log << "Saving image";
            ImageFileReader::save(outputPath, img);
            log << "Image saved";

            log << "Processing completed successfully";
            return 0;
        }
        catch (const std::string& err) {
            log <<"ERROR: "<<  err;
            throw; 
        }
    }
};
