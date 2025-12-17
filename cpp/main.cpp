#include "Application.hpp"

int main(int argc, char* argv[]){
    try {
        Application app(argc, argv);
        return app.run();
    }
    catch (const std::string& err) {
        std::cerr << "Processing failed: " << err << "\n";
        return 1;
    }
}
