package opencvproject.com;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
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

import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.opencv.imgproc.Imgproc.putText;

public class ImgProcessing extends Fragment {
    private native String jniTellMeWhy(String hiJni);

    static {
        System.loadLibrary("JniImgProc");
    }

    private double max_size = 256;
    private int PICK_IMAGE_REQUEST = 1;
    private ImageView myImageView;//通过ImageView来显示结果
    private ImageView myImageChangeView;//通过ImageView来显示结果
    private Bitmap selectbp;//所选择的bitmap

    // bar
//    private TextView textView;
//    private ProgressBar progressBar;
    private SeekBar seekBar;


//    private SeekBar seekBarBlur;
//    private SeekBar seekBarBinarization;
//    private SeekBar seekBarcanny1;
//    private SeekBar seekBarcanny2;
//    private SeekBar seekBarmatrix_threshold;
//    private int canny = 0;
//    private int thres = 0;

    // 1 Gray 2 Blur 3 Binarization 4 canny 5 Matrix 6 histogram
    int who = 0;

    //相机权限获取
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//写权限
            Manifest.permission.CAMERA//照相权限
    };


    //初始化
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.img_proc,container,false);


        String s = jniTellMeWhy("我是MainActivity,tell me why!");
        Log.i("TAG",s);

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
        RadioButton histogram = (RadioButton) view.findViewById(R.id.histogram);
        histogram.setOnClickListener(v -> { who = 6; if (checkIMG()) histogramProcess();}); // reduce redundant calc
//        matrix.setEnabled(false);

//        textView = (TextView) findViewById(R.id.textView);
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
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
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        seekBarBlur = (SeekBar) view.findViewById(R.id.seekBarBlur);
//        seekBarBlur.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (checkIMG()) BlurProcess(progress);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });

//        seekBarBinarization = (SeekBar) view.findViewById(R.id.seekBarBinarization);
//        seekBarBinarization.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (checkIMG()) BinarizationProcess(progress);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });

//        seekBarcanny1 = (SeekBar) view.findViewById(R.id.seekBarcanny1);
//        seekBarcanny1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                canny1 = progress;
//                if (checkIMG()) cannyProcess(canny1, canny2);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });

//        seekBarcanny2 = (SeekBar) view.findViewById(R.id.seekBarcanny2);
//        seekBarcanny2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                canny2 = progress;
//                if (checkIMG()) cannyProcess(canny1, canny2);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });

//        seekBarmatrix_threshold = (SeekBar) view.findViewById(R.id.seekBarmatrix_threshold);
//        seekBarmatrix_threshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                thres = progress;
//                if (checkIMG()) Matrix(thres);
//            }
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {}
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {}
//        });

//        myImageView = (ImageView) view.findViewById(R.id.imageView);
//        myImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);//设置显示图片的属性。把图片按比例扩大/缩小到View的宽度，居中显示
        myImageChangeView = (ImageView) view.findViewById(R.id.imageChangeView);
        myImageChangeView.setScaleType(ImageView.ScaleType.FIT_CENTER);//设置显示图片的属性。把图片按比例扩大/缩小到View的宽度，居中显示

        Button select_img_Btn = (Button) view.findViewById(R.id.select_btn);
        select_img_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//定义按钮监听器
                selectImage(v);
            }
        });

//        //模糊
//        //定义处理的按钮
//        Button processBtn_blur = (Button) view.findViewById(R.id.process_btn_blur);
//        processBtn_blur.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {//定义按钮监听器
//                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
//                BlurProcess(50);//灰度转换
//            }
//        });

//        //二值化
//        //定义处理的按钮
//        Button processBtn_Binarization = (Button) view.findViewById(R.id.Binarization);
//        processBtn_Binarization.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {//定义按钮监听器
//                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
//                BinarizationProcess(50);//二值化
//            }
//        });

//        //canny
//        //定义处理的按钮
//        Button processBtn_canny = (Button) view.findViewById(R.id.canny);
//        processBtn_canny.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {//定义按钮监听器
//                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
//                cannyProcess(100, 100);
//            }
//        });


        //直方图
        //定义处理的按钮
//        Button processBtn_histogram = (Button) view.findViewById(R.id.histogram);
//        processBtn_histogram.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {//定义按钮监听器
//                // makeText(MainActivity.this.getApplicationContext(), "hello, image process", Toast.LENGTH_SHORT).show();
//                if (checkIMG()) histogramProcess();
//            }
//
//            //处理
//            private void histogramProcess() {
//                Mat src = new Mat();
//                Mat temp = new Mat();
//                Mat dst = new Mat();
//                Utils.bitmapToMat(selectbp, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
//                Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
//                //进行直方图绘制
//                displayHistogram(src,dst);
//
//                Bitmap selectbp2 = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888) ;
//                Utils.matToBitmap(dst, selectbp2);//再将mat转换为位图
//                myImageChangeView.setImageBitmap(selectbp2);//显示位图
//            }
//
//            private void displayHistogram(Mat src, Mat dst){
//                Mat gray=new Mat();
//                Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);//转换为灰度图
//
//                //计算直方图数据并归一化
//                List<Mat> images=new ArrayList<>();
//                images.add(gray);
//                Mat mask=Mat.ones(src.size(),CvType.CV_8UC1);
//                Mat hist=new Mat();
//                Imgproc.calcHist(images,new MatOfInt(0),mask,hist,new MatOfInt(256),new MatOfFloat(0,255));
//                Core.normalize(hist,hist,0,255,Core.NORM_MINMAX);
//                int height=hist.rows();
//
//                dst.create(400,400,src.type());
//                dst.setTo(new Scalar(200,200,200));
//                float[] histdata=new float[256];
//                hist.get(0,0,histdata);
//                int offsetx=50;
//                int offsety=350;
//
//                //绘制直方图
//                Imgproc.line(dst,new Point(offsetx,0),new Point(offsetx,offsety),new Scalar(0,0,0));
//                Imgproc.line(dst,new Point(offsetx,offsety),new Point(400,offsety),new Scalar(0,0,0));
//
//                for(int i=0;i<height-1;i++){
//                    int y1=(int) histdata[i];
//                    int y2=(int) histdata[i+1];
//                    Rect rect =new Rect();
//                    rect.x=offsetx+i;
//                    rect.y=offsety-y1;
//                    rect.width=1;
//                    rect.height=y1;
//                    Imgproc.rectangle(dst,rect.tl(),rect.br(),new Scalar(15,15,15));
//                }
//                //释放内存
//                gray.release();
//            }
//        });
        return view;
    }

    public void selectImage(View v) {
        // method 1
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);//允许用户选择特殊种类的数据，并返回（特殊种类的数据：照一张相片或录一段音）
//        startActivityForResult(Intent.createChooser(intent,"选择图像..."), PICK_IMAGE_REQUEST);//启动另外一个活动
        // method 2
        Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(myIntent, PICK_IMAGE_REQUEST);
    }


    //免安装Opencv manager
    //onResume()这个方法在活动准备好和用户进行交互的时候调用。此时的活动一定位于返回栈的栈顶，并且处于运行状态。
    //所以在活动开启前调用，检查是否有opencv库，若没有，则下载
    @Override
    public void onResume() {
        super.onResume();
        //免安装opencv manager（opencv3.0开始可以采用这种方法）
        if (!OpenCVLoader.initDebug()) {
            System.out.println("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, getActivity(), mLoaderCallback);
        } else {
            System.out.println("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    // OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.out.println("OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };


    //静态注册opencv发表方法（应该是旧版的opencv采用的方法）
//    private void staticLoadCVLibraries() {
//        boolean load = OpenCVLoader.initDebug();
//        if(load) {//注册成功后，输出
//            Log.i("CV", "Open CV Libraries loaded...");
//        }
//
//    }


    //在一个主界面(主Activity)通过意图（startActivityForResult）跳转至多个不同子Activity上去，
    // 当子模块的代码执行完毕后再次返回主页面，将子activity中得到的数据显示在主界面/完成的数据交给主Activity处理。
    // 这种带数据的意图跳转需要使用activity的onActivityResult()方法
    //note:点击完选择图片按钮后，应该进入的是这里的选项
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //requestCode 最初提供给startActivityForResult（）的整数请求代码，允许您识别此结果的来源。
        //整数requestCode用于与startActivityForResult中的requestCode中值进行比较判断，是以便确认返回的数据是从哪个Activity返回的。
        //resultCode 子活动通过其setResult（）返回的整数结果代码。适用于多个activity都返回数据时，来标识到底是哪一个activity返回的值。
        //data。一个Intent对象，带有返回的数据。可以通过data.getXxxExtra( );方法来获取指定数据类型的数据，
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {//当为选择image这个意图时，进入下面代码。选择图片以位图的形式显示出来
            // method to reduce the image
            Uri uri = data.getData();
            try {
                Log.d("image-tag", "start to decode selected image now...");
                InputStream input =
                        getActivity().getContentResolver().openInputStream(uri);
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
            // easy method
//            imageUri = data.getData();
//            try{
//                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
//            }catch (IOException e){
//                e.printStackTrace();
//            }
//            imageView.setImageBitmap(imageBitmap);
        }
    }


    //灰度转换函数
    public void convertGray(int progress) {
        Bitmap bmOut = Bitmap.createBitmap(selectbp.getWidth(), selectbp.getHeight(), selectbp.getConfig());
        double cal = progress/50.0;
        // color information
        int A, R, G, B;
        int pixel;
        for (int x = 0; x < selectbp.getWidth(); ++x) {
            for (int y = 0; y < selectbp.getHeight(); ++y) {
                // get pixel color
                pixel = selectbp.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                // use 128 as threshold, above -> white, below -> black
                gray *= cal;
                if (gray > 255) {
                    gray = 255;
                }
                else if (gray < 0){
                    gray = 0;
                }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }
        myImageChangeView.setImageBitmap(bmOut);//显示位图
    }

    public void BlurProcess(int progress) {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
        progress = progress/10+1;
        Imgproc.blur(temp,dst,new Size(progress,progress)); // kernal size
        Bitmap selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst, selectbp2);//再将mat转换为位图
        myImageChangeView.setImageBitmap(selectbp2);//显示位图
    }

    public void BinarizationProcess(int progress) {
        Mat src = new Mat();
        Mat temp = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
        Imgproc.cvtColor(src, temp, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
        Imgproc.cvtColor(temp, temp, Imgproc.COLOR_BGR2GRAY);//灰度化处理。

        double pro = progress*2.55;
//                Imgproc.adaptiveThreshold(temp,dst,255,Imgproc.ADAPTIVE_THRESH_MEAN_C,Imgproc.THRESH_BINARY,15,10);
        Imgproc.threshold(temp,dst, pro,255,Imgproc.THRESH_BINARY);
        Bitmap selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst, selectbp2);//再将mat转换为位图
        myImageChangeView.setImageBitmap(selectbp2);//显示位图
    }

    // canny转换函数
    public void cannyProcess(int c) {
        Mat src = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成

        Mat gray=new Mat();
        //转换为灰度图
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(gray,gray,c*2,c*2,3,true); // here got gray form
        Core.bitwise_and(src,src,dst,gray); // here color form

        Bitmap selectbp2 = Bitmap.createBitmap(src.width(), src.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst, selectbp2);//再将mat转换为位图
        myImageChangeView.setImageBitmap(selectbp2);//显示位图
    }

    private void histogramProcess() {
        Mat src = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(selectbp, src);//将位图转换为Mat数据。而对于位图，其由A、R、G、B通道组成
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);//转换为BGR（opencv中数据存储方式）
        //进行直方图绘制
        displayHistogram(src,dst);

        Bitmap selectbp2 = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(dst, selectbp2);//再将mat转换为位图
        myImageChangeView.setImageBitmap(selectbp2);//显示位图
    }

    private void displayHistogram(Mat src, Mat dst){
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

    public void Matrix(int threshold){

        threshold *= 2.55;

        if (!checkIMG()) {
            return;
        }

        int width = selectbp.getWidth();
        int height = selectbp.getHeight();

        Mat mat_selectbp = new Mat();
        Mat canny=new Mat();
        Utils.bitmapToMat(selectbp, mat_selectbp);

        Imgproc.cvtColor(mat_selectbp,canny,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(canny,canny,100,100,3,true); // here got gray form

        // 自适应大小限制
        int cellHeight = 1, cellWidth = 1;
        while (width/cellWidth > 100 || height/cellHeight > 100){
            cellWidth *= 2;
            cellHeight *= 2;
        }

        Size sz = new Size((width / cellWidth),(height / cellHeight));
        Imgproc.resize(canny, canny, sz);

        Bitmap cannybitmap = Bitmap.createBitmap(canny.width(), canny.height(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(canny, cannybitmap);

        Size newsz = new Size(width,height);
        Mat newImg = Mat.zeros(newsz, CvType.CV_8UC3);

        String num = "0";
        int pixel;
        int R, G, B;

        for (int i = 0; i < canny.cols(); i+=1) {
            for (int j = 0; j < canny.rows(); j+=1) {
                pixel = cannybitmap.getPixel(i, j);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
//                Log.i("看看", pixel+" "+R+" "+G+" "+B);
                int k = (R + G + B) / 3;
                double newi = i * cellWidth;
                double newj = j * cellHeight;
                if (k > threshold){
                    putText(newImg, num, new Point(newi, newj), Core.FONT_HERSHEY_COMPLEX_SMALL, 0.4, new Scalar(0, 255, 0));
                    num = (num.equals("0")?"1":"0");
                }
            }
        }
        Bitmap selectbp2 = Bitmap.createBitmap(newImg.cols(), newImg.rows(), Bitmap.Config.ARGB_8888) ;
        Utils.matToBitmap(newImg, selectbp2);//再将mat转换为位图
        myImageChangeView.setImageBitmap(selectbp2);//显示位图
    }
}
