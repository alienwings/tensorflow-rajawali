package org.rajawali3d.examples.examples.fcc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.CameraPermissionHelper;
import org.rajawali3d.examples.common.helpers.FullScreenHelper;
import org.rajawali3d.examples.common.helpers.SnackbarHelper;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.view.SurfaceView;
import org.tensorflow.demo.OverlayView;
import org.tensorflow.demo.tracking.MultiBoxTracker;

public class DeerGirlActivity extends AppCompatActivity {

    public static final String TAG = DeerGirlActivity.class.getSimpleName();

    public static final boolean DEBUG = true;

    private static DeerGirlARCoreRenderer sRender;

    private boolean installRequested = false;

    private SurfaceView surfaceView;
    private ImageView thumb;
    private OverlayView trackingOverlay;

    private DeerGirlARCoreRenderer renderer;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private TapHelper tapHelper;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceview);
        thumb = findViewById(R.id.thumb);
        trackingOverlay = findViewById(R.id.tracking_overlay);

        if (DEBUG) {
            thumb.setVisibility(View.VISIBLE);
            trackingOverlay.setVisibility(View.VISIBLE);
        } else {
            thumb.setVisibility(View.GONE);
            trackingOverlay.setVisibility(View.VISIBLE);
        }

        trackingOverlay.addCallback(new OverlayView.DrawCallback() {
            @Override
            public void drawCallback(Canvas canvas) {
                // test draw
//                Rect rect = new Rect(100, 600, 980, 1060);
//                Paint paint = new Paint();
//                paint.setColor(Color.RED);
//                paint.setStrokeWidth(20);
//                canvas.drawRect(rect, paint);

                renderer.getTracker().draw(canvas);
            }
        });

        // Set up tap listener.
        tapHelper = new TapHelper(this);
        surfaceView.setOnTouchListener(tapHelper);

        installRequested = false;
    }

    public static void setSRender(DeerGirlARCoreRenderer render) {
        sRender = render;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (renderer == null) {
            Exception exception = null;
            String message = null;
            try {
                ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(this, !installRequested);
                if (installStatus== ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    installRequested = true;
                    return;
                } else if (installStatus == ArCoreApk.InstallStatus.INSTALLED) {

                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(this)) {
                    CameraPermissionHelper.requestCameraPermission(this);
                    return;
                }

                // Create the session.
                Session session = new Session(this);

                if (sRender != null) {
                    sRender.setSession(session);
                    sRender.setTapHelper(tapHelper);
                    renderer = sRender;
                    renderer.setContext(this);

                } else {
                    // test all renders
                renderer = new DeerGirlObjRender(this, tapHelper, session);
//                renderer = new FbxDemo1Render(this, tapHelper, session);
//                renderer = new FbxDemo2Render(this, tapHelper, session);
//                renderer = new SkeletalAWDDemo1Render(this, tapHelper, session);
//                renderer = new SkeletalMD5Demo1Render(this, tapHelper, session);
//                    renderer = new SkeletalMD5Demo2Render(this, tapHelper, session);
//                renderer = new SkeletalBlendDemo1Render(this, tapHelper, session);
//                renderer = new Planes2000Demo1Render(this, tapHelper, session);
//                renderer = new ColorAnimDemo1Render(this, tapHelper, session);
//                    renderer = new VideoTextureDemo1Render(this, tapHelper, session);

                    //all post processing render not work
//                renderer = new FogDemo1Render(this, tapHelper, session);
//                renderer = new RenderToTextureDemo1Render(this, tapHelper, session);
//                renderer = new MultiPassDemo1Render(this, tapHelper, session);
                }

                // for test why pre-parse not work
//                try {
//                    AMeshLoader meshLoader = new LoaderMD5Mesh(renderer,
//                            R.raw.boblampclean_mesh);
//                    DeerGirlARCoreRenderer.setSMeshParser(meshLoader);
//                    meshLoader.parse();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                renderer.setSnackbarHelper(messageSnackbarHelper, this);
                renderer.setFrameCallback(new DeerGirlARCoreRenderer.FrameCallback() {
                    @Override
                    public void onFrameArrived(final Bitmap bitmap) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                thumb.setImageBitmap(bitmap);
//                                trackingOverlay.invalidate();
                            }
                        });
                    }

                    @Override
                    public void onTrackerUpdate() {
                        trackingOverlay.postInvalidate();
                    }
                });
                surfaceView.setSurfaceRenderer(renderer);

            } catch (UnavailableArcoreNotInstalledException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

        // Note that order matters - see the note in onPause(), the reverse applies here.
        if (renderer != null) {
            renderer.onResume();
        }
        surfaceView.onResume();
        messageSnackbarHelper.showMessage(this, "Searching for surfaces...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
        if (renderer != null ) {
            renderer.onPause();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this);
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
    }

}
