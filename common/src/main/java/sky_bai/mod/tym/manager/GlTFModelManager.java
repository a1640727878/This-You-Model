package sky_bai.mod.tym.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.api.PlayerState;
import sky_bai.mod.tym.lib.jgltf.model.AnimationModel;
import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.jgltf.model.NodeModel;
import sky_bai.mod.tym.lib.jgltf.model.io.GltfModelReader;
import sky_bai.mod.tym.lib.mcgltf.RenderedGltfModel;
import sky_bai.mod.tym.lib.mcgltf.RenderedGltfScene;
import sky_bai.mod.tym.lib.mcgltf.animation.GltfAnimationCreator;
import sky_bai.mod.tym.lib.mcgltf.animation.InterpolatedChannel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GlTFModelManager {

    static GlTFModelManager glTFModelManager;

    private final Set<ModelData> glTFParent = new HashSet<>();

    private ModelData default_model;

    public static GlTFModelManager getManager() {
        if (glTFModelManager == null) {
            glTFModelManager = new GlTFModelManager();
            glTFModelManager.reload();
        }
        return glTFModelManager;
    }

    public void reload() {
        if (Files.notExists(DirectoryManager.MODELS_DIR)) {
            try {
                Files.createDirectories(DirectoryManager.MODELS_DIR);
                ThisYouModel_Main.LOGGER.info("Create ThisYouModel Folder...");
            } catch (IOException ignored) {
            }
        }
        glTFParent.clear();
        GltfModel model = getModel(new ResourceLocation("gltf", "default.glb"));
        default_model = new ModelData("default", model);
        setModelPaths();
    }

    public ModelData getDefaultModelData() {
        return default_model;
    }

    public ModelData getModelData(String name) {
        for (ModelData data : glTFParent) {
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
        return new HashSet<>(glTFParent);
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
        File[] files = DirectoryManager.MODELS_DIR.toFile().listFiles();
        if (files == null) return;
        for (File file : files) {
            File[] fs;
            if ((fs = file.listFiles()) != null) {
                Path p = isGlTF(fs);
                if (p == null) continue;
                GltfModel gltfModel = getModel(p);
                if (gltfModel == null) continue;
                String name = file.getName();
                glTFParent.add(new ModelData(name, gltfModel));
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
        } catch (IOException | RuntimeException e) {
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

        protected Set<NodeModel> animNode;

        public void onReceiveSharedModel(RenderedGltfModel renderedModel) {
            renderedScene = renderedModel.renderedGltfScenes.get(0);
            List<AnimationModel> animationModels = renderedModel.gltfModel.getAnimationModels();
            animations = new HashMap<>(animationModels.size());
            animNode = new HashSet<>();
            for (AnimationModel model : animationModels) {
                animations.put(model.getName(), GltfAnimationCreator.createGltfAnimation(model));
                model.getChannels().forEach(channel -> animNode.add(channel.getNodeModel()));
            }
            animNode.forEach(NodeModel::record);

            GltfModel model = renderedModel.gltfModel;

            List<NodeModel> nodes = model.getNodeModels();
            coreNode = new HashMap<>();
            for (NodeModel node : nodes) {
                String name = node.getName();
                if (name == null) continue;
                switch (name) {
                    case "Head", "LeftHandLocator", "RightHandLocator" -> coreNode.put(name, node);
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

        public List<InterpolatedChannel> getMainAnimation(Player player, float partialTick) {
            List<InterpolatedChannel> list;
            PlayerState state = AnimationsManager.getManager().refreshPlayerState(player, partialTick);
            String[] main_name = state.getMainAnimName();

            list = animations.get(main_name[0]);
            if (list == null && main_name.length > 1) list = animations.get(main_name[0]);
            if (list == null) list = animations.get(PlayerState.IDLE);
            return list != null ? list : new ArrayList<>();
        }

        public Map<String, NodeModel> getCoreNode() {
            return coreNode;
        }

        public void resetNode() {
            animNode.forEach(NodeModel::reset);
        }
    }

}
