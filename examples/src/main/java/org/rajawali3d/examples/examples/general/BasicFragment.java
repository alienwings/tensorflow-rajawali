package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class BasicFragment extends AExampleFragment {

	private static final String TAG = "BasicFragment";

	@Override
    public AExampleRenderer createRenderer() {
		return new BasicRenderer(getActivity(), this);
	}

	public static final class BasicRenderer extends AExampleRenderer {

		private Object3D mSphere;

		public BasicRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			try {
				Material material = new Material();
				material.addTexture(new Texture("earthColors",
												R.drawable.earthtruecolor_nasa_big));
				material.setColorInfluence(0);
				mSphere = new Sphere(1, 24, 24);
				mSphere.setMaterial(material);
				getCurrentScene().addChild(mSphere);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "Camera initial orientation: " + getCurrentCamera().getOrientation());
            getCurrentCamera().enableLookAt();
            getCurrentCamera().setLookAt(0, 0, 0);
			getCurrentCamera().setUpAxis(new Vector3(0, 1, 0));
//            getCurrentCamera().setLookAt(0, 0, -100);// change from -100 to 0 does not matter
//            getCurrentCamera().setZ(6);
//			getCurrentCamera().setY(6);
            Vector3 vector3 = new Vector3();
//            vector3.setAll(6, 6, 6); // you see nothing! for camera up is y+ axis
            vector3.setAll(0, 6, 0);
            getCurrentCamera().setPosition(vector3);
            // what does orientation mean? ATransformable3D#isCamera
//			getCurrentCamera().setOrientation(getCurrentCamera().getOrientation().inverse()); //观察向量与up向量平行，居然能看到！

			getCurrentCamera().setNearPlane(1.0);
//			getCurrentCamera().setNearPlane(5.5); // the near plane is inside sphere! Amazing vision happens!
//			getCurrentCamera().setFarPlane(10.0); // the near plane is too far, sphere is not included
			getCurrentCamera().setFarPlane(120.0); // change far plane from 120.0 to 10.0 does not matter, object is still included
			getCurrentCamera().setFieldOfView(45); // FieldOfView is larger, object on screen is smaller

			getCurrentScene().setBackgroundColor(Color.RED);
		}

        @Override
        public void onRender(final long elapsedTime, final double deltaTime) {
			super.onRender(elapsedTime, deltaTime);
			mSphere.rotate(Vector3.Axis.Z, 1.0);
		}
	}
}
