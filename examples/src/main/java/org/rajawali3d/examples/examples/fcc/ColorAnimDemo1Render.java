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
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.ColorAnimation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AlphaMapTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;

public class ColorAnimDemo1Render extends DeerGirlARCoreRenderer {

    private TapHelper tapHelper;


    public ColorAnimDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    @Override
    protected void initScene() {
        super.initScene();
        //
        // -- First cube
        //

        Material material1 = new Material();

        Cube cube1 = new Cube(1);
        cube1.setMaterial(material1);
        cube1.setTransparent(true);
        cube1.setX(-1);
        getCurrentScene().addChild(cube1);

        Animation3D anim = new ColorAnimation3D(0xaaff1111, 0xffffff11);
        anim.setTransformable3D(cube1);
        anim.setDurationMilliseconds(2000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
        anim.setTransformable3D(cube1);
        anim.setDurationMilliseconds(6000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        //
        // -- second cube
        //

        Material material2 = new Material();
        try {
            AlphaMapTexture alphaTex = new AlphaMapTexture("camdenTown", R.drawable.camden_town_alpha);
            alphaTex.setInfluence(.5f);
            material2.addTexture(alphaTex);
            material2.setColorInfluence(0);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        material2.setColorInfluence(.5f);

        Cube cube2 = new Cube(1);
        cube2.setMaterial(material2);
        cube2.setX(1);
        cube2.setDoubleSided(true);
        getCurrentScene().addChild(cube2);

        anim = new ColorAnimation3D(0xaaff1111, 0xff0000ff);
        anim.setTransformable3D(cube2);
        anim.setDurationMilliseconds(2000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -359);
        anim.setTransformable3D(cube2);
        anim.setDurationMilliseconds(6000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        getCurrentCamera().setPosition(0, 4, 8);
        getCurrentCamera().setLookAt(0, 0, 0);
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

    private void cloneObjects(float[]  translation) {
        Material material1 = new Material();

        Cube cube1 = new Cube(1);
        cube1.setMaterial(material1);
        cube1.setTransparent(true);
        cube1.setX(-1);
        cube1.setPosition(translation[0], translation[1], translation[2]);
        getCurrentScene().addChild(cube1);

        Animation3D anim = new ColorAnimation3D(0xaaff1111, 0xffffff11);
        anim.setTransformable3D(cube1);
        anim.setDurationMilliseconds(2000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 359);
        anim.setTransformable3D(cube1);
        anim.setDurationMilliseconds(6000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        //
        // -- second cube
        //

        Material material2 = new Material();
        try {
            AlphaMapTexture alphaTex = new AlphaMapTexture("camdenTown", R.drawable.camden_town_alpha);
            alphaTex.setInfluence(.5f);
            material2.addTexture(alphaTex);
            material2.setColorInfluence(0);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        material2.setColorInfluence(.5f);

        Cube cube2 = new Cube(1);
        cube2.setMaterial(material2);
        cube2.setX(1);
        cube2.setPosition(translation[0], translation[1], translation[2]);
        cube2.setDoubleSided(true);
        getCurrentScene().addChild(cube2);

        anim = new ColorAnimation3D(0xaaff1111, 0xff0000ff);
        anim.setTransformable3D(cube2);
        anim.setDurationMilliseconds(2000);
        anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        anim = new RotateOnAxisAnimation(Vector3.Axis.Y, -359);
        anim.setTransformable3D(cube2);
        anim.setDurationMilliseconds(6000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();
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
                cloneObjects(translation);
                break;
            }
        }
    }
}
