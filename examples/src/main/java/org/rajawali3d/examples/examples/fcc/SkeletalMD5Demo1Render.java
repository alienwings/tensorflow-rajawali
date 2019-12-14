package org.rajawali3d.examples.examples.fcc;

import android.content.Context;
import android.os.AsyncTask;
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
import org.rajawali3d.loader.AMeshLoader;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;

public class SkeletalMD5Demo1Render extends DeerGirlARCoreRenderer {

    private DirectionalLight mLight;
    private SkeletalAnimationObject3D mObject;

    public SkeletalMD5Demo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    public SkeletalMD5Demo1Render(Context context) {
        super(context);
    }

    @Override
    protected void initScene() {
        super.initScene();
        mLight = new DirectionalLight(0, -0.2f, -1.0f); // set the direction
        mLight.setColor(1.0f, 1.0f, 1.0f);
        mLight.setPower(2);

        getCurrentScene().addLight(mLight);
        getCurrentCamera().setY(1);
        getCurrentCamera().setZ(6);

        if (false) {
            try {
                AMeshLoader meshParser = null;
                if (sMeshParser != null) {
                    meshParser = sMeshParser;
                    ((LoaderMD5Mesh) meshParser).checkCapabilitiesIfNeed();

                    // todo
                    // it seems that parse must in initScene
                    // if parse earlier, nothing can be seen
                    meshParser.parse();
                } else {
                    meshParser = new LoaderMD5Mesh(this,
                            R.raw.boblampclean_mesh);
//                ((LoaderMD5Mesh) meshParser).checkCapabilitiesIfNeed();
                    meshParser.parse();
                }

                LoaderMD5Anim animParser = new LoaderMD5Anim("attack2", this,
                        R.raw.boblampclean_anim);
                animParser.parse();

                SkeletalAnimationSequence sequence = (SkeletalAnimationSequence) animParser
                        .getParsedAnimationSequence();

                mObject = (SkeletalAnimationObject3D) ((LoaderMD5Mesh) meshParser).getParsedAnimationObject();
                mObject.setAnimationSequence(sequence);
                mObject.setScale(.04f);
                mObject.play();

                getCurrentScene().addChild(mObject);
            } catch (ParsingException e) {
                e.printStackTrace();
            }
        }

        new ModelTask(this).execute();

    }

    private class ModelTask extends AsyncTask {

        DeerGirlARCoreRenderer renderer;

        AMeshLoader meshParser;
        LoaderMD5Anim animParser;

        public ModelTask(DeerGirlARCoreRenderer renderer) {
            this.renderer = renderer;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                meshParser = new LoaderMD5Mesh(renderer, R.raw.boblampclean_mesh);
//                ((LoaderMD5Mesh) meshParser).checkCapabilitiesIfNeed();
                meshParser.parse();

                animParser = new LoaderMD5Anim("attack2", renderer,
                        R.raw.boblampclean_anim);
                animParser.parse();

            } catch (ParsingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            SkeletalAnimationSequence sequence = (SkeletalAnimationSequence) animParser
                    .getParsedAnimationSequence();
            mObject = (SkeletalAnimationObject3D) ((LoaderMD5Mesh) meshParser).getParsedAnimationObject();
            mObject.setAnimationSequence(sequence);
            mObject.setScale(.04f);
            mObject.play();

            getCurrentScene().addChild(mObject);
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
