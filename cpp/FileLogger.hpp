#pragma once
#include <fstream>
#include <string>

class FileLogger {
private:
    std::ofstream file;

    FileLogger();
    FileLogger(const FileLogger&) = delete;
    FileLogger& operator=(const FileLogger&) = delete;

public:
    static FileLogger& getInstance();
    void log(const std::string& msg);
    FileLogger& operator<<(const std::string& msg);
    ~FileLogger();
};


