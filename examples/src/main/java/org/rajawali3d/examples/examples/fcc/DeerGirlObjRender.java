package org.rajawali3d.examples.examples.fcc;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.jetbrains.annotations.NotNull;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;

import java.util.ArrayList;
import java.util.List;

public class DeerGirlObjRender extends DeerGirlARCoreRenderer implements OnObjectPickedListener {

    private TapHelper tapHelper;
    private List<Object3D> object3DList = new ArrayList<Object3D>();
    private List<Object3D> newGirlBody = new ArrayList<Object3D>();
    private ObjectColorPicker objectPicker;
    float scale = 0.05f;
    private ReflectionUtil reflectionUtil = new ReflectionUtil();
    private AnimExecutor animExecutor = new AnimExecutor();

    public DeerGirlObjRender(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
        objectPicker = new ObjectColorPicker(this);
        objectPicker.setOnObjectPickedListener(this);
    }

    private void girlScene(LoaderOBJ objParser) {
        int num = objParser.getParsedObject().getNumChildren();
        for (int i = 1; i < num; i++) {
            Object3D droid = objParser.getParsedObject().getChildAt(i);
//            droid.setScale(0.1);
            droid.setScale(scale);
            droid.setPosition(0.0, 0.0, -0.8); // in the middle
            droid.setPosition(0.3, 0.3, -0.1); // 在摄像机右侧后方
//            droid.setPosition(0.5, 0.5, 0.5); // in the sky
            getCurrentScene().addChild(droid); //放置一个初始的女孩
            object3DList.add(droid);
            objectPicker.registerObject(droid);

            //这是个例子，初始化动画
//            animExecutor.addTransAnim(getCurrentScene(), droid, new Vector3(0, 0, 0));
//            animExecutor.addScaleAnim(getCurrentScene(), droid);
        }
    }


    @Override
    protected void initScene() {
        super.initScene();

        //  Spawn droid object in front of you
        LoaderOBJ objParser = new LoaderOBJ(this, R.raw.multiobjects_obj);
        try {
            objParser.parse();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        girlScene(objParser);

        Log.e("fcc", "DeerGirlObjRender initScene");
    }

    public void setFrameCallback(FrameCallback frameCallback) {
        this.frameCallback = frameCallback;
    }

    @Override
    public void onFrame(@NotNull Frame frame, long ellapsedRealtime, double deltaTime) {

        //包含物体识别和跟踪
        super.onFrame(frame, ellapsedRealtime, deltaTime);

        //********************* Phase 1 begin ***********************
        // original code, use touch event
        MotionEvent tap = tapHelper.poll();

        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            onTap(frame, tap);
            // do object tracking
            objectPicker.getObjectAt(tap.getX(), tap.getY());

            //这是一个例子，点击任何位置，都播放动画
            //playAnim();
        }

        /*
         * Light estimating.
         */

        // Compute lighting from average intensity of the image.
        // The first three components are color scaling factors.
        // The last one is the average pixel intensity in gamma space.
        float[] colorCorrectionRgba = new float[4];

        /*
         * Process point cloud
         */

        // Process point cloud
        // Application is responsible for releasing the point cloud resources after
        // using it.
        //PointCloud pointCloud = frame.acquirePointCloud();

        // Do something

        // And finally call this
        //pointCloud.release();
        //********************* Phase 1 end ***********************


        //********************* Phase 2 begin ***********************
        // use object detector
        android.graphics.Point point = detectResultHelper.poll();
        if (point != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            onTap(frame, point);
        }
        //********************* Phase 2 end ***********************

    }

    /**
     * 物体检测的添加女孩
     * @param frame
     * @param point
     */
    private void onTap(Frame frame, android.graphics.Point point) {
        List<HitResult> hitResults = frame.hitTest(point.x, point.y);
        if (hitResults == null || hitResults.size() == 0) {
            Log.e("fcc", "onTap: NO plane hit");
            return;
        }
        Log.e("fcc", "onTap: HAS plane hit! newGirDrawed="+newGirlDrawed);
        for (HitResult hit : hitResults) {

            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();

            // Creates an anchor if a plane or an oriented point was hit.
            if (trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                    || trackable instanceof Point
                    && ((Point) trackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {

                if (!newGirlDrawed) {
                    newGirlDrawed = true;

                    // Create anchor at touched place
                    Anchor anchor = hit.createAnchor();
//                val rot = FloatArray(4)
//                anchor.pose.getRotationQuaternion(rot, 0)
                    float[] translation = new float[3];
                    anchor.getPose().getTranslation(translation, 0);

                    // Spawn new droid object at anchor position
                    List<Object3D> tmpList = new ArrayList<Object3D>();
                    for (Object3D object3D : object3DList) {
                        Object3D newDroid = object3D.clone();
                        newDroid.setPosition(translation[0], translation[1], translation[2]);
                        newDroid.setScale(scale / 2);
                        tmpList.add(newDroid);
                        getCurrentScene().addChild(newDroid);
                    }

                    if (newGirlBody != null) {
                        newGirlBody = tmpList;
                    }

                    Vector3 vector3 = null;
                    //初始化并播放动画
                    if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                        Plane plane = (Plane) trackable;
                        Pose centerPos = plane.getCenterPose();
                        float[] centerArray = new float[3];
                        centerPos.getTranslation(centerArray, 0);
                        vector3 = new Vector3(centerArray[0], centerArray[1], centerArray[2]);
                        Log.e("fcc", "before anim: Plane");
                    } else if (trackable instanceof Point &&
                            ((Point) trackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {
                        Point point2 = (Point) trackable;
                        Pose pointPos = (Pose) reflectionUtil.invokeMethod(point2, "getPose");
                        if (pointPos != null) {
                            float[] centerArray = new float[3];
                            pointPos.getTranslation(centerArray, 0);
                            vector3 = new Vector3(centerArray[0], centerArray[1], centerArray[2]);
                            Log.e("fcc", "before anim: Pose");
                        }
                    }

                    float tmpScale = 0.001f;
                    if (vector3 != null) {
                        for (Object3D object3D : object3DList) {
                            //测试动画
                            //平移+缩放：飞出效果，进入trackable
//                            animExecutor.addTransAnim(getCurrentScene(), object3D, vector3);
                            //平移+缩放：飞入效果，先进入trackable，再从trackable飞出来，倒放效果
//                            animExecutor.addTransScaleAnim(getCurrentScene(), object3D, vector3, tmpScale, true);
                            //平移+缩放+旋转：飞入效果
                            animExecutor.addTransScaleRotateAnim(getCurrentScene(), object3D,
                                    vector3, tmpScale, true, new IAnimationListener() {
                                        @Override
                                        public void onAnimationEnd(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animation animation) {
                                            Log.e("fcc", "anim repeat");
                                        }

                                        @Override
                                        public void onAnimationStart(Animation animation) {

                                        }

                                        @Override
                                        public void onAnimationUpdate(Animation animation, double interpolatedTime) {
                                            //可用，但卡帧严重
/*                                            for (Object3D part1 : object3DList) {
                                                for (Object3D part2 : newGirlBody) {
                                                    IBoundingVolume boundingBox1 = part1.getGeometry().getBoundingBox();
                                                    boundingBox1.transform(part1.getModelMatrix());

                                                    IBoundingVolume boundingBox2 = part2.getGeometry().getBoundingBox();
                                                    boundingBox2.transform(part2.getModelMatrix());

                                                    boolean intersect = boundingBox1.intersectsWith(boundingBox2);
                                                    if (intersect) {
                                                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(getContext(), "Girls meet!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                }
                                            }*/
                                        }
                                    });
                        }
                        animExecutor.playAnim();
                    }

                    Log.e("fcc", "set newGirlDrawed true");
                }
                break;
            }
        }
    }

    /**
     * 触摸就添加女孩
     * @param frame
     * @param tap
     */
    private void onTap(Frame frame, MotionEvent tap) {

        List<HitResult>hitResults = frame.hitTest(tap);
        if (hitResults == null || hitResults.size() == 0) {
            Log.e("fcc", "onTap: NO plane hit");
            return;
        }
        Log.e("fcc", "onTap: HAS plane hit!");
        for (HitResult hit : hitResults) {

            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();

            // Creates an anchor if a plane or an oriented point was hit.
            if (trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                    || trackable instanceof Point
                    && ((Point) trackable).getOrientationMode() == Point.OrientationMode.ESTIMATED_SURFACE_NORMAL) {

                // Create anchor at touched place
                Anchor anchor = hit.createAnchor();
//                val rot = FloatArray(4)
//                anchor.pose.getRotationQuaternion(rot, 0)
                float[] translation = new float[3];
                anchor.getPose().getTranslation(translation, 0);

                // Spawn new droid object at anchor position
                for (Object3D object3D : object3DList) {
                    Object3D newDroid = object3D.clone();
                    newDroid.setPosition(translation[0], translation[1], translation[2]);
                    newDroid.setScale(scale / 2);
                    objectPicker.registerObject(newDroid);
                    getCurrentScene().addChild(newDroid);
                }

                newGirlDrawed = true;
                break;
            }
        }
    }


    @Override
    public void onObjectPicked(@NonNull Object3D object) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "girl is picked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNoObjectPicked() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "girl is NOT picked", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
