package sky_bai.mod.tym.manager.data;

import sky_bai.mod.tym.manager.IOManager;
import sky_bai.mod.tym.manager.json.JsonAnimations;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileModelData implements Serializable {
    private final String name;
    private final byte[] model;
    private final byte[] arm_model;

    private final Map<String, byte[]> images = new HashMap<>();
    private String amin_json;

    public FileModelData(String name, byte[] model, byte[] arm_model) {
        this.name = name;
        this.model = model;
        this.arm_model = arm_model;
    }

    public FileModelData(String name, byte[] model) {
        this(name, model, null);
    }

    public String getAminJson() {
        return amin_json;
    }

    public JsonAnimations toJsonAnimations() {
        return IOManager.GSON.fromJson(amin_json, JsonAnimations.class);
    }

    public void setAminJson(String amin_json) {
        this.amin_json = amin_json;
    }

    public String getName() {
        return name;
    }

    public byte[] getModel() {
        return model;
    }

    public byte[] getArmModel() {
        return arm_model;
    }

    public byte[] getImage(String name) {
        return this.images.get(name);
    }

    public Map<String, byte[]> getImages() {
        return this.images;
    }

    public void addImage(String name, byte[] image) {
        this.images.put(name, image);
    }

    public void addImages(Map<String, byte[]> images) {
        this.images.putAll(images);
    }

    public ModelData getData() {
        return ModelData.getData(this);
    }

}
