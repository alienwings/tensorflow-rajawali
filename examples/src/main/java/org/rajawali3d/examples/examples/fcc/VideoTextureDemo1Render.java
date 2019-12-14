package org.rajawali3d.examples.examples.fcc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Point;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.jetbrains.annotations.NotNull;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Plane;

import java.util.List;

public class VideoTextureDemo1Render extends DeerGirlARCoreRenderer {

    private TapHelper tapHelper;
    private MediaPlayer mMediaPlayer;
    private StreamingTexture mVideoTexture;
    private ReflectionUtil reflectionUtil = new ReflectionUtil();

    public VideoTextureDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    private void addVideo(Vector3 vector3) {
        try {
            Object3D object3D = new Cube(2.0f);
            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.setSpecularMethod(new SpecularMethod.Phong());
            object3D.setMaterial(material);
            object3D.setColor(0xff99C224);
            //getCurrentScene().addChild(object3D);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        mMediaPlayer = MediaPlayer.create(getContext(),
                R.raw.sintel_trailer_480p);
        mMediaPlayer.setLooping(true);

        mVideoTexture = new StreamingTexture("sintelTrailer", mMediaPlayer);
        Material material = new Material();
        material.setColorInfluence(0);
        try {
            material.addTexture(mVideoTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        float scale = 0.5f;
        Plane screen = new Plane(3*scale, 2*scale, (int)(2*scale), (int)(2*scale), Vector3.Axis.Z);
        screen.setMaterial(material);
        screen.setX(vector3.x);
        screen.setY(vector3.y);
        screen.setZ(vector3.z);
        getCurrentScene().addChild(screen);

        getCurrentCamera().enableLookAt();
        getCurrentCamera().setLookAt(0, 0, 0);

        // -- animate the spot light

//        TranslateAnimation3D lightAnim = new TranslateAnimation3D(
//                new Vector3(-3, 3, 10), // from
//                new Vector3(3, 1, 3)); // to
//        lightAnim.setDurationMilliseconds(5000);
//        lightAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
//        lightAnim.setTransformable3D(pointLight);
//        lightAnim.setInterpolator(new AccelerateDecelerateInterpolator());
//        getCurrentScene().registerAnimation(lightAnim);
//        lightAnim.play();


        // -- animate the camera
        //环绕浮动效果
//        EllipticalOrbitAnimation3D camAnim = new EllipticalOrbitAnimation3D(
//                new Vector3(3, 2, 10), new Vector3(1, 0, 8), 0, 359);
//        camAnim.setDurationMilliseconds(20000);
//        camAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
//        camAnim.setTransformable3D(getCurrentCamera());
//        getCurrentScene().registerAnimation(camAnim);
//        camAnim.play();

        mMediaPlayer.start();
    }


    @Override
    protected void initScene() {
        super.initScene();
        PointLight pointLight = new PointLight();
        pointLight.setPower(1);
        pointLight.setPosition(-1, 1, 4);

        getCurrentScene().addLight(pointLight);
        getCurrentScene().setBackgroundColor(0xff040404);

//        addVideo(new Vector3(0.1f, -0.2f, 0.5f));
    }

    @Override
    protected void onFrame(Frame frame, long ellapsedRealtime, double deltaTime) {
        super.onFrame(frame, ellapsedRealtime, deltaTime);

        android.graphics.Point point = detectResultHelper.poll();
        if (point != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
            onTap(frame, point);
        }
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        if (mVideoTexture != null) {
            mVideoTexture.update();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null)
            mMediaPlayer.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMediaPlayer != null)
            mMediaPlayer.start();
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surfaceTexture) {
        super.onRenderSurfaceDestroyed(surfaceTexture);
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
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
            if (trackable instanceof com.google.ar.core.Plane
                    && ((com.google.ar.core.Plane) trackable).isPoseInPolygon(hit.getHitPose())
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

                    Vector3 vector3 = null;
                    //初始化并播放动画
                    if (trackable instanceof com.google.ar.core.Plane && ((com.google.ar.core.Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                        com.google.ar.core.Plane plane = (com.google.ar.core.Plane) trackable;
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

                    addVideo(vector3);

                    Log.e("fcc", "set newGirlDrawed true");
                }
                break;
            }
        }
    }
}
