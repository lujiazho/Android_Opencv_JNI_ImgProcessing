package opencvproject.com.ImgProcCamera;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import opencvproject.com.camera.BaseCameraView;

public class CameraView extends BaseCameraView {
    private static final String TAG = "ObjectDetectionView";
    private ArrayList<ObjectDetector> mObjectDetects;
    private ArrayList<ImgProcessor>  mImgProcessors;

    @Override
    public void onOpenCVLoadSuccess() {
        Log.i(TAG, "onOpenCVLoadSuccess: ");

        mObjectDetects = new ArrayList<>();
        mImgProcessors = new ArrayList<>();
    }

    @Override
    public void onOpenCVLoadFail() {
        Log.i(TAG, "onOpenCVLoadFail: ");
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // 子线程（非UI线程）
        mRgba = inputFrame.rgba();

        for (ObjectDetector detector : mObjectDetects) {
            // 检测目标
            Mat detections = detector.detectObject(mRgba);

            int cols = mRgba.cols(); // width
            int rows = mRgba.rows(); // height
            double THRESHOLD = 0.55;

            for (int i = 0; i < detections.rows(); ++i) {
                double confidence = detections.get(i, 2)[0];
                if (confidence > THRESHOLD) {
                    int left   = (int)(detections.get(i, 3)[0] * cols); // x1 ∈ [0, 1]
                    int top    = (int)(detections.get(i, 4)[0] * rows); // y1
                    int right  = (int)(detections.get(i, 5)[0] * cols); // x2
                    int bottom = (int)(detections.get(i, 6)[0] * rows); // y2

                    Imgproc.rectangle(mRgba, new Point(left, top), new Point(right, bottom), detector.getRectColor(),3);
                }
            }
        }

        for (ImgProcessor processor : mImgProcessors) {
            mRgba = processor.process(mRgba);
        }

        return mRgba;
    }

    /**
     * 添加检测器
     *
     * @param detector 检测器
     */
    public synchronized void addDetector(ObjectDetector detector) {
        if (!mObjectDetects.contains(detector)) {
            mObjectDetects.add(detector);
        }
    }

    public synchronized void addProcessor(ImgProcessor processor) {
        if (!mImgProcessors.contains(processor)) {
            mImgProcessors.add(processor);
        }
    }

    /**
     * 移除检测器
     *
     * @param detector 检测器
     */
    public synchronized void removeDetector(ObjectDetector detector) {
        if (mObjectDetects.contains(detector)) {
            mObjectDetects.remove(detector);
        }
    }

    public synchronized void removeProcessor(ImgProcessor processor) {
        if (mImgProcessors.contains(processor)) {
            mImgProcessors.remove(processor);
        }
    }
}
