package sky_bai.mod.tym.manager;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GlTFModelManager {

    static GlTFModelManager manager;

    private final Set<ModelData> glTFParent = new HashSet<>();
    private final Map<String, byte[]> glTFByteBuf = new HashMap<>();

    private ModelData default_model;

    public static GlTFModelManager getManager() {
        if (manager == null) {
            manager = new GlTFModelManager();
            manager.reload();
        }
        return manager;
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
        GltfModel model = loadDefaultModel();
        default_model = new ModelData("default", model);
        setModels();
    }

    public String getModelNamesString() {
        String names = "";
        for (ModelData data : glTFParent) {
            names += (data.name + "<->");
        }
        return names.length() != 0 ? names.substring(0, names.length() - "<->".length()) : names;
    }

    private Map<String, byte[]> getServerModels(String names) {
        Set<String> name_set = Set.of(names.split("<->"));
        Map<String, byte[]> no_name = new HashMap<>();
        if (names.length() == 0) return no_name;
        for (Map.Entry<String, byte[]> entry : glTFByteBuf.entrySet()) {
            if (name_set.contains(entry.getKey())) no_name.put(entry.getKey(), entry.getValue());
        }
        return no_name;
    }

    public Map<String, byte[]> toBytesFoModels(byte[] bytes) {
        return IOManager.theBytesToObject(bytes);
    }

    public String removeModelAndGetNoModel(String names) {
        if (names.length() == 0) return "";
        Set<String> name_set = Set.of(names.split("<->"));
        Set<ModelData> data = new HashSet<>();
        String noModel = "";
        for (ModelData d : glTFParent) {
            if (!name_set.contains(d.name)) continue;
            data.add(d);
            noModel += (d.name + "<->");
        }
        glTFParent.clear();
        glTFParent.addAll(data);
        return noModel.length() != 0 ? noModel.substring(0, names.length() - "<->".length()) : noModel;
    }

    public byte[] getServerModelsBytes(String names) {
        return IOManager.theObjectToBytes(getServerModels(names));
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

    public Set<ModelData> getGlTF() {
        return new HashSet<>(glTFParent);
    }

    private Path isGlTF(File[] files) {
        for (File file : files) {
            String name = file.getName();
            if ((name.equals("main.gltf") || name.equals("main.glb")) && file.isFile()) return file.toPath();
        }
        return null;
    }

    private void setModels() {
        File[] files = DirectoryManager.MODELS_DIR.toFile().listFiles();
        if (files == null) return;
        for (File file : files) {
            String file_name = file.getName();
            File[] fs;
            if ((fs = file.listFiles()) != null) {
                Path p = isGlTF(fs);
                if (p == null) return;
                setModelPath(p, file_name);
                setModelByteBuf(p, file_name);
            }

        }
    }

    private void setModelPath(Path p, String name) {
        GltfModel gltfModel = loadModel(p);
        if (gltfModel == null) return;
        glTFParent.add(new ModelData(name, gltfModel));
    }

    public void addModelByte(String name, byte[] model) {
        GltfModel gltfModel = loadModel(model);
        if (gltfModel == null) return;
        glTFParent.add(new ModelData(name, gltfModel));
    }

    private void setModelByteBuf(Path p, String name) {
        byte[] bs = toBytes(p);
        if (bs == null) return;
        glTFByteBuf.put(name, bs);
    }

    private byte[] toBytes(Path p) {
        byte[] bs;
        try {
            bs = Files.readAllBytes(p);
            return bs;
        } catch (IOException e) {
            return null;
        }
    }

    private GltfModel loadModel(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return new GltfModelReader().readWithoutReferences(is);
        } catch (IOException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    private GltfModel loadModel(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            return new GltfModelReader().readWithoutReferences(bis);
        } catch (IOException e) {
            return null;
        }
    }

    private GltfModel loadDefaultModel() {
        try (InputStream is = ThisYouModel_Main.class.getClassLoader().getResourceAsStream("default.glb")) {
            return new GltfModelReader().readWithoutReferences(is);
        } catch (IOException e) {
            return null;
        }
    }

    public void addServerModels(byte[] bytes){
        Map<String, byte[]> server_models = GlTFModelManager.getManager().toBytesFoModels(bytes);
        for (Map.Entry<String, byte[]> entry : server_models.entrySet()) {
            GlTFModelManager.getManager().addModelByte(entry.getKey(), entry.getValue());
        }
    }

    public byte[] rendServerModel(){
        Path path = DirectoryManager.SERVER_MODEL_CACHE_DIR.resolve(ServerManage.getSeverKey());
        try (InputStream is = Files.newInputStream(path)){
            return is.readAllBytes();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public void asyncWriteServerModel(byte[] bytes) {
        IOManager.SERVICE.execute(() -> writeServerModel(bytes));
    }

    private void writeServerModel(byte[] bytes) {
        Path path = DirectoryManager.SERVER_MODEL_CACHE_DIR.resolve(ServerManage.getSeverKey());
        if (Files.notExists(path)) IOManager.createFile(path);
        try {
            Files.write(path, bytes);
        } catch (IOException ignored) {

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
