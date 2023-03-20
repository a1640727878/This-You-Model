package sky_bai.mod.tym.manager.data;

import sky_bai.mod.tym.lib.jgltf.model.*;
import sky_bai.mod.tym.lib.jgltf.model.impl.*;
import sky_bai.mod.tym.lib.jgltf.model.io.Buffers;
import sky_bai.mod.tym.lib.jgltf.model.io.GltfModelReader;
import sky_bai.mod.tym.manager.json.JsonAnimations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class ModelData {
    private String name;

    private GltfModel model;
    private ModelRendererData model_renderer = new ModelRendererData();

    private GltfModel arm_model_left;
    private ModelRendererData arm_model_left_renderer = new ModelRendererData();

    private GltfModel arm_model_right;
    private ModelRendererData arm_model_right_renderer = new ModelRendererData();

    private Map<String, ImageModel> images = new HashMap<>();

    private ModelData() {

    }

    public static ModelData getDefaultData(GltfModel model, GltfModel model_copy_left, GltfModel model_copy_right) {
        ModelData modelData = new ModelData();
        GltfModel[] gs = extractArmModel(model_copy_left, model_copy_right);
        setModel(modelData, "default", model, gs[0], gs[1], new HashMap<>());
        return modelData;
    }

    public static ModelData getData(FileModelData data) {
        ModelData modelData = new ModelData();
        GltfModel model = bytesToGltfModel(data.getModel());
        if (model == null) return null;
        GltfModel arm_model_left;
        GltfModel arm_model_right;
        if (data.getArmModel() != null) {
            GltfModel[] gs = extractArmModel(
                    Objects.requireNonNull(bytesToGltfModel(data.getArmModel())),
                    Objects.requireNonNull(bytesToGltfModel(data.getArmModel())));
            arm_model_left = gs[0];
            arm_model_right = gs[1];
        } else {
            GltfModel[] gs = extractArmModel(
                    Objects.requireNonNull(bytesToGltfModel(data.getModel())),
                    Objects.requireNonNull(bytesToGltfModel(data.getModel())));
            arm_model_left = gs[0];
            arm_model_right = gs[1];
        }
        setModel(modelData, data.getName(), model, arm_model_left, arm_model_right, data.getImages());
        JsonAnimations JSON = data.toJsonAnimations();
        return modelData;
    }

    private static void setModel(ModelData data, String name, GltfModel model, GltfModel arm_model_left, GltfModel arm_model_right, Map<String, byte[]> images) {
        data.name = name;
        data.model = model;
        data.arm_model_left = arm_model_left;
        data.arm_model_right = arm_model_right;
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

    private static GltfModel[] extractArmModel(GltfModel left, GltfModel right) {
        Set<NodeModel> nodes_ext_left = new HashSet<>();
        Set<NodeModel> nodes_ext_right = new HashSet<>();
        List<NodeModel> nodes_left = left.getNodeModels();
        NodeModel left_hand = null;
        NodeModel right_hand = null;
        for (NodeModel node : nodes_left) {
            String name = node.getName();
            if (name == null) continue;
            switch (name) {
                case "LeftHand" -> {
                    left_hand = node;
                    nodes_ext_left.add(node);
                    List<NodeModel> children = node.getChildren();
                    addNode(nodes_ext_left, children);
                }
                case "RightHand" -> {
                    right_hand = node;
                    nodes_ext_right.add(node);
                    List<NodeModel> children = node.getChildren();
                    addNode(nodes_ext_right, children);
                }
            }
        }
        List<DefaultNodeModel> node_new_left = new ArrayList<>();
        List<DefaultNodeModel> node_new_right = new ArrayList<>();
        for (NodeModel node : nodes_left) {
            if (nodes_ext_left.contains(node)) node_new_left.add((DefaultNodeModel) node);
            if (nodes_ext_right.contains(node)) node_new_right.add((DefaultNodeModel) node);
        }

        ((DefaultGltfModel) left).clearNodeModels();
        ((DefaultGltfModel) left).addNodeModels(node_new_left);
        setSceneNode(left, left_hand);
        extractAnimations(left, true);
        ((DefaultGltfModel) right).clearNodeModels();
        ((DefaultGltfModel) right).addNodeModels(node_new_right);
        setSceneNode(right, right_hand);
        extractAnimations(right, false);
        return new GltfModel[]{left, right};
    }

    private static void setSceneNode(GltfModel model, NodeModel node) {
        for (SceneModel scene : model.getSceneModels()) {
            DefaultSceneModel d_scene = (DefaultSceneModel) scene;
            d_scene.clearNodeModels();
            if (node != null) {
                ((DefaultNodeModel) node).setParent(null);
                d_scene.addNode(node);
            }
        }
    }

    private static void extractAnimations(GltfModel model, boolean isLeft) {
        DefaultGltfModel d_model = (DefaultGltfModel) model;
        List<AnimationModel> animations = model.getAnimationModels();
        List<DefaultAnimationModel> animations_new = new ArrayList<>();
        for (AnimationModel animation : animations) {
            String name = animation.getName();
            if (name.equals("use_lefthand") && isLeft) animations_new.add((DefaultAnimationModel) animation);
            else if (name.equals("use_righthand") && !isLeft) animations_new.add((DefaultAnimationModel) animation);
        }
        d_model.clearAnimationModels();
        d_model.addAnimationModels(animations_new);
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

    public GltfModel getArmModelLeft() {
        return arm_model_left;
    }

    public GltfModel getArmModelRight() {
        return arm_model_right;
    }

    public ModelRendererData getArmModelLeftRenderer() {
        return arm_model_left_renderer;
    }

    public ModelRendererData getArmModelRightRenderer() {
        return arm_model_right_renderer;
    }

    public Map<String, ImageModel> getImages() {
        return images;
    }
}
