package sky_bai.mod.tym.manager.json;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class JsonAnimation {

    boolean loop = false;
    @SerializedName("animation_length")
    float animationLength;

    Map<String,JsonBones> bones;


}
