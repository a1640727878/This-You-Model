package sky_bai.mod.tym.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import sky_bai.mod.lib.jgltf.model.AnimationModel;
import sky_bai.mod.lib.jgltf.model.GltfModel;
import sky_bai.mod.lib.jgltf.model.NodeModel;
import sky_bai.mod.lib.jgltf.model.io.GltfModelReader;
import sky_bai.mod.lib.mcgltf.RenderedGltfModel;
import sky_bai.mod.lib.mcgltf.RenderedGltfScene;
import sky_bai.mod.lib.mcgltf.animation.GltfAnimationCreator;
import sky_bai.mod.lib.mcgltf.animation.InterpolatedChannel;
import sky_bai.mod.tym.api.PlayerState;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GlTFModelManager {

    static GlTFModelManager glTFModelManager;

    private final Set<ModelData> GlTF = new HashSet<>();

    private ModelData default_model;

    public static GlTFModelManager getManager() {
        if (glTFModelManager == null) {
            glTFModelManager = new GlTFModelManager();
            glTFModelManager.reload();
        }
        return glTFModelManager;
    }

    public void reload() {
        if (Files.notExists(Manager_Dirs.MODELS_DIR)) {
            try {
                Files.createDirectories(Manager_Dirs.MODELS_DIR);
            } catch (IOException ignored) {
            }
        }
        GlTF.clear();
        GltfModel model = getModel(new ResourceLocation("gltf", "default.glb"));
        default_model = new ModelData("default", model);
        setModelPaths();
    }

    public ModelData getDefaultModelData() {
        return default_model;
    }

    public ModelData getModelData(String name) {
        for (ModelData data : GlTF) {
            if (data.name.equals(name)) return data;
        }
        return getDefaultModelData();
    }

    public GltfModel getGltfModel(String name) {
        return getModelData(name).model;
    }

    public RendererData getRendererData(String name) {
        return getModelData(name).data;
    }

    public Set<ModelData> getGlTF() {
        return new HashSet<>(GlTF);
    }

    private Path isGlTF(File[] files) {
        for (File file : files) {
            String name = file.getName();
            if ((name.equals("main.gltf") || name.equals("main.glb")) && file.isFile())
                return file.toPath();
        }
        return null;
    }

    private void setModelPaths() {
        File[] files = Manager_Dirs.MODELS_DIR.toFile().listFiles();
        if (files == null) return;
        for (File file : files) {
            File[] fs;
            if ((fs = file.listFiles()) != null) {
                Path p = isGlTF(fs);
                if (p == null) continue;
                GltfModel gltfModel = getModel(p);
                if (gltfModel == null) continue;
                String name = file.getName();
                GlTF.add(new ModelData(name, gltfModel));
            }
        }
    }

    private GltfModel getModel(Path path) {
        try {
            InputStream is = Files.newInputStream(path);
            return new GltfModelReader().readWithoutReferences(is);
        } catch (IOException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    private GltfModel getModel(ResourceLocation location) {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(location);
            InputStream is = resource.getInputStream();
            return new GltfModelReader().readWithoutReferences(is);
        } catch (IOException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static class ModelData {
        public final String name;
        public final GltfModel model;
        private final RendererData data;

        ModelData(String name, GltfModel model) {
            this.name = name;
            this.model = model;
            data = new RendererData();
        }

        public RendererData getData() {
            return data;
        }
    }


    public static class RendererData {

        protected RenderedGltfScene renderedScene;

        protected Map<String, List<InterpolatedChannel>> animations;

        protected Map<String, NodeModel> coreNode;

        ShaderInstance SHADER = GameRenderer.getRendertypeEntityCutoutNoCullShader();

        public void setShader(ShaderInstance SHADER) {
            this.SHADER = SHADER;
        }

        public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
            renderedScene = renderedModel.renderedGltfScenes.get(0);
            renderedScene.setShader(SHADER);
            List<AnimationModel> animationModels = renderedModel.gltfModel.getAnimationModels();
            animations = new HashMap<>(animationModels.size());
            for (AnimationModel model : animationModels) {
                animations.put(model.getName(), GltfAnimationCreator.createGltfAnimation(model));
            }

            GltfModel model = renderedModel.gltfModel;

            List<NodeModel> nodes = model.getNodeModels();
            coreNode = new HashMap<>();
            for (NodeModel node : nodes) {
                String name = node.getName();
                if (name == null) continue;
                switch (name) {
                    case "Body_ALL", "Head", "LeftHandLocator", "RightHandLocator" -> coreNode.put(name, node);
                }
            }
        }

        public boolean isReceiveSharedModel(GltfModel gltfModel, List<Runnable> gtfRenderData) {
            return true;
        }

        public RenderedGltfScene getRenderedScene() {
            return renderedScene;
        }

        public Map<String, List<InterpolatedChannel>> getAnimations() {
            return animations;
        }

        public Map<String, List<InterpolatedChannel>> getAnimations(AbstractClientPlayer player, float partialTick) {
            LinkedHashMap<String, List<InterpolatedChannel>> map = new LinkedHashMap<>();
            PlayerState state = AnimationsManager.getManager().refreshPlayerState(player, partialTick);
            String[] main_name = state.getMainAnimName();
            setAnimationsMap(main_name, map);
            return map;
        }

        public void setAnimationsMap(String[] names, LinkedHashMap<String, List<InterpolatedChannel>> map) {
            String name_tow = null;
            boolean is = false;
            for (Map.Entry<String, List<InterpolatedChannel>> entry : animations.entrySet()) {
                String name = entry.getKey();
                if (names.length > 1 && name.equals(names[1])) is = true;
                if (!name.equals(names[0])) continue;
                name_tow = names[0];
                break;
            }
            if (name_tow == null) name_tow = is ? names[1] : PlayerState.IDLE;
            List<InterpolatedChannel> list = new ArrayList<>(animations.get(name_tow));
            map.put(name_tow, list);
        }

        public Map<String, NodeModel> getCoreNode() {
            return coreNode;
        }
    }

}
