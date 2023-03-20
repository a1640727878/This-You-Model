package sky_bai.mod.tym.manager.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class JsonAnimations {

    @SerializedName("format_version")
    String formatVersion;

    @SerializedName("animations")
    Map<String,JsonAnimation> animations;

}
