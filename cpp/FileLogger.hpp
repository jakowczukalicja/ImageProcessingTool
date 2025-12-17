#pragma once
#include <fstream>
#include <string>

class FileLogger {
private:
    std::ofstream file;

    FileLogger() {
        file.open("processing_log.txt", std::ios::out);
        file << "=== Logger started ===\n";
    }

    FileLogger(const FileLogger&) = delete;
    FileLogger& operator=(const FileLogger&) = delete;

public:
    static FileLogger& getInstance() {
        static FileLogger instance; 
        return instance;
    }

    void log(const std::string& msg) {
        file << msg << "\n";
        file.flush();
    }

    FileLogger& operator<<(const std::string& msg) { //operator overloading
        log(msg);
        return *this;
    }

    ~FileLogger() {
        file << "=== Logger ended ===\n";
        file.close();
    }
};


