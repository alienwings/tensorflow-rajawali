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
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.common.helpers.TapHelper;
import org.rajawali3d.examples.common.rendering.DeerGirlARCoreRenderer;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;

public class FogDemo1Render extends DeerGirlARCoreRenderer {
    private TapHelper tapHelper;

    private DirectionalLight mLight;
    private Object3D mRoad;

    public FogDemo1Render(@NotNull Context context, @NotNull TapHelper tapHelper, @NotNull Session session) {
        super(context, session);
        this.tapHelper = tapHelper;
    }

    @Override
    protected void initScene() {
        super.initScene();
        mLight = new DirectionalLight(0, -1, -1);
        mLight.setPower(.5f);

        getCurrentScene().addLight(mLight);

        int fogColor = 0x999999;

        getCurrentScene().setBackgroundColor(fogColor);
        getCurrentScene().setFog(new FogMaterialPlugin.FogParams(FogMaterialPlugin.FogType.LINEAR, fogColor, 1, 15));

        LoaderOBJ objParser = new LoaderOBJ(mContext.getResources(),
                mTextureManager, R.raw.road);
        try {
            objParser.parse();
            mRoad = objParser.getParsedObject();
            mRoad.setZ(5);
            mRoad.setRotY(180);
            getCurrentScene().addChild(mRoad);

            Material roadMaterial = new Material();
            roadMaterial.enableLighting(true);
            roadMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
            roadMaterial.addTexture(new Texture("roadTex", R.drawable.road));
            roadMaterial.setColorInfluence(0);
            mRoad.getChildByName("Road").setMaterial(roadMaterial);

            Material signMaterial = new Material();
            signMaterial.enableLighting(true);
            signMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
            signMaterial.addTexture(new Texture("rajawaliSign", R.drawable.sign));
            signMaterial.setColorInfluence(0);
            mRoad.getChildByName("WarningSign").setMaterial(signMaterial);

            Material warningMaterial = new Material();
            warningMaterial.enableLighting(true);
            warningMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
            warningMaterial.addTexture(new Texture("warning", R.drawable.warning));
            warningMaterial.setColorInfluence(0);
            mRoad.getChildByName("Warning").setMaterial(warningMaterial);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TranslateAnimation3D camAnim = new TranslateAnimation3D(
                new Vector3(0, 2, 0),
                new Vector3(0, 2, -23));
        camAnim.setDurationMilliseconds(8000);
        camAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        camAnim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
        camAnim.setTransformable3D(getCurrentCamera());
        getCurrentScene().registerAnimation(camAnim);
        camAnim.play();
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
                mRoad.setPosition(translation[0], translation[1], translation[2]);
                break;
            }
        }
    }
}
