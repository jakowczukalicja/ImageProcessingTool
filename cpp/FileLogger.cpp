#include "FileLogger.hpp"

FileLogger::FileLogger() {
    file.open("processing_log.txt", std::ios::out);
    file << "=== Logger started ===\n";
}

FileLogger& FileLogger::getInstance() {
    static FileLogger instance; 
    return instance;
}

void FileLogger::log(const std::string& msg) {
    file << msg << "\n";
    file.flush();
}

FileLogger& FileLogger::operator<<(const std::string& msg) {
    log(msg);
    return *this;
}

FileLogger::~FileLogger() {
    file << "=== Logger ended ===\n";
    file.close();
}
