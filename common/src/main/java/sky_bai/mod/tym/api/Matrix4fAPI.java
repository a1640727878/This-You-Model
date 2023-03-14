package sky_bai.mod.tym.api;

import com.mojang.math.Matrix4f;

public interface Matrix4fAPI {

    static void setMatrix4f(Matrix4f poss, float[] floats) {
        ((Matrix4fAPI) ((Object) poss)).setM(floats);
    }

    void setM(float[] m);

}
