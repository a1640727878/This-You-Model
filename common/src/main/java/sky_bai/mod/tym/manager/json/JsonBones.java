package sky_bai.mod.tym.manager.json;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import sky_bai.mod.tym.manager.IOManager;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JsonBones implements JsonDeserializer<JsonBones> {

    Map<String, JsonObject> map;


    @Override
    public JsonBones deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return null;
    }
}
