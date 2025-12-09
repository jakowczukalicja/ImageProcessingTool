#include <bits/stdc++.h>
#include <opencv2/imgcodecs.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>
#include "ImageFileReader.hpp"
#include "ImageFilter.hpp"
#include "ImageProcessor.hpp"


using namespace std;
using namespace cv;




int main(){

    string path = "Resources/lambo.png";

    Mat img = ImageFileReader::load(path);

    ImageProcessor im;

   
    im.addFilter("Rainbow");
    im.addFilter("Rainbow");
    im.process(img);


    ImageFileReader::save("output.jpg", img);



    return 0;
}
