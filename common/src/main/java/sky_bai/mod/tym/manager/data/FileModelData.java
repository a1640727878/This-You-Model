package sky_bai.mod.tym.manager.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FileModelData implements Serializable {
    private String name;
    private byte[] model;
    private byte[] arm_model;

    private Map<String, byte[]> images = new HashMap<>();
    private byte[] amin_json;

    public FileModelData(String name, byte[] model, byte[] arm_model) {
        this.name = name;
        this.model = model;
        this.arm_model = arm_model;
    }

    public FileModelData(String name, byte[] model) {
        this(name, model, null);
    }

    public byte[] getAminJson() {
        return amin_json;
    }

    public void setAminJson(byte[] amin_json) {
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
