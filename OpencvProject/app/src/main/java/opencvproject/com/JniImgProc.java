package opencvproject.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import org.opencv.imgproc.Imgproc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JniImgProc extends Fragment {
    private native void jniMatrix(long matAddr, int threshold, long targetAddr);
    private native void jniGray(long matAddr, double progress, long targetAddr);
    private native void jniBlur(long matAddr, int progress, long targetAddr);
    private native void jniBinarization(long matAddr, int progress, long targetAddr);
    private native void jniCanny(long matAddr, int progress, long targetAddr);

    static {
        System.loadLibrary("matrixProc");
    }

    // for camera
    private int PICK_IMAGE_REQUEST = 1;
    private double max_size = 256;
    private ImageView myImageChangeView;//通过ImageView来显示结果
    private Bitmap selectbp;//所选择的bitmap

    // matrix proc
    private SeekBar seekBar;
    // 1 Gray 2 Blur 3 Binarization 4 canny 5 Matrix 6 histogram
    int who = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.jni_img_proc, container,false);

        // 1 Gray 2 Blur 3 Binarization 4 canny 5 Matrix 6 histogram 7 equalization;
        RadioButton gray = (RadioButton) view.findViewById(R.id.gray);
        gray.setOnClickListener(v -> { who = 1; });
        RadioButton blur = (RadioButton) view.findViewById(R.id.blur);
        blur.setOnClickListener(v -> { who = 2 ; });
        RadioButton binarize = (RadioButton) view.findViewById(R.id.binarize);
        binarize.setOnClickListener(v -> { who = 3; });
        RadioButton canny = (RadioButton) view.findViewById(R.id.canny);
        canny.setOnClickListener(v -> { who = 4; });
        RadioButton matrix = (RadioButton) view.findViewById(R.id.matrix);
        matrix.setOnClickListener(v -> { who = 5; });

        seekBar = (SeekBar) view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                progressBar.setProgress(progress);
//                textView.setText("" + progress + "%");
                if (checkIMG())
                    // 1 Gray 2 Blur 3 Binarization 4 canny 5 Matrix 6 histogram 7 equalization;
                    switch (who){
                        case 1: convertGray(progress); break;
                        case 2: BlurProcess(progress); break;
                        case 3: BinarizationProcess(progress); break;
                        case 4: cannyProcess(progress); break;
                        case 5: Matrix(progress); break;
                        case 6: break;
                        default: break;
                    }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        Button select_img_Btn = (Button) view.findViewById(R.id.select_btn);
        select_img_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//定义按钮监听器
                selectImage(v);
            }
        });

        myImageChangeView = (ImageView) view.findViewById(R.id.imageChangeView);
        myImageChangeView.setScaleType(ImageView.ScaleType.FIT_CENTER);//设置显示图片的属性。把图片按比例扩大/缩小到View的宽度，居中显示

        return view;
    }

    //灰度转换函数
    public void convertGray(int progress) {
        double cal = progress/50.0;

        // src to mat
        Mat mat_selectbp = new Mat();
        Utils.bitmapToMat(selectbp, mat_selectbp);
        // target mat
        Mat newImg = Mat.zeros(new Size(selectbp.getWidth(), selectbp.getHeight()), CvType.CV_8UC3);

        // cpp调用
        jniGray(mat_selectbp.getNativeObjAddr(), cal, newImg.getNativeObjAddr());

        // target mat to bitmap
        Bitmap selectbp2 = Bitmap.createBitmap(newImg.cols(), newImg.rows(), selectbp.getConfig()) ;
        Utils.matToBitmap(newImg, selectbp2);
        // 显示位图
        myImageChangeView.setImageBitmap(selectbp2);
    }

    public void BlurProcess(int progress) {
        // src to mat
        Mat mat_selectbp = new Mat();
        Utils.bitmapToMat(selectbp, mat_selectbp);
        // target
        Mat newImg = Mat.zeros(new Size(selectbp.getWidth(), selectbp.getHeight()), CvType.CV_8UC3);
        // cpp调用
        jniBlur(mat_selectbp.getNativeObjAddr(), progress, newImg.getNativeObjAddr());

        Bitmap selectbp2 = Bitmap.createBitmap(newImg.width(), newImg.height(), selectbp.getConfig()) ;
        Utils.matToBitmap(newImg, selectbp2);
        myImageChangeView.setImageBitmap(selectbp2);
    }

    public void BinarizationProcess(int progress) {
        // src to mat
        Mat mat_selectbp = new Mat();
        Utils.bitmapToMat(selectbp, mat_selectbp);
        // target
        Mat newImg = Mat.zeros(new Size(selectbp.getWidth(), selectbp.getHeight()), CvType.CV_8UC3);
        // cpp调用
        jniBinarization(mat_selectbp.getNativeObjAddr(), progress, newImg.getNativeObjAddr());

        Bitmap selectbp2 = Bitmap.createBitmap(newImg.width(), newImg.height(), selectbp.getConfig()) ;
        Utils.matToBitmap(newImg, selectbp2);
        myImageChangeView.setImageBitmap(selectbp2);
    }

    // canny转换函数
    public void cannyProcess(int c) {
        // src to mat
        Mat mat_selectbp = new Mat();
        Utils.bitmapToMat(selectbp, mat_selectbp);
        // target
        Mat newImg = Mat.zeros(new Size(selectbp.getWidth(), selectbp.getHeight()), CvType.CV_8UC3);
        // cpp调用
        jniCanny(mat_selectbp.getNativeObjAddr(), c, newImg.getNativeObjAddr());

        Bitmap selectbp2 = Bitmap.createBitmap(newImg.width(), newImg.height(), selectbp.getConfig()) ;
        Utils.matToBitmap(newImg, selectbp2);
        myImageChangeView.setImageBitmap(selectbp2);
    }

    public void Matrix(int threshold){

        threshold *= 2.55;

        Mat mat_selectbp = new Mat();
        Utils.bitmapToMat(selectbp, mat_selectbp);

        Size newsz = new Size(selectbp.getWidth(), selectbp.getHeight());
        Mat newImg = Mat.zeros(newsz, CvType.CV_8UC3);

        // cpp调用
        jniMatrix(mat_selectbp.getNativeObjAddr(), threshold, newImg.getNativeObjAddr());

        Bitmap selectbp2 = Bitmap.createBitmap(newImg.cols(), newImg.rows(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(newImg, selectbp2);//再将mat转换为位图
        myImageChangeView.setImageBitmap(selectbp2);//显示位图
    }

    // camera 回调函数
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {//当为选择image这个意图时，进入下面代码。选择图片以位图的形式显示出来
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input = getActivity().getContentResolver().openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(input, null, options);
                int raw_width = options.outWidth;
                int raw_height = options.outHeight;
                int max = Math.max(raw_width, raw_height);
                int newWidth = raw_width;
                int newHeight = raw_height;
                int inSampleSize = 1;
                if(max > max_size) {
                    newWidth = raw_width / 2;
                    newHeight = raw_height / 2;
                    while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                        inSampleSize *=2;
                    }
                }

                options.inSampleSize = inSampleSize;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                selectbp = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(uri), null, options);

                myImageChangeView.setImageBitmap(selectbp);//将所选择的位图显示出来

                // 自适应宽高
                DisplayMetrics dm=new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                double delta1 = (dm.widthPixels*1.0/newWidth);
                double delta2 = (0.68*dm.heightPixels/newHeight);
                double delta = delta1>delta2?delta2:delta1;

                android.view.ViewGroup.LayoutParams layoutParams = myImageChangeView.getLayoutParams();
                layoutParams.width = (int)(delta*newWidth);
                layoutParams.height = (int)(delta*newHeight);
                myImageChangeView.setLayoutParams(layoutParams);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void selectImage(View v) {
        Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(myIntent, PICK_IMAGE_REQUEST);
    }

    public boolean checkIMG(){
        if (selectbp == null){
            final Toast toast = Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT);
            toast.show();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    toast.cancel();
                }
            }, 1000);
            return false;
        }
        return true;
    }
}
