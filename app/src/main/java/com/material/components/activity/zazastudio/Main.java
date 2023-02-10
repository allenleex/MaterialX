package com.material.components.activity.zazastudio;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.material.components.R;
import com.material.components.utils.Tools;

import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutioncore.VideoInput;
import com.google.mediapipe.solutions.facemesh.FaceMesh;
import com.google.mediapipe.solutions.facemesh.FaceMeshOptions;
import com.google.mediapipe.solutions.facemesh.FaceMeshResult;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.zazastudio.tcp.FaceMeshResultGlRenderer;
import cn.zazastudio.tcp.FaceMeshResultImageView;
import cn.zazastudio.tcp.TCPClient;
import cn.zazastudio.tcp.TCPHandler;

public class Main extends AppCompatActivity {

    private BottomNavigationView navigation;
    private View search_bar;
    private ActionBar actionBar;
    private boolean rotate = false;

    private static final String TAG = "MainActivity";
    private FaceMesh facemesh;
    // Run the pipeline and the model inference on GPU or CPU.
    private static final boolean RUN_ON_GPU = true;
    private enum InputSource {
        UNKNOWN,
        IMAGE,
        VIDEO,
        CAMERA,
    }
    private InputSource inputSource = InputSource.UNKNOWN;
    // Image demo UI and image loader components.
    private ActivityResultLauncher<Intent> imageGetter;
    private FaceMeshResultImageView imageView;
    // Video demo UI and video loader components.
    private VideoInput videoInput;
    private ActivityResultLauncher<Intent> videoGetter;
    // Live camera demo UI and camera components.
    private CameraInput cameraInput;
    private SolutionGlSurfaceView<FaceMeshResult> glSurfaceView;
    private static final int[] USEFUL_LANDMARKS = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
    private static final int MAX_LANDMARK_ID = 30; //68
    private TCPClient client;
    private TCPHandler handler;
    private String ip = "192.168.1.12";
    private String port = "60000";
    private boolean tcp_enabled = false;
    private boolean camera_opened = false;
    private boolean camera_front = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zazastudio_main);

        SharedPreferences sharedPref = getSharedPreferences("option", Context.MODE_PRIVATE);
        tcp_enabled = sharedPref.getBoolean("openTcp", false);
        ip = sharedPref.getString("tcpIp", "");
        port = sharedPref.getString("tcpPort", "");

        initToolbar();
        initComponent();

        setupStaticImageDemoUiComponents();
        setupVideoDemoUiComponents();
        setupLiveDemoUiComponents();
        setupTcpDemoUiComponents();

        if (tcp_enabled) {
            tcpInit();
        } else if (client != null) client.closeAll();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name_cn);
        actionBar.setDisplayHomeAsUpEnabled(false);
        Tools.setSystemBarColor(this, R.color.grey_1000);
    }

    private void initComponent() {
        NestedScrollView nested_content = (NestedScrollView) findViewById(R.id.nested_scroll_view);
        nested_content.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                if (scrollY < oldScrollY) { // up
//                    animateNavigation(false);
//                    animateSearchBar(false);
//                }
//                if (scrollY > oldScrollY) { // down
//                    animateNavigation(true);
//                    animateSearchBar(true);
//                }
            }
        });
        search_bar = (View) findViewById(R.id.search_bar);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.navigation_main);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                NestedScrollView nested_content = (NestedScrollView) findViewById(R.id.nested_scroll_view);
                switch (item.getItemId()) {
                    case R.id.navigation_help:
                        startActivity(new Intent(getApplicationContext(), Help.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.navigation_main:
                        return true;
                    case R.id.navigation_settings:
                        startActivity(new Intent(getApplicationContext(), Settings.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        // display image
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_1), R.drawable.image_8);
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_2), R.drawable.image_9);
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_3), R.drawable.image_15);
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_4), R.drawable.image_14);
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_5), R.drawable.image_12);
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_6), R.drawable.image_2);
//        Tools.displayImageOriginal(this, (ImageView) findViewById(R.id.image_7), R.drawable.image_5);

    }


    boolean isNavigationHide = false;

    private void animateNavigation(final boolean hide) {
        if (isNavigationHide && hide || !isNavigationHide && !hide) return;
        isNavigationHide = hide;
        int moveY = hide ? (2 * navigation.getHeight()) : 0;
        navigation.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    boolean isSearchBarHide = false;

    private void animateSearchBar(final boolean hide) {
        if (isSearchBarHide && hide || !isSearchBarHide && !hide) return;
        isSearchBarHide = hide;
        int moveY = hide ? -(2 * search_bar.getHeight()) : 0;
        search_bar.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (inputSource == InputSource.CAMERA) {
            // Restarts the camera and the opengl surface rendering.
            cameraInput = new CameraInput(this);
            cameraInput.setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
            glSurfaceView.post(this::startCamera);
            glSurfaceView.setVisibility(View.VISIBLE);
        } else if (inputSource == InputSource.VIDEO) {
            videoInput.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (inputSource == InputSource.CAMERA) {
            glSurfaceView.setVisibility(View.GONE);
            cameraInput.close();
        } else if (inputSource == InputSource.VIDEO) {
            videoInput.pause();
        }
    }

    private Bitmap downscaleBitmap(Bitmap originalBitmap) {
        double aspectRatio = (double) originalBitmap.getWidth() / originalBitmap.getHeight();
        int width = imageView.getWidth();
        int height = imageView.getHeight();
        if (((double) imageView.getWidth() / imageView.getHeight()) > aspectRatio) {
            width = (int) (height * aspectRatio);
        } else {
            height = (int) (width / aspectRatio);
        }
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    private Bitmap rotateBitmap(Bitmap inputBitmap, InputStream imageData) throws IOException {
        int orientation =
                new ExifInterface(imageData)
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        if (orientation == ExifInterface.ORIENTATION_NORMAL) {
            return inputBitmap;
        }
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                matrix.postRotate(0);
        }
        return Bitmap.createBitmap(
                inputBitmap, 0, 0, inputBitmap.getWidth(), inputBitmap.getHeight(), matrix, true);
    }

    /** Sets up the UI components for the static image demo. */
    private void setupStaticImageDemoUiComponents() {
        imageView = new FaceMeshResultImageView(this);
    }

    /** Sets up core workflow for static image mode. */
    private void setupStaticImageModePipeline() {
    }

    /** Sets up the UI components for the video demo. */
    private void setupVideoDemoUiComponents() {
    }

    /** Sets up the UI components for the live demo with camera input. */
    private void setupLiveDemoUiComponents() {
        ((FloatingActionButton) findViewById(R.id.fab_capture)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!camera_opened) {
                    stopCurrentPipeline();
                    setupStreamingModePipeline(InputSource.CAMERA);
                } else {
                    stopCurrentPipeline();
                }
                camera_opened = !camera_opened;
            }
        });
        ((FloatingActionButton) findViewById(R.id.fab_switch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputSource == InputSource.CAMERA && camera_opened) {
                    if(camera_front) {
                        cameraInput.start(
                                Main.this,
                                facemesh.getGlContext(),
                                CameraInput.CameraFacing.BACK,
                                glSurfaceView.getWidth(),
                                glSurfaceView.getHeight());
                    } else {
                        cameraInput.start(
                                Main.this,
                                facemesh.getGlContext(),
                                CameraInput.CameraFacing.FRONT,
                                glSurfaceView.getWidth(),
                                glSurfaceView.getHeight());
                    }
                    camera_front = !camera_front;
                }
            }
        });

    }

    /** Sets up core workflow for streaming mode. */
    private void setupStreamingModePipeline(InputSource inputSource) {
        this.inputSource = inputSource;
        // Initializes a new MediaPipe Face Mesh solution instance in the streaming mode.
        facemesh =
                new FaceMesh(
                        this,
                        FaceMeshOptions.builder()
                                .setStaticImageMode(false)
                                .setRefineLandmarks(true)
                                .setRunOnGpu(RUN_ON_GPU)
                                .build());
        facemesh.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Face Mesh error:" + message));

        if (inputSource == InputSource.CAMERA) {
            cameraInput = new CameraInput(this);
            cameraInput.setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
        } else if (inputSource == InputSource.VIDEO) {
            videoInput = new VideoInput(this);
            videoInput.setNewFrameListener(textureFrame -> facemesh.send(textureFrame));
        }

        // Initializes a new Gl surface view with a user-defined FaceMeshResultGlRenderer.
        glSurfaceView =
                new SolutionGlSurfaceView<>(this, facemesh.getGlContext(), facemesh.getGlMajorVersion());
        glSurfaceView.setSolutionResultRenderer(new FaceMeshResultGlRenderer());
        glSurfaceView.setRenderInputImage(true);

        AtomicInteger count = new AtomicInteger(1);
        List<ActionCapture> actionCaptureList = new ArrayList<>();
        facemesh.setResultListener(
                faceMeshResult -> {
                    ActionCapture actionCapture = logNoseLandmark(faceMeshResult, /*showPixelValues=*/ false, count.get() / 30, count.get() % 30);
                    actionCaptureList.add(actionCapture);
//                    if (tcp_enabled && count.incrementAndGet() > 60) {
//                        count.set(1);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                client.sendMessage(actionCapture.toString()); //开启新线程发送数据
                                actionCaptureList.clear();
                            }
                        }).start();
//                    }

                    glSurfaceView.setRenderData(faceMeshResult);
                    glSurfaceView.requestRender();
                });

        // The runnable to start camera after the gl surface view is attached.
        // For video input source, videoInput.start() will be called when the video uri is available.
        if (inputSource == InputSource.CAMERA) {
            glSurfaceView.post(this::startCamera);
        }

        // Updates the preview layout.
        FrameLayout frameLayout = findViewById(R.id.preview_display_layout);
        imageView.setVisibility(View.GONE);
        frameLayout.removeAllViewsInLayout();
        frameLayout.addView(glSurfaceView);
        glSurfaceView.setVisibility(View.VISIBLE);
        frameLayout.requestLayout();
    }

    private void startCamera() {
        cameraInput.start(
                this,
                facemesh.getGlContext(),
                CameraInput.CameraFacing.FRONT,
                glSurfaceView.getWidth(),
                glSurfaceView.getHeight());
    }

    private void stopCurrentPipeline() {
        if (cameraInput != null) {
            cameraInput.setNewFrameListener(null);
            cameraInput.close();
        }
        if (videoInput != null) {
            videoInput.setNewFrameListener(null);
            videoInput.close();
        }
        if (glSurfaceView != null) {
            glSurfaceView.setVisibility(View.GONE);
        }
        if (facemesh != null && camera_opened) {
            facemesh.close();
        }
    }

    @SuppressLint("DefaultLocale")
    private ActionCapture logNoseLandmark(FaceMeshResult result, boolean showPixelValues, int second, int frame) {
        if (result == null || result.multiFaceLandmarks().isEmpty()) {
            return null;
        }

        java.util.List<NormalizedLandmark> landmarks = result.multiFaceLandmarks().get(0).getLandmarkList();
        List<FaceData> data = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        for (int k = 0; k <= MAX_LANDMARK_ID; k++) {
            NormalizedLandmark mark = landmarks.get(k);

            data.add(new FaceData(k, mark.getX(), mark.getY(), mark.getZ()));
        }
        ActionCapture actionCapture = new ActionCapture(second, frame, 0, timestamp, data);

        Log.i(TAG, actionCapture.toString()); // 打印位点坐标数据到日志
        return actionCapture;
    }

    private void setupTcpDemoUiComponents() {
//        Button startTcpButton = findViewById(R.id.button_start_tcp);
//        startTcpButton.setOnClickListener(
//                v -> {
//                    tcp_enabled = !tcp_enabled;
//                });
    }

    private void tcpInit() {
        handler = new TCPHandler(Main.this);
        client = new TCPClient(handler);

        if (TCPHandler.CONNECT_STATUS) {
            handler.sendEmptyMessage(TCPHandler.CONNECT_BREAK);
            client.closeAll();
        } else //如果未连接，则开始连接服务端
        {
            if (!ip.equals("") && !port.equals("")) {
                new Thread(new Runnable()//开启新线程进行网络请求操作
                {
                    @Override
                    public void run() {
                        client.connect(ip, Integer.parseInt(port));
                    }
                }).start();
            } else {
                //
            }
        }
    }

}