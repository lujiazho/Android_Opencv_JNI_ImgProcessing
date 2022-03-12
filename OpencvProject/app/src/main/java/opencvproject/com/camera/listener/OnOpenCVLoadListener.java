package opencvproject.com.camera.listener;

public interface OnOpenCVLoadListener {

    // OpenCV加载成功
    void onOpenCVLoadSuccess();

    // OpenCV加载失败
    void onOpenCVLoadFail();
}
