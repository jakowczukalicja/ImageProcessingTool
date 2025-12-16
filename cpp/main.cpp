#include <bits/stdc++.h>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include "ImageFileReader.hpp"
#include "ImageFilter.hpp"
#include "ImageProcessor.hpp"
#include "FilterType.hpp"


using namespace std;
using namespace cv;



int main(){

    string path = "Resources/lambo.png";

    Mat img = ImageFileReader::load(path);

    std::vector<FilterType> k = {FilterType::Rainbowr, FilterType::Heart};
    ImageProcessor im(k);

   
    
    im.process(img);


    ImageFileReader::save("output.jpg", img);



    return 0;
}
