package opencvproject.com.ImgProcCamera;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ObjectDetector {

    private static final String TAG = "ObjectDetector";

    Net detector;
    private Scalar mRectColor;

    // path
    String protoPath;
    String caffeWeights;

    /**
     * 构造方法
     *
     * @param context              上下
     * @param rectColor            画笔颜色
     */
    public ObjectDetector(Context context, Scalar rectColor) {
        context = context.getApplicationContext();
        detector = createDetector(context);
        mRectColor = rectColor;
    }

    /**
     * 创建检测器
     *
     * @param context 上下文
     * @return 检测器
     */
    private Net createDetector(Context context) {
        ///android_asset/deploy.prototxt";
        protoPath = getPath("deploy.prototxt", context);
        //"file:///android_asset/res10_300x300_ssd_iter_140000.caffemodel";
        caffeWeights = getPath("res10_300x300_ssd_iter_140000.caffemodel", context);
        return Dnn.readNetFromCaffe(protoPath, caffeWeights); // load model
    }

    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }

    /**
     * 目标检测
     *
     * @param frame   单帧图像
     * @return 检测到的目标位置集合
     */
    public Mat detectObject(Mat frame) {
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        // image preprocessing
        Mat imageBlob = Dnn.blobFromImage(frame, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0), true, false, CvType.CV_32F);
        // as input of model
        detector.setInput(imageBlob); //set the input to network model
        // forward inference
        Mat detections = detector.forward(); //feed forward the input to the netwrok to get the output

        detections = detections.reshape(1, (int)detections.total() / 7);
        return detections;
    }

    // rectangular color
    public Scalar getRectColor() {
        return mRectColor;
    }
}
