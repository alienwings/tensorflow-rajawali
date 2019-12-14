package org.rajawali3d.examples.common.rendering;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.media.Image;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.examples.java.common.rendering.CameraBackground;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;

import org.rajawali3d.examples.common.helpers.DetectResultHelper;
import org.rajawali3d.examples.common.helpers.DisplayRotationHelper;
import org.rajawali3d.examples.common.helpers.SnackbarHelper;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.examples.fcc.DeerGirlActivity;
import org.rajawali3d.examples.examples.fcc.PlaneRenderer;
import org.rajawali3d.examples.examples.fcc.PointCloud3D;
import org.rajawali3d.examples.examples.fcc.PointCloudRenderer;
import org.rajawali3d.loader.AMeshLoader;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.renderer.Renderer;
import org.tensorflow.demo.Classifier;
import org.tensorflow.demo.TensorFlowObjectDetectionAPIModel;
import org.tensorflow.demo.env.ImageUtils;
import org.tensorflow.demo.tracking.MultiBoxTracker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DeerGirlARCoreRenderer extends Renderer {

    protected static final int CROP_SIZE = 300;
    protected Integer sensorOrientation = 90;
    protected boolean isProcessingFrame = false;
    protected boolean computingDetection = false;
    protected int previewWidth;
    protected int previewHeight;
    protected byte[][] yuvBytes = new byte[3][];
    protected int yRowStride;
    protected int[] rgbBytes;
    protected Bitmap rgbFrameBitmap;
    protected Bitmap croppedBitmap;
    protected Bitmap cropCopyBitmap;
    protected Matrix frameToCropTransform;
    protected Matrix cropToFrameTransform;
    private Runnable postInferenceCallback;
    private Runnable imageConverter;

    protected FrameCallback frameCallback;
    protected DetectResultHelper detectResultHelper;

    protected boolean newGirlDrawed;

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.  Optionally use legacy Multibox (trained using an older version of the API)
    // or YOLO.
    protected enum DetectorMode {
        TF_OD_API, MULTIBOX, YOLO;
    }
    protected static final DetectorMode MODE = DetectorMode.TF_OD_API;
    protected static final boolean MAINTAIN_ASPECT = MODE == DetectorMode.YOLO;

    protected static final int TF_OD_API_INPUT_SIZE = 300;
//    protected static final String TF_OD_API_MODEL_FILE =
//                "file:///android_asset/ssd_mobilenet_v1_android_export.pb";
//    protected static final String TF_OD_API_LABELS_FILE = "file:///android_asset/coco_labels_list.txt";
    protected static final String TF_OD_API_MODEL_FILE =
                "file:///android_asset/spiderman_frozen_inference_graph.pb";
    protected static final String TF_OD_API_LABELS_FILE = "file:///android_asset/spiderman_labels.txt";
    protected Classifier detector;
    protected long lastProcessingTimeMs;
    protected long timestamp = 0;
    protected MultiBoxTracker tracker;
    protected byte[] luminanceCopy;

    // Minimum detection confidence to track a detection.
//    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.6f;
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.25f; // for spiderman
    private static final float MINIMUM_CONFIDENCE_MULTIBOX = 0.1f;
    private static final float MINIMUM_CONFIDENCE_YOLO = 0.25f;

    protected TapHelper tapHelper;
    private StreamingTexture backgroundTexture;
    private DisplayRotationHelper displayRotationHelper;
    private final PointCloudRenderer pointCloudRenderer = new PointCloudRenderer();
    private final PlaneRenderer planeRenderer = new PlaneRenderer();

    private SnackbarHelper snackbarHelper;
    private Activity activity;

    // Temporary matrix values
    private float[] projectionMatrixValues = new float[16];
    private float[] viewMatrixValues = new float[16];
    private Matrix4 projectionMatrix = new Matrix4();
    private Matrix4 viewMatrix = new Matrix4();

    private PointCloud3D mPointCloud3D;
    private static final int MAX_NUMBER_OF_POINTS = 60000;
    private static final int BYTES_PER_FLOAT = Float.SIZE / 8;
    private static final int FLOATS_PER_POINT = 4; // X,Y,Z,confidence.
    private static final int BYTES_PER_POINT = BYTES_PER_FLOAT * FLOATS_PER_POINT;
    private static final int INITIAL_BUFFER_POINTS = 1000;

    private Session session;
    private Context context;

    protected static AMeshLoader sMeshParser;
    protected CalcThread calcThread;
    protected Handler calcHandler;

    public DeerGirlARCoreRenderer(Context context) {
        super(context);
        init(context);
    }

    public DeerGirlARCoreRenderer(Context context, Session session) {
        super(context);
        this.session = session;
        init(context);
    }

    private void init(final Context context) {
        this.context = context;
        detectResultHelper = new DetectResultHelper();
        displayRotationHelper = new DisplayRotationHelper(context);
        calcThread = new CalcThread("calc");
        calcThread.start();
/*        calcThread.setLooperState(new CalcThread.LooperState() {
            @Override
            public void onPrepared() {
                calcHandler = new Handler(calcThread.getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg.what == CalcThread.MSG_GET_BITMAP) {
                            int w = 1080;
                            int h = 1920;
                            int b[] = new int[w*(0+h)];
                            int bt[] = new int[w*h];
                            IntBuffer ib = IntBuffer.wrap(b);
                            ib.position(0);
                            GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

                            for(int i=0, k=0; i<h; i++, k++)
                            {//remember, that OpenGL bitmap is incompatible with Android bitmap
                                //and so, some correction need.
                                for(int j=0; j<w; j++)
                                {
                                    int pix=b[i*w+j];
                                    int pb=(pix>>16)&0xff;
                                    int pr=(pix<<16)&0x00ff0000;
                                    int pix1=(pix&0xff00ff00) | pr | pb;
                                    bt[(h-k-1)*w+j]=pix1;
                                }
                            }

                            rgbFrameBitmap = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
                            frameCallback.onFrameArrived(rgbFrameBitmap);
                        }
                    }
                };
            }
        });*/
        calcHandler = new Handler(calcThread.getLooper());

        try {
            detector = TensorFlowObjectDetectionAPIModel.create(
                    context.getAssets(), TF_OD_API_MODEL_FILE, TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        tracker = new MultiBoxTracker(context);
        tracker.setDrawLocactionCallback(new MultiBoxTracker.DrawLocactionCallback() {
            @Override
            public void onLocationDraw(RectF rect, String title) {
                if (
//                        title.contains("laptop") //laptop经常识别成tv，先提高测试效率
//                        || title.contains("tv")
//                        ||
                                title.contains("handbag")
                        ) {
                    detectResultHelper.offer(new Point((int) rect.centerX(), (int) rect.centerY()));
                    //Toast.makeText(context, "Find laptop!", Toast.LENGTH_SHORT).show();
                    Log.e("fcc", "find laptop!");
                }
            }
        });
    }

    public MultiBoxTracker getTracker() {
        return tracker;
    }

    public void setFrameCallback(FrameCallback frameCallback) {
        this.frameCallback = frameCallback;
    }

    public static void setSMeshParser(AMeshLoader meshParser) {
        sMeshParser = meshParser;
    }

    public void setTapHelper(TapHelper tapHelper) {
        this.tapHelper = tapHelper;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setSnackbarHelper(SnackbarHelper snackbarHelper, Activity activity) {
        this.snackbarHelper = snackbarHelper;
        this.activity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        displayRotationHelper.onResume();

        try {
            session.resume();
        } catch (CameraNotAvailableException e) {
            // In some cases (such as another camera app launching) the camera may be given to
            // a different app instead. Handle this properly by showing a message and recreate the
            // session at the next iteration.
            Log.e("ARCoreRenderer", "Error", e);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        displayRotationHelper.onPause();

        session.pause();
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(gl, width, height);
        displayRotationHelper.onSurfaceChanged(width, height);
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        try {
            pointCloudRenderer.createOnGlThread(/*context=*/ context);
            planeRenderer.createOnGlThread(/*context=*/ context, "models/trigrid.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initScene() {
        // Create camera background
        CameraBackground plane = new CameraBackground();

        // Keep texture to field for later update
        backgroundTexture = plane.getTexture();

        // Add to scene
        getCurrentScene().addChild(plane);

        mPointCloud3D = new PointCloud3D(MAX_NUMBER_OF_POINTS, 4);
        getCurrentScene().addChild(mPointCloud3D);
        getCurrentScene().setBackgroundColor(Color.WHITE);

        // seems to be unnecessary
        getCurrentCamera().setNearPlane(0.1f);
        getCurrentCamera().setFarPlane(200);
        getCurrentCamera().setFieldOfView(37.5);

        previewWidth = 640;
        previewHeight = 480;
        croppedBitmap = Bitmap.createBitmap(CROP_SIZE, CROP_SIZE, Bitmap.Config.ARGB_8888);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        frameToCropTransform = ImageUtils.getTransformationMatrix(
                previewWidth, previewHeight,
                CROP_SIZE, CROP_SIZE, 0, MAINTAIN_ASPECT);
        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
    }

    /**
     * Process every frame update.
     */
    protected void onFrame(Frame frame, long ellapsedRealtime, double deltaTime) {

/*
        try {
            Image image = frame.acquireCameraImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] input = new byte[buffer.remaining()];
            int width = image.getWidth();
            int height = image.getHeight();
            Log.e("fcc", String.format("image format: %x, w=%d, h=%d, remaining=%d, capacity=%d",
                    image.getFormat(), width, height, buffer.remaining(), buffer.capacity()));
            buffer.get(input);
            if (rgbBytes == null) {
                ImageUtils.convertYUV420SPToARGB8888(input, image.getWidth(), image.getHeight(), rgbBytes);
            }
            image.close();

            rgbFrameBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            rgbFrameBitmap.setPixels(rgbBytes, 0, width, 0, 0, width, height);

        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }
*/

        // get current frame bitmap in non-UI thread
        // onFrame is called in non-UI thread, but calculate cost too much time will block the next onFrame
 /*       int w = 1080;
        int h = 1920;
        int b[] = new int[w*(0+h)];
        int bt[] = new int[w*h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        GLES20.glReadPixels(0, 0, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);

        for(int i=0, k=0; i<h; i++, k++)
        {//remember, that OpenGL bitmap is incompatible with Android bitmap
            //and so, some correction need.
            for(int j=0; j<w; j++)
            {
                int pix=b[i*w+j];
                int pb=(pix>>16)&0xff;
                int pr=(pix<<16)&0x00ff0000;
                int pix1=(pix&0xff00ff00) | pr | pb;
                bt[(h-k-1)*w+j]=pix1;
            }
        }*/

        // the WH of rgbFrameBitmap is not 1080*1920
//        rgbFrameBitmap = Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
//        frameCallback.onFrameArrived(rgbFrameBitmap);

        // can't call glReadPixels in another thread not sharing the opengl context
//        Message message = Message.obtain();
//        message.what = CalcThread.MSG_GET_BITMAP;
//        calcHandler.sendMessage(message);

        onImageAvailable(frame);
        processImage(frame);

    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected void onImageAvailable(Frame frame) {
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }

        Image image = null;
        try {
            image = frame.acquireCameraImage();
        } catch (NotYetAvailableException e) {
            e.printStackTrace();
        }

        if (image == null) {
            return;
        }

        final Image curImage = image;

        if (isProcessingFrame) {
            curImage.close();
            return;
        }
        isProcessingFrame = true;

        final Image.Plane[] planes = image.getPlanes();

        fillBytes(planes, yuvBytes);
        yRowStride = planes[0].getRowStride();
        final int uvRowStride = planes[1].getRowStride();
        final int uvPixelStride = planes[1].getPixelStride();

        imageConverter =
                new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.convertYUV420ToARGB8888(
                                yuvBytes[0],
                                yuvBytes[1],
                                yuvBytes[2],
                                previewWidth,
                                previewHeight,
                                yRowStride,
                                uvRowStride,
                                uvPixelStride,
                                rgbBytes);
                    }
                };

        postInferenceCallback =
                new Runnable() {
                    @Override
                    public void run() {
                        curImage.close();
                        isProcessingFrame = false;
                    }
                };

    }

    protected void readyForNextImage() {
        if (postInferenceCallback != null) {
            postInferenceCallback.run();
        }
    }

    protected void processImage(Frame frame) {
        ++timestamp;
        final long currTimestamp = timestamp;
        byte[] originalLuminance = getLuminance();
        // remove lost tracking object
        tracker.onFrame(
                previewWidth,
                previewHeight,
                getLuminanceStride(),
                sensorOrientation,
                originalLuminance,
                timestamp);
        frameCallback.onTrackerUpdate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }

        computingDetection = true;

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        if (luminanceCopy == null) {
            luminanceCopy = new byte[originalLuminance.length];
        }
        System.arraycopy(originalLuminance, 0, luminanceCopy, 0, originalLuminance.length);
        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);

        frameCallback.onFrameArrived(croppedBitmap);

        calcHandler.post(new Runnable() {
            @Override
            public void run() {

                final long startTime = SystemClock.uptimeMillis();
                final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                final Canvas canvas = new Canvas(cropCopyBitmap);
                final Paint paint = new Paint();
                paint.setColor(Color.RED);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0f);

                float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                switch (MODE) {
                    case TF_OD_API:
                        minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                        break;
                    case MULTIBOX:
                        minimumConfidence = MINIMUM_CONFIDENCE_MULTIBOX;
                        break;
                    case YOLO:
                        minimumConfidence = MINIMUM_CONFIDENCE_YOLO;
                        break;
                }

                final List<Classifier.Recognition> mappedRecognitions =
                        new LinkedList<Classifier.Recognition>();

                for (final Classifier.Recognition result : results) {
                    final RectF location = result.getLocation();
                    if (location != null && result.getConfidence() >= minimumConfidence) {
                        canvas.drawRect(location, paint);

                        cropToFrameTransform.mapRect(location);
                        result.setLocation(location);
                        mappedRecognitions.add(result);
                    }
                }

                tracker.trackResults(mappedRecognitions, luminanceCopy, currTimestamp);
                frameCallback.onTrackerUpdate();
                computingDetection = false;
            }
        });
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session);

        session.setCameraTextureName(backgroundTexture.getTextureId());

        Frame frame = null;
        try {
            frame = session.update();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }
        Camera camera = frame.getCamera();

        // If not tracking, don't draw 3d objects.
        if (camera.getTrackingState() == TrackingState.PAUSED) {
            return;
        }

        onFrame(frame, ellapsedRealtime, deltaTime);

        // Get projection matrix.
        float[] projmtx = new float[16];
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);

        // Get camera matrix and draw.
        float[] viewmtx = new float[16];
        camera.getViewMatrix(viewmtx, 0);

        // draw PointCloud by rajawali, points are small red
        PointCloud pointCloud = frame.acquirePointCloud();
        FloatBuffer points = pointCloud.getPoints();
        mPointCloud3D.updateCloud(points.remaining() / FLOATS_PER_POINT, points);
        Pose pose = camera.getPose();
        float[] matrixValues = new float[16];
        Matrix4 matrix4 = new Matrix4(matrixValues);
        pose.toMatrix(matrixValues, 0);
        mPointCloud3D.setPosition(matrix4.getTranslation());
        mPointCloud3D.setOrientation(new Quaternion().fromMatrix(matrix4).conjugate());

        // draw PointCloud by opengl, points are big blue
        pointCloudRenderer.update(pointCloud);
        if (DeerGirlActivity.DEBUG) {
            pointCloudRenderer.draw(viewmtx, projmtx);
        }

        // draw plane by opengl, planes are kept in AR memory after been searched
        camera.getProjectionMatrix(projmtx, 0, 0.1f, 100.0f);
        // Visualize planes.
        if (DeerGirlActivity.DEBUG) {
            planeRenderer.drawPlanes(
                    session.getAllTrackables(Plane.class), camera.getDisplayOrientedPose(), projmtx);
        }

        // Update projection matrix.
        camera.getProjectionMatrix(projectionMatrixValues, 0, 0.1f, 100.0f);
        projectionMatrix.setAll(projectionMatrixValues);

        getCurrentScene().getCamera().setProjectionMatrix(projectionMatrix);

        // Update camera matrix.
        camera.getViewMatrix(viewMatrixValues, 0);
        viewMatrix.setAll(viewMatrixValues).inverse();

        getCurrentScene().getCamera().setRotation(viewMatrix);
        getCurrentScene().getCamera().setPosition(viewMatrix.getTranslation());

        // Check if we detected at least one plane. If so, hide the loading message.
        Log.e("fcc", "plane size="+session.getAllTrackables(Plane.class).size());
        if (snackbarHelper.isShowing()) {
            for (Plane plane : session.getAllTrackables(Plane.class)) {
                if (plane.getType() == com.google.ar.core.Plane.Type.HORIZONTAL_UPWARD_FACING
                        && plane.getTrackingState() == TrackingState.TRACKING) {
                    snackbarHelper.hide(activity);
                    break;
                }
            }
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    public interface FrameCallback {
        void onFrameArrived(Bitmap bitmap);
        void onTrackerUpdate();
    }
}
