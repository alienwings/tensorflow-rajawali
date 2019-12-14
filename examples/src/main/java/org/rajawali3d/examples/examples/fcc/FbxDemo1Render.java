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
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.fbx.LoaderFBX;
import org.rajawali3d.math.vector.Vector3;

public class FbxDemo1Render extends DeerGirlARCoreRenderer {

    private Animation3D mAnim;
    private TapHelper tapHelper;

    private Object3D object3D;

    public FbxDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    @Override
    protected void initScene() {
        super.initScene();
        mAnim = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
        mAnim.setDurationMilliseconds(16000);
        mAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(mAnim);

        try {
            // -- Model by Sampo Rask
            // (http://www.blendswap.com/blends/characters/low-poly-rocks-character/)
            LoaderFBX parser = new LoaderFBX(this,
                    R.raw.lowpolyrocks_character_blendswap);
            parser.parse();
            object3D = parser.getParsedObject();
            object3D.setY(-.5f);
            object3D.setPosition(0.0, 0.0, -0.5);
            getCurrentScene().addChild(object3D);
            mAnim.setTransformable3D(object3D);
            mAnim.play();
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
                Object3D newDroid = object3D.clone();
                newDroid.setY(-.5f);
                newDroid.setPosition(translation[0], translation[1], translation[2]);
                getCurrentScene().addChild(newDroid);
                mAnim.setTransformable3D(newDroid);
                mAnim.play();
                break;
            }
        }
    }
}
