#include "Application.hpp"
#include "ImageFileReader.hpp"
#include "ImageProcessor.hpp"
#include "FileLogger.hpp"
#include <algorithm>

void Application::toLowercase(std::string& s){
    std::transform(s.begin(), s.end(), s.begin(), 
    [](char c) { return std::tolower(c); } ); //STL
}

FilterType Application::parseFilterType(const std::string& s0) {
    std::string s=s0;

    toLowercase(s);

    if (s == "gray")      return FilterType::Gray;
    if (s == "blur")      return FilterType::Blur;
    if (s == "edge")      return FilterType::Edge;
    if (s == "rose")      return FilterType::RoseBlush;
    if (s == "singlecolour")      return FilterType::SingleColourFilter;
    if (s == "rainbow")  return FilterType::Rainbow;
    if (s == "heart")     return FilterType::Heart;

    FileLogger::getInstance() << "ERROR: invalid filter type: " + s;
    throw std::string("Invalid filter type: " + s);
}

int Application::getParamCount(FilterType type) {
    switch (type) {
        case FilterType::Gray: return 0;
        case FilterType::Blur: return 2;
        case FilterType::Edge: return 2;
        case FilterType::RoseBlush: return 0;
        case FilterType::SingleColourFilter: return 3;
        case FilterType::Rainbow: return 1;
        case FilterType::Heart: return 0;
    }
    return 0;
}

Application::Application(int argc, char* argv[]){
    auto& log = FileLogger::getInstance();

    if (argc < 4) {
        log << "ERROR: invalid arguments count";
        throw std::string("Invalid arguments count, Usage: app <input> <output> <filters...>");
    }

    inputPath = argv[1];
    outputPath  = argv[2];

    log << "Input path: " + inputPath;
    log << "Output path: " + outputPath;

    for (int i = 3; i < argc; ) {
        std::string arg = argv[i];
        if (arg.length() >= 2 && arg.substr(0, 2) == "--") {
            FilterType type = parseFilterType(arg.substr(2));
            int paramCount = getParamCount(type);
            
            FilterWithParams fwp;
            fwp.type = type;
            
            if (i + paramCount >= argc) {
                log << "ERROR: not enough parameters for filter " + arg;
                throw std::string("Not enough parameters for filter " + arg);
            }
            
            for (int j = 0; j < paramCount; j++) {
                fwp.params.push_back(argv[i + 1 + j]);
            }
            
            filters.push_back(fwp);
            i += paramCount + 1;
        } else {
            i++;
        }
    }
}

int Application::run(){

    auto& log = FileLogger::getInstance();

    try {
        log << "Loading image";
        cv::Mat img = ImageFileReader::load(inputPath);
        log << "Image loaded";

        log << "Applying filters";
        ImageProcessor im;
        for (const auto& fwp : filters) {
            im.addFilter(fwp.type, fwp.params);
        }
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
