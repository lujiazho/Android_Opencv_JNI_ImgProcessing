package opencvproject.com.ImgProcCamera;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImgProcessor {

    private static final String TAG = "ObjectDetector";

    // 1: canny, 2: histogram
    int proc_order;

    // path
    String protoPath;
    String caffeWeights;

    /**
     * 构造方法
     *
     * @param context              上下
     * @param order                处理方法
     */
    public ImgProcessor(Context context, int order) {
        context = context.getApplicationContext();
        proc_order = order;
    }

    /**
     * 目标检测
     *
     * @param frame   单帧图像
     * @return 检测到的目标位置集合
     */
    public Mat process(Mat frame) {
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        switch (proc_order){
            case 1:
                frame = Canny(frame);
                break;
            case 2:
                frame = histogramProcess(frame);
                break;
            default:
                break;
        }

        return frame;
    }

    public Mat Canny(Mat frame){
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(frame, frame, 100, 100);
        return frame;
    }

    private Mat histogramProcess(Mat frame) {
        Mat dst = new Mat();
        //进行直方图绘制
        return displayHistogram(frame, dst);
    }

    private Mat displayHistogram(Mat src, Mat dst){
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);//转换为灰度图

        //计算直方图数据并归一化
        List<Mat> images=new ArrayList<>();
        images.add(gray);
        Mat mask=Mat.ones(src.size(),CvType.CV_8UC1);
        Mat hist=new Mat();
        Imgproc.calcHist(images,new MatOfInt(0),mask,hist,new MatOfInt(256),new MatOfFloat(0,255));
        Core.normalize(hist,hist,0,255,Core.NORM_MINMAX);
        int height=hist.rows();

        dst.create(400,400,src.type());
        dst.setTo(new Scalar(200,200,200));
        float[] histdata=new float[256];
        hist.get(0,0,histdata);
        int offsetx=50;
        int offsety=350;

        //绘制直方图
        Imgproc.line(dst,new Point(offsetx,0),new Point(offsetx,offsety),new Scalar(0,0,0));
        Imgproc.line(dst,new Point(offsetx,offsety),new Point(400,offsety),new Scalar(0,0,0));

        for(int i=0;i<height-1;i++){
            int y1=(int) histdata[i];
            int y2=(int) histdata[i+1];
            Rect rect =new Rect();
            rect.x=offsetx+i;
            rect.y=offsety-y1;
            rect.width=1;
            rect.height=y1;
            Imgproc.rectangle(dst,rect.tl(),rect.br(),new Scalar(15,15,15));
        }
        //释放内存
        gray.release();
        Imgproc.resize(dst, dst, new Size(src.cols(), src.rows()));
        return dst;
    }
}
