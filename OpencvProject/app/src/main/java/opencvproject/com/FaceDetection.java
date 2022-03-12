package opencvproject.com;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import org.opencv.core.Scalar;

import android.view.Window;

import opencvproject.com.ImgProcCamera.ImgProcessor;
import opencvproject.com.camera.BaseActivity;
import opencvproject.com.ImgProcCamera.CameraView;
import opencvproject.com.ImgProcCamera.ObjectDetector;
import opencvproject.com.camera.listener.OnOpenCVLoadListener;

public class FaceDetection extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "FaceDetection";
    // 1. initialize javaCameraView
//    private CameraBridgeViewBase mOpenCvCameraView;

    // new detector
    private CameraView cameraView;
    private ObjectDetector mFaceDetector;

    // new processor
    private ImgProcessor mCannyProcessor;
    private ImgProcessor mHistProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 之前是需要的，但在fragment里如下会导致错误
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detection);

        ((RadioButton) findViewById(R.id.face)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.canny)).setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.histogram)).setOnCheckedChangeListener(this);

        cameraView = (CameraView) findViewById(R.id.face_detection);

        cameraView.setOnOpenCVLoadListener(new OnOpenCVLoadListener() {
            @Override
            public void onOpenCVLoadSuccess() {
                Toast.makeText(getApplicationContext(), "OpenCV 加载成功", Toast.LENGTH_SHORT).show();
                mFaceDetector = new ObjectDetector(getApplicationContext(), new Scalar(255, 0, 0, 255));
                mCannyProcessor = new ImgProcessor(getApplicationContext(), 1);
                mHistProcessor = new ImgProcessor(getApplicationContext(), 2);
                findViewById(R.id.radio_group).setVisibility(View.VISIBLE);
            }

            @Override
            public void onOpenCVLoadFail() {
                Toast.makeText(getApplicationContext(), "OpenCV 加载失败", Toast.LENGTH_SHORT).show();
            }
        });

        cameraView.loadOpenCV(this);
    }

    public void swapCamera(View view) {
        cameraView.swapCamera();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.face:
                if (isChecked) {
                    Toast.makeText(this, "face detection", Toast.LENGTH_SHORT).show();
                    cameraView.addDetector(mFaceDetector);
                } else {
                    cameraView.removeDetector(mFaceDetector);
                }
                break;
            case R.id.canny:
                if (isChecked) {
                    Toast.makeText(this, "cannize", Toast.LENGTH_SHORT).show();
                    cameraView.addProcessor(mCannyProcessor);
                }else{
                    cameraView.removeProcessor(mCannyProcessor);
                }
                break;
            case R.id.histogram:
                if (isChecked) {
                    Toast.makeText(this, "histogram", Toast.LENGTH_SHORT).show();
                    cameraView.addProcessor(mHistProcessor);
                }else{
                    cameraView.removeProcessor(mHistProcessor);
                }
            default:
                break;
        }
    }
}
