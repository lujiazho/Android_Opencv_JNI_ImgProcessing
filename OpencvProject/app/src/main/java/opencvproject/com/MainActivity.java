package opencvproject.com;

import android.Manifest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import opencvproject.com.camera.BaseActivity;
import opencvproject.com.logo.logo;
import com.kongqw.permissionslibrary.PermissionsManager;

//public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2
public class MainActivity extends BaseActivity {

    private static final String  TAG = "MainActivity";

    // 摄像头权限
    private PermissionsManager mPermissionsManager;
    // 要校验的权限
    private final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    // bottomNavigation
    Fragment selectedFragment = new logo();

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (getSupportActionBar() != null){ // 隐藏action bar
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 导航栏
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        bottomNavigationView.getMenu().getItem(3).setChecked(true);

        // 封面
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

        // 动态权限校验
        mPermissionsManager = new PermissionsManager(this) {

            @Override
            public void authorized(int requestCode) {
                // 权限通过
                switch (requestCode) {
                    case MY_CAMERA_PERMISSION_CODE:
                        Log.i("权限通过", "xxx");
                        startActivity(new Intent(MainActivity.this, FaceDetection.class));
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void noAuthorization(int requestCode, String[] lacksPermissions) {
                // 缺少必要权限
                showPermissionDialog();
            }

            @Override
            public void ignore(int requestCode) {
                // Android 6.0 以下系统不校验
                authorized(requestCode);
            }
        };

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 用户做出选择以后复查权限，判断是否通过了权限申请
        mPermissionsManager.recheckPermissions(requestCode, permissions, grantResults);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item){
                    switch (item.getItemId()){
                        case R.id.imgProcessing:
                            selectedFragment = new ImgProcessing();
                            break;
                        case R.id.jniImgProc:
                            selectedFragment = new JniImgProc();
                            break;
                        case R.id.faceDetection:
                            Log.i("点击了", "xxx");
                            mPermissionsManager.checkPermissions(MY_CAMERA_PERMISSION_CODE, PERMISSIONS);
                            return false;
                    }
                    Log.i("开始segment", "xxx");
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    // this function is right after onStart() and onRestart(), so we can both start and restart the camera view here
    {
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
    }
}