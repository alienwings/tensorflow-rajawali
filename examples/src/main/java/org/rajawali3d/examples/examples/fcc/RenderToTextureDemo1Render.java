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
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.PostProcessingManager;
import org.rajawali3d.postprocessing.passes.RenderPass;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.scene.Scene;

import java.util.Random;

public class RenderToTextureDemo1Render extends DeerGirlARCoreRenderer {

    private PostProcessingManager mEffects;
    private Scene                 mOtherScene;
    private Object3D mSphere;
    private ATexture              mCurrentTexture;
    private TapHelper tapHelper;

    public RenderToTextureDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    @Override
    public void initScene() {
        super.initScene();
        //
        // -- Create the scene that we are going to use for
        //    off-screen rendering
        //

        DirectionalLight light = new DirectionalLight();
        light.setLookAt(0, 0, -1);
        light.enableLookAt();
        light.setPower(1);
        getCurrentScene().setBackgroundColor(0xdfae74);
        getCurrentScene().addLight(light);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());

        getCurrentCamera().setZ(10);

        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            Cube cube = new Cube(1);
            cube.setPosition(-5 + random.nextFloat() * 10,
                    -5 + random.nextFloat() * 10, random.nextFloat() * -10);
            cube.setMaterial(material);
            cube.setColor(0x666666 + random.nextInt(0x999999));
            getCurrentScene().addChild(cube);

            Vector3 randomAxis = new Vector3(random.nextFloat(),
                    random.nextFloat(), random.nextFloat());
            randomAxis.normalize();

            RotateOnAxisAnimation anim = new RotateOnAxisAnimation(randomAxis,
                    360);
            anim.setTransformable3D(cube);
            anim.setDurationMilliseconds(3000 + (int) (random.nextDouble() * 5000));
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            getCurrentScene().registerAnimation(anim);
            anim.play();
        }

        //
        // -- Create the scene that will contain an object
        //    that uses the rendered to texture
        //

        mOtherScene = new Scene(this);
        mOtherScene.setBackgroundColor(0xffffff);
        mOtherScene.addLight(light);

        Material cubeMaterial = new Material();
        cubeMaterial.enableLighting(true);
        cubeMaterial.setColorInfluence(0);
        cubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());

        mSphere = new Cube(1);
        mSphere.setMaterial(cubeMaterial);
        mOtherScene.addChild(mSphere);

        Vector3 axis = new Vector3(1, 1, 0);
        axis.normalize();

        RotateOnAxisAnimation anim = new RotateOnAxisAnimation(axis, 360);
        anim.setTransformable3D(mSphere);
        anim.setDurationMilliseconds(10000);
        anim.setRepeatMode(Animation.RepeatMode.INFINITE);
        getCurrentScene().registerAnimation(anim);
        anim.play();

        //
        // -- Set up the post processing manager with the required texture size
        //

        mEffects = new PostProcessingManager(this, 400, 400);
        RenderPass renderPass = new RenderPass(getCurrentScene(),
                getCurrentCamera(), 0);
        mEffects.addPass(renderPass);

        //
        // -- Other effect passes could be added here
        //

        switchScene(mOtherScene);
    }

    @Override
    public void onRender(final long ellapsedTime, final double deltaTime) {
        //
        // -- Off screen rendering first. Render to texture.
        //
        mEffects.render(ellapsedTime, deltaTime);
        try {
            if (mCurrentTexture != null)
                mSphere.getMaterial().removeTexture(mCurrentTexture);

            //
            // -- Get the latest updated texture from the post
            //    processing manager
            //

            mCurrentTexture = mEffects.getTexture();
            mSphere.getMaterial().addTexture(mCurrentTexture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        super.onRender(ellapsedTime, deltaTime);
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
