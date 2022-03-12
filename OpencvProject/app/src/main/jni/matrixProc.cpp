#include <jni.h>
#include <opencv2/opencv.hpp>
#include "opencv2/imgproc.hpp"
#include <unordered_set>
using namespace std;

extern "C"
void Java_opencvproject_com_JniImgProc_jniMatrix(JNIEnv *env, jobject obj,
                                                    jlong matAddr,
                                                    jint threshold,
                                                    jlong targetAddr)
{
    cv::Mat img = *(cv::Mat*)matAddr;

    int height = img.rows;
    int width = img.cols;

    cv::Mat img2;

    int cellHeight = 1, cellWidth = 1;
    while (width/cellWidth > 100 || height/cellHeight > 100){
        cellWidth *= 2;
        cellHeight *= 2;
    }


    cv::cvtColor(img, img2, CV_BGR2GRAY);
    Canny(img2, img2, 100, 100, 3, true);
    cv::resize(img2, img2, cv::Size((width / cellWidth), (height / cellHeight)));

    cv::Mat &newImg = *(cv::Mat*)targetAddr;
    string num = "0";
    int pixel;

    for (int i = 0; i < img2.cols; i+=1) {
        for (int j = 0; j < img2.rows; j+=1) {
            pixel = img2.at<uchar>(j, i); // here must be uchar because we make it gray

            int newi = i * cellWidth;
            int newj = j * cellHeight;

            if (pixel > threshold){
                cv::putText(newImg, num, cv::Point(newi, newj), cv::FONT_HERSHEY_COMPLEX_SMALL, 0.4, cv::Scalar(0, 255, 0));
                num = (num == "0"?"1":"0");
            }
        }
    }
}

extern "C"
void Java_opencvproject_com_JniImgProc_jniGray(JNIEnv *env, jobject obj,
                                                 jlong matAddr,
                                                 jdouble progress,
                                                 jlong targetAddr)
{
    cv::Mat img = *(cv::Mat*)matAddr;
    cv::Mat &targetImg = *(cv::Mat*)targetAddr;

    cv::cvtColor(img, targetImg, CV_BGR2GRAY);

    int now;
    for (int x = 0; x < img.cols; ++x) {
        for (int y = 0; y < img.rows; ++y) {
            now = targetImg.at<uchar>(y, x);
            now *= progress;
            if (now > 255) now = 255;
            else if (now < 0) now = 0;
            targetImg.at<uchar>(y, x) = now;
        }
    }
}

extern "C"
void Java_opencvproject_com_JniImgProc_jniBlur(JNIEnv *env, jobject obj,
                                               jlong matAddr,
                                               jint progress,
                                               jlong targetAddr)
{
    cv::Mat img = *(cv::Mat*)matAddr;
    cv::Mat &targetImg = *(cv::Mat*)targetAddr;

    cv::cvtColor(img, targetImg, cv::COLOR_BGRA2BGR);
    progress = progress/10+1;
    cv::blur(targetImg ,targetImg, cv::Size(progress,progress));
}

extern "C"
void Java_opencvproject_com_JniImgProc_jniBinarization(JNIEnv *env, jobject obj,
                                               jlong matAddr,
                                               jint progress,
                                               jlong targetAddr)
{
    cv::Mat img = *(cv::Mat*)matAddr;
    cv::Mat &targetImg = *(cv::Mat*)targetAddr;

    cv::cvtColor(img, targetImg, cv::COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
    cv::cvtColor(targetImg, targetImg, cv::COLOR_BGR2GRAY);//灰度化处理。

    double pro = progress*2.55;
    cv::threshold(targetImg, targetImg, pro,255, cv::THRESH_BINARY);
}

extern "C"
void Java_opencvproject_com_JniImgProc_jniCanny(JNIEnv *env, jobject obj,
                                               jlong matAddr,
                                               jint progress,
                                               jlong targetAddr)
{
    cv::Mat img = *(cv::Mat*)matAddr;
    cv::Mat &targetImg = *(cv::Mat*)targetAddr;

    cv::cvtColor(img, targetImg, cv::COLOR_BGR2GRAY);
    cv::Canny(targetImg, targetImg, progress*2,progress*2,3,true); // here got gray form
}