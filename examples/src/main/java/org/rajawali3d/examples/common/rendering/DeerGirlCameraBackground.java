package org.rajawali3d.examples.common.rendering;

import android.graphics.Color;
import android.util.Log;
import android.view.Surface;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.StreamingTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.ScreenQuad;

public class DeerGirlCameraBackground extends ScreenQuad {

    public static final String TAG = DeerGirlCameraBackground.class.getSimpleName();
    private StreamingTexture texture;
    private Material material;

    public DeerGirlCameraBackground() {
        super(1, 1);
        texture = new StreamingTexture("backgroundTexture", new StreamingTexture.ISurfaceListener() {
            @Override
            public void setSurface(Surface surface) {
                // This callback is for setSurface or something but ARCore handles texture directly so Surface is not used.
                Log.d(TAG, "Texture is created");
            }
        });
        material = new Material();
        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
        material.setColor(Color.BLACK);
        rotate(Vector3.Axis.Z, 90.0);
    }

    public StreamingTexture getTexture() {
        return texture;
    }
}
