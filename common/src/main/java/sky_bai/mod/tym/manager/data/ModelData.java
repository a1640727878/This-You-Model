package sky_bai.mod.tym.manager.data;

import sky_bai.mod.tym.lib.jgltf.model.*;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultAnimationModel;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultGltfModel;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultImageModel;
import sky_bai.mod.tym.lib.jgltf.model.impl.DefaultSceneModel;
import sky_bai.mod.tym.lib.jgltf.model.io.Buffers;
import sky_bai.mod.tym.lib.jgltf.model.io.GltfModelReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

public class ModelData {
    private String name;

    private GltfModel model;
    private final ModelRendererData model_renderer = new ModelRendererData();

    private GltfModel translucence_model;
    private final ModelRendererData translucence_model_renderer = new ModelRendererData();

    private GltfModel arm_model;
    private final ModelRendererData arm_model_renderer = new ModelRendererData();

    private final Map<String, ImageModel> images = new HashMap<>();

    private ModelData() {

    }

    public static ModelData getDefaultData(GltfModel[] models) {
        ModelData modelData = new ModelData();
        GltfModel model = extractTranslucenceModel(models[0], false);
        GltfModel translucence = extractTranslucenceModel(models[1], true);
        GltfModel armModel = extractArmModel(models[2]);
        setModel(modelData, "default", model, translucence, armModel, new HashMap<>());
        return modelData;
    }

    public static ModelData getData(FileModelData data) {
        ModelData modelData = new ModelData();
        GltfModel model = bytesToGltfModel(data.getModel());
        GltfModel translucence_model = bytesToGltfModel(data.getModel());
        if (model == null || translucence_model == null) return null;
        extractTranslucenceModel(model, false);
        extractTranslucenceModel(translucence_model, true);
        GltfModel arm_model;
        if (data.getArmModel() != null) {
            arm_model = extractArmModel(
                    Objects.requireNonNull(bytesToGltfModel(data.getArmModel())));
        } else {
            /**arm_model = extractArmModel(Objects.requireNonNull(bytesToGltfModel(data.getModel())));*/
            arm_model = bytesToGltfModel(data.getModel());
        }
        setModel(modelData, data.getName(), model, translucence_model, arm_model, data.getImages());
        return modelData;
    }

    private static void setModel(ModelData data, String name, GltfModel model, GltfModel translucence_model,
                                 GltfModel arm_model,
                                 Map<String, byte[]> images) {
        data.name = name;
        data.model = model;
        data.translucence_model = translucence_model;
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

    private static GltfModel extractTranslucenceModel(GltfModel model, boolean is) {
        List<SceneModel> scenes = model.getSceneModels();
        for (SceneModel scene : scenes) {
            List<NodeModel> nodes = scene.getNodeModels();
            if (nodes == null || nodes.isEmpty()) continue;
            for (NodeModel node : nodes) {
                eliminateNode(node, is);
            }
        }
        return model;
    }

    private static void eliminateNode(NodeModel node, boolean is) {
        List<NodeModel> children = node.getChildren();
        for (NodeModel child : children) {
            String name = child.getName();
            if (name != null && name.startsWith("transl_") == is) continue;
            child.eliminate();
            eliminateNode(child, is);
        }
    }

    private static GltfModel extractArmModel(GltfModel model) {
        List<SceneModel> scenes = model.getSceneModels();
        if (scenes.isEmpty()) return model;
        List<NodeModel> nodes = scenes.get(0).getNodeModels();
        if (nodes == null || nodes.isEmpty()) return model;
        for (NodeModel node : nodes) {
            eliminateArm(node);
        }
        extractAnimations(model);
        return model;
    }

    private static void eliminateArm(NodeModel node) {
        List<NodeModel> children = node.getChildren();
        for (NodeModel child : children) {
            String name = child.getName();
            if (name != null && ((name.equals("LeftArm")) || name.equals("RightArm"))) continue;
            child.eliminate();
            eliminateArm(child);
        }
    }

    private static void extractAnimations(GltfModel model) {
        DefaultGltfModel d_model = (DefaultGltfModel) model;
        List<AnimationModel> animations = model.getAnimationModels();
        List<DefaultAnimationModel> animations_new = new ArrayList<>();
        for (AnimationModel animation : animations) {
            String name = animation.getName();
            if ((name.equals("use_lefthand")) || name.equals("use_righthand"))
                animations_new.add((DefaultAnimationModel) animation);
        }
        d_model.clearAnimationModels();
        d_model.addAnimationModels(animations_new);
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

    public GltfModel getTranslucenceModel() {
        return translucence_model;
    }

    public ModelRendererData getTranslucenceModelRenderer() {
        return translucence_model_renderer;
    }

    public GltfModel getArmModel() {
        return arm_model;
    }

    public ModelRendererData getArmModelRenderer() {
        return arm_model_renderer;
    }

    public Map<GltfModel, ModelRendererData> getInitializeModelRenderer() {
        return Map.of(
                getModel(), getModelRenderer(),
                getTranslucenceModel(), getTranslucenceModelRenderer(),
                getArmModel(), getArmModelRenderer()
        );
    }

    public Map<String, ImageModel> getImages() {
        return images;
    }
}
