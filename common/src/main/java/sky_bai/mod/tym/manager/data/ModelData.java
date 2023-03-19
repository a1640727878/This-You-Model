package sky_bai.mod.tym.manager.data;

import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.jgltf.model.ImageModel;
import sky_bai.mod.tym.lib.jgltf.model.NodeModel;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultGltfModel;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultImageModel;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultNodeModel;
import sky_bai.mod.tym.lib.jgltf.model.io.Buffers;
import sky_bai.mod.tym.lib.jgltf.model.io.GltfModelReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class ModelData {
    private String name;

    private GltfModel model;
    private ModelRendererData model_renderer = new ModelRendererData();

    private GltfModel arm_model;
    private ModelRendererData arm_model_renderer = new ModelRendererData();

    private Map<String, ImageModel> images = new HashMap<>();

    private ModelData() {

    }

    public static ModelData getDefaultData(GltfModel model, GltfModel model_copy) {
        ModelData modelData = new ModelData();
        setModel(modelData, "default", model, extractArmModel(model_copy), new HashMap<>());
        return modelData;
    }

    public static ModelData getData(FileModelData data) {
        ModelData modelData = new ModelData();
        GltfModel model = bytesToGltfModel(data.getModel());
        if (model == null) return null;
        GltfModel arm_model;
        if (data.getArmModel() != null) arm_model = bytesToGltfModel(data.getArmModel());
        else arm_model = extractArmModel(Objects.requireNonNull(bytesToGltfModel(data.getModel())));
        setModel(modelData, data.getName(), model, arm_model, data.getImages());
        return modelData;
    }

    private static void setModel(ModelData data, String name, GltfModel model, GltfModel arm_model, Map<String, byte[]> images) {
        data.name = name;
        data.model = model;
        data.arm_model = arm_model;
        Map<String, ImageModel> images_map = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : images.entrySet()) {
            setImage(images_map, entry);
        }
        data.images.putAll(images_map);
    }

    private static void setImage(Map<String, ImageModel> images, Map.Entry<String, byte[]> entry) {
        String name = entry.getKey();
        String type = "image/jpeg";
        if (name.endsWith(".png")) type = "image/png";
        DefaultImageModel imageModel = new DefaultImageModel();
        imageModel.setMimeType(type);
        imageModel.setImageData(Buffers.create(entry.getValue()));
        images.put(name, imageModel);
    }

    private static GltfModel bytesToGltfModel(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return new GltfModelReader().readWithoutReferences(bis);
        } catch (IOException e) {
            return null;
        }
    }

    private static GltfModel extractArmModel(GltfModel model) {
        Set<NodeModel> nodes_ext = new HashSet<>();
        List<NodeModel> nodes = model.getNodeModels();
        for (NodeModel node : nodes) {
            String name = node.getName();
            if (name == null) continue;
            switch (name) {
                case "LeftHand", "RightHand" -> {
                    nodes_ext.add(node);
                    List<NodeModel> children = node.getChildren();
                    addNode(nodes_ext, children);
                }
            }
        }
        List<DefaultNodeModel> node_new = new ArrayList<>();
        for (NodeModel node : nodes) {
            if (nodes_ext.contains(node)) node_new.add((DefaultNodeModel) node);
        }
        ((DefaultGltfModel) model).clearNodeModels();
        ((DefaultGltfModel) model).addNodeModels(node_new);
        return model;
    }

    private static void addNode(Set<NodeModel> set, List<NodeModel> children) {
        for (NodeModel child : children) {
            set.add(child);
            List<NodeModel> cs = child.getChildren();
            if (cs.size() > 0) addNode(set, cs);
        }
    }

    public String getName() {
        return name;
    }

    public GltfModel getModel() {
        return model;
    }

    public ModelRendererData getModelRenderer() {
        return model_renderer;
    }

    public GltfModel getArmModel() {
        return arm_model;
    }

    public ModelRendererData getArmModelRenderer() {
        return arm_model_renderer;
    }

    public Map<String, ImageModel> getImages() {
        return images;
    }
}
