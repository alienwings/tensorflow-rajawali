package org.rajawali3d.examples.examples.fcc;

import android.content.Context;
import android.view.MotionEvent;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.jetbrains.annotations.NotNull;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;

public class SkeletalBlendDemo1Render extends DeerGirlARCoreRenderer {

    private TapHelper tapHelper;

    private DirectionalLight          mLight;
    private SkeletalAnimationObject3D mObject;
    private SkeletalAnimationSequence mSequenceWalk;
    private SkeletalAnimationSequence mSequenceIdle;
    private SkeletalAnimationSequence mSequenceArmStretch;
    private SkeletalAnimationSequence mSequenceBend;

    public SkeletalBlendDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }


    @Override
    protected void initScene() {
        super.initScene();
        mLight = new DirectionalLight(0, -0.2f, -1.0f); // set the direction
        mLight.setColor(1.0f, 1.0f, .8f);
        mLight.setPower(1);

        getCurrentScene().addLight(mLight);
        getCurrentCamera().setZ(8);

        try {
            LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this,
                    R.raw.ingrid_mesh);
            meshParser.parse();

            LoaderMD5Anim animParser = new LoaderMD5Anim("idle", this,
                    R.raw.ingrid_idle);
            animParser.parse();

            mSequenceIdle = (SkeletalAnimationSequence) animParser
                    .getParsedAnimationSequence();

            animParser = new LoaderMD5Anim("walk", this, R.raw.ingrid_walk);
            animParser.parse();

            mSequenceWalk = (SkeletalAnimationSequence) animParser
                    .getParsedAnimationSequence();

            animParser = new LoaderMD5Anim("armstretch", this,
                    R.raw.ingrid_arm_stretch);
            animParser.parse();

            mSequenceArmStretch = (SkeletalAnimationSequence) animParser
                    .getParsedAnimationSequence();

            animParser = new LoaderMD5Anim("bend", this, R.raw.ingrid_bend);
            animParser.parse();

            mSequenceBend = (SkeletalAnimationSequence) animParser
                    .getParsedAnimationSequence();

            mObject = (SkeletalAnimationObject3D) meshParser
                    .getParsedAnimationObject();
            mObject.setAnimationSequence(mSequenceIdle);
            mObject.setFps(24);
            mObject.setScale(.8f);
            mObject.play();

//            mObject.transitionToAnimationSequence(mSequenceIdle, 1000);
//            mObject.transitionToAnimationSequence(mSequenceWalk, 1000);
//            mObject.transitionToAnimationSequence(mSequenceArmStretch, 1000);
            mObject.transitionToAnimationSequence(mSequenceBend, 1000);

            getCurrentScene().addChild(mObject);
        } catch (ParsingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFrame(@NotNull Frame frame, long ellapsedRealtime, double deltaTime) {
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
        PointCloud pointCloud = frame.acquirePointCloud();

        // Do something

        // And finally call this
        pointCloud.release();
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
}
