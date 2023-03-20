package sky_bai.mod.tym.manager;

import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.jgltf.model.io.GltfModelReader;
import sky_bai.mod.tym.manager.data.FileModelData;
import sky_bai.mod.tym.manager.data.ModelData;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlTFModelManager {

    private static GlTFModelManager manager;

    private final Set<ModelData> glTFParent = new HashSet<>();

    private ModelData default_model;

    private GlTFModelManager() {
        initialize();
    }

    public static GlTFModelManager getManager() {
        if (manager == null) manager = new GlTFModelManager();
        return manager;
    }

    private void initialize() {
        if (Files.notExists(DirectoryManager.MODELS_DIR)) {
            try {
                Files.createDirectories(DirectoryManager.MODELS_DIR);
                ThisYouModel_Main.LOGGER.info("Create ThisYouModel Folder...");
            } catch (IOException ignored) {
            }
        }
        default_model = ModelData.getDefaultData(loadDefaultModel(), loadDefaultModel(),loadDefaultModel());
    }

    private GltfModel loadDefaultModel() {
        try (InputStream is = ThisYouModel_Main.class.getClassLoader().getResourceAsStream("default.glb")) {
            return new GltfModelReader().readWithoutReferences(is);
        } catch (IOException e) {
            return null;
        }
    }

    public void loadServerModels() {
        glTFParent.clear();
        loadModels(FileModelManager.getTheFileModelServerDataMap());
    }

    public void loadModels(Map<String, FileModelData> fileMap) {
        for (Map.Entry<String, FileModelData> entry : fileMap.entrySet()) {
            glTFParent.add(entry.getValue().getData());
        }
    }

    public ModelData getDefaultModel() {
        return default_model;
    }

    public Set<ModelData> getGlTFParent() {
        return glTFParent;
    }

    public ModelData getModel(String name) {
        for (ModelData data : glTFParent) {
            if (data.getName().equals(name)) return data;
        }
        return getDefaultModel();
    }
}
