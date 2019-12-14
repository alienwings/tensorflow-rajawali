package org.rajawali3d.examples.examples.fcc;

import android.content.Context;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;

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
import org.rajawali3d.animation.SplineTranslateAnimation3D;
import org.rajawali3d.curves.CatmullRomCurve3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.examples.examples.interactive.planes.PlanesGalore;
import org.rajawali3d.examples.examples.interactive.planes.PlanesGaloreMaterialPlugin;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Planes2000Demo1Render extends DeerGirlARCoreRenderer {

    private TapHelper tapHelper;

    private long mStartTime;
    private Material mMaterial;
    private PlanesGaloreMaterialPlugin mMaterialPlugin;

    public Planes2000Demo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        super.onRenderSurfaceCreated(config, gl, width, height);
        mStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onRender(long ellapsedRealtime, double deltaTime) {
        super.onRender(ellapsedRealtime, deltaTime);
        mMaterial.setTime((System.currentTimeMillis() - mStartTime) / 1000f);
        mMaterialPlugin.setCameraPosition(getCurrentCamera().getPosition());
    }

    @Override
    protected void initScene() {
        super.initScene();
        DirectionalLight light = new DirectionalLight(0, 0, 1);

        getCurrentScene().addLight(light);
        getCurrentCamera().setPosition(0, 0, -16);

        final PlanesGalore planes = new PlanesGalore();
        mMaterial = planes.getMaterial();
        mMaterial.setColorInfluence(0);
        try {
            mMaterial.addTexture(new Texture("flickrPics", R.drawable.flickrpics));
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }

        mMaterialPlugin = planes.getMaterialPlugin();

        planes.setDoubleSided(true);
        planes.setZ(4);
        getCurrentScene().addChild(planes);

        Object3D empty = new Object3D();
        getCurrentScene().addChild(empty);

        CatmullRomCurve3D path = new CatmullRomCurve3D();
        path.addPoint(new Vector3(-4, 0, -20));
        path.addPoint(new Vector3(2, 1, -10));
        path.addPoint(new Vector3(-2, 0, 10));
        path.addPoint(new Vector3(0, -4, 20));
        path.addPoint(new Vector3(5, 10, 30));
        path.addPoint(new Vector3(-2, 5, 40));
        path.addPoint(new Vector3(3, -1, 60));
        path.addPoint(new Vector3(5, -1, 70));

        final SplineTranslateAnimation3D anim = new SplineTranslateAnimation3D(path);
        anim.setDurationMilliseconds(20000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        anim.setTransformable3D(getCurrentCamera());
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        getCurrentScene().registerAnimation(anim);
        anim.play();

        getCurrentCamera().setLookAt(new Vector3(0, 0, 30));
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

                break;
            }
        }
    }
}
