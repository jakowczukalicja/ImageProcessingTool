#pragma once
#include <fstream>
#include <string>

class FileLogger {

private:
    std::ofstream file;                
    static FileLogger* instance;       

    FileLogger() {
        file.open("processing_log.txt", std::ios::out); //fstream used
        file << "=== Logger started ===\n";
    }


    FileLogger(const FileLogger&) = delete; //copying is forbidden (singleton architecture)
    FileLogger& operator=(const FileLogger&) = delete;

public:

    FileLogger& getInstance() {
        if (instance == nullptr) {
            instance = new FileLogger();
        }
        return *instance;
    }


    void log(const std::string& msg) {
        file << msg << "\n";
    }

    FileLogger& operator<<(const std::string& msg) { //operator overloading
    this->log(msg);
    return *this;
    }



    ~FileLogger() {
        file << "=== Logger ended ===\n";
        file.close();
    }
};

