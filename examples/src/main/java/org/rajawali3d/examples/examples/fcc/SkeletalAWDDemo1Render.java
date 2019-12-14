package org.rajawali3d.examples.examples.fcc;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.jetbrains.annotations.NotNull;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.IAnimationListener;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

public class SkeletalAWDDemo1Render extends DeerGirlARCoreRenderer {

    private TapHelper tapHelper;
    private DirectionalLight mLight;
    private SkeletalAnimationObject3D mObject;

    float scale = 0.01f;
    ReflectionUtil reflectionUtil;
    AnimExecutor animExecutor = new AnimExecutor();

    public SkeletalAWDDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }


    @Override
    protected void initScene() {
        super.initScene();

        //去掉光照，人物的阴影没有了，更亮
//        mLight = new DirectionalLight(0, -0.2f, -1.0f); // set the direction
//        mLight.setColor(1.0f, 1.0f, 1.0f);
//        mLight.setPower(2);
//        getCurrentScene().addLight(mLight);

        getCurrentCamera().setY(1);
        getCurrentCamera().setZ(6);

        try {
            final LoaderAWD parser =
                    new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.boblampclean_anim_awd);

            parser.parse();
            mObject = (SkeletalAnimationObject3D) parser.getParsedObject();
            mObject.setPosition(0, 0, -0.8);
            mObject.rotate(Vector3.Y, -90.0);

            mObject.setAnimationSequence(0);
            mObject.setScale(scale);
            mObject.play();

            getCurrentScene().addChild(mObject);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFrame(@NotNull Frame frame, long ellapsedRealtime, double deltaTime) {

        //包含物体识别和跟踪
        super.onFrame(frame, ellapsedRealtime, deltaTime);

        MotionEvent tap = tapHelper.poll();

        if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            onTap(frame, tap);
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
//        PointCloud pointCloud = frame.acquirePointCloud();

        // Do something

        // And finally call this
//        pointCloud.release();

        //********************* Phase 2 begin ***********************
        // use object detector
        android.graphics.Point point = detectResultHelper.poll();
        if (point != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            onTap(frame, point);
        }
        //********************* Phase 2 end ***********************
    }

    private void onTap(Frame frame, MotionEvent tap) {
        for (HitResult hit : frame.hitTest(tap)) {

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
                SkeletalAnimationObject3D newDroid = (SkeletalAnimationObject3D) mObject.clone();
                newDroid.setPosition(translation[0], translation[1], translation[2]);

                newDroid.setAnimationSequence(0);
                newDroid.setScale(.04f);
                newDroid.play();
                getCurrentScene().addChild(newDroid);
                break;
            }
        }
    }

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
                    SkeletalAnimationObject3D newDroid = (SkeletalAnimationObject3D) mObject.clone();
                    newDroid.setPosition(translation[0], translation[1], translation[2]);
                    newDroid.setScale(scale / 2);
                    getCurrentScene().addChild(newDroid);

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
                        //测试动画
                        //平移+缩放：飞出效果，进入trackable
//                            animExecutor.addTransAnim(getCurrentScene(), object3D, vector3);
                        //平移+缩放：飞入效果，先进入trackable，再从trackable飞出来，倒放效果
//                            animExecutor.addTransScaleAnim(getCurrentScene(), object3D, vector3, tmpScale, true);
                        //平移+缩放+旋转：飞入效果
                        animExecutor.addTransScaleRotateAnim(getCurrentScene(), mObject,
                                vector3, tmpScale, true, null);
                        animExecutor.playAnim();
                    }

                    Log.e("fcc", "set newGirlDrawed true");
                }
                break;
            }
        }
    }
}
