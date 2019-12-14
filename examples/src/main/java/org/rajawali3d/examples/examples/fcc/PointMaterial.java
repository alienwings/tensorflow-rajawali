package org.rajawali3d.examples.examples.fcc;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;

public class PointMaterial extends Material {

    private static final String VERTEX_SHADER = "void main() {\n" +
            "  gl_PointSize = 10.0\n" +
            "  vColor = aColor;\n" +
            "  gl_Position = uMVPMatrix * vPosition;\n" +
            "}";

    private static final String FRAG_SHADER = "precision mediump float;\n" +
            "varying vec4 v_Color;\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = v_Color;\n" +
            "}";

    public PointMaterial() {
        super(new VertexShader(VERTEX_SHADER), new FragmentShader(FRAG_SHADER));
    }


}
