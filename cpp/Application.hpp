#pragma once
#include <vector>
#include <string>

#include "FilterType.hpp"

struct FilterWithParams {
    FilterType type;
    std::vector<std::string> params;
};

class Application {

private:

    std::string inputPath;
    std::string outputPath;
    std::vector<FilterWithParams> filters;

    void toLowercase(std::string& s);
    FilterType parseFilterType(const std::string& s0);
    int getParamCount(FilterType type);

public:

    Application(int argc, char* argv[]);
    int run();
};
