package sky_bai.mod.tym.manager;

import sky_bai.mod.tym.manager.data.FileModelData;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileModelManager {

    private static final Map<String, FileModelData> theFileModelServerDataMap = new HashMap<>();
    private static final Map<String, FileModelData> theFileModelDataMap = new HashMap<>();
    private static FileModelManager manager;

    private FileModelManager() {
        loadFileModels();
    }

    public static FileModelManager getManager() {
        if (manager == null) manager = new FileModelManager();
        return manager;
    }

    public static Map<String, FileModelData> getTheFileModelServerDataMap() {
        return new HashMap<>(theFileModelServerDataMap);
    }

    public static Map<String, FileModelData> getTheFileModelDataMap() {
        return new HashMap<>(theFileModelDataMap);
    }

    public static Set<String> getFileModelNames() {
        return theFileModelDataMap.keySet();
    }

    public byte[] generateNoModelBytes(String names) {
        return IOManager.theObjectToBytes(generateByteModelMap(names));
    }

    public byte[] generateAllModelBytes() {
        return IOManager.theObjectToBytes(generateByteModelMap());
    }

    public String generateServerFileNames() {
        String names = "";
        for (Map.Entry<String, FileModelData> entry : theFileModelDataMap.entrySet()) {
            names += (entry.getKey() + "<->");
        }
        return names.length() != 0 ? names.substring(0, names.length() - "<->".length()) : names;
    }

    public byte[] getFileModelDataBytes(FileModelData data) {
        return IOManager.theObjectToBytes(data);
    }

    public void reload() {
        loadFileModels();
    }

    public void asyncWriteServerFileModel() {
        IOManager.SERVICE.execute(() -> writeServerFileModels(getServerFileModelBytes()));
    }

    private void writeServerFileModels(byte[] bytes) {
        Path path = DirectoryManager.getServerFileModelCacheDie();
        if (Files.notExists(path)) IOManager.createFile(path);
        try {
            Files.write(path, bytes);
        } catch (IOException ignored) {

        }
    }

    public void loadSeverFileModels(byte[] bytes) {
        Map<String, byte[]> server_models = IOManager.theBytesToObject(bytes);
        if (server_models == null) return;
        for (Map.Entry<String, byte[]> entry : server_models.entrySet()) {
            String name = entry.getKey();
            FileModelData data = IOManager.theBytesToObject(entry.getValue());
            if (data != null) theFileModelServerDataMap.put(name, data);
        }
        asyncWriteServerFileModel();
    }

    public byte[] getCacheSeverFileModels() {
        Path path = DirectoryManager.getServerFileModelCacheDie();
        try (InputStream is = Files.newInputStream(path)) {
            return is.readAllBytes();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public String getNoModelString(String names) {
        String noModel = "";
        if (names.length() == 0) return noModel;
        Set<String> name_set = Set.of(names.split("<->"));
        Set<String> c_name_set = new HashSet<>();
        Map<String, FileModelData> dataMap = new HashMap<>();
        for (Map.Entry<String, FileModelData> entry : theFileModelServerDataMap.entrySet()) {
            if (!name_set.contains(entry.getKey())) continue;
            dataMap.put(entry.getKey(), entry.getValue());
            c_name_set.add(entry.getKey());
        }
        for (String name : name_set) {
            if (!c_name_set.contains(name)) noModel += (name + "<->");
        }
        theFileModelServerDataMap.clear();
        theFileModelServerDataMap.putAll(dataMap);
        return noModel;
    }

    private Map<String, byte[]> generateByteModelMap(String names) {
        if (names.length() == 0) return new HashMap<>();
        Set<String> name_set = Set.of(names.split("<->"));
        Map<String, byte[]> byte_model_map = new HashMap<>();
        for (Map.Entry<String, FileModelData> entry : theFileModelDataMap.entrySet()) {
            if (name_set.contains(entry.getKey()))
                byte_model_map.put(entry.getKey(), getFileModelDataBytes(entry.getValue()));
        }
        return byte_model_map;
    }

    private Map<String, byte[]> generateByteModelMap() {
        Map<String, byte[]> byte_model_map = new HashMap<>();
        for (Map.Entry<String, FileModelData> entry : theFileModelDataMap.entrySet()) {
            byte_model_map.put(entry.getKey(), getFileModelDataBytes(entry.getValue()));
        }
        return byte_model_map;
    }

    private Map<String, byte[]> generateServerByteModelMap() {
        Map<String, byte[]> byte_model_map = new HashMap<>();
        for (Map.Entry<String, FileModelData> entry : theFileModelServerDataMap.entrySet()) {
            byte_model_map.put(entry.getKey(), getFileModelDataBytes(entry.getValue()));
        }
        return byte_model_map;
    }

    private byte[] getServerFileModelBytes() {
        return IOManager.theObjectToBytes(generateServerByteModelMap());
    }

    private void loadFileModels() {
        theFileModelDataMap.clear();
        File[] files = DirectoryManager.MODELS_DIR.toFile().listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isFile()) continue;
            String name = file.getName();
            FileModelData data = getFileModel(file, name);
            if (data != null) theFileModelDataMap.put(name, data);
        }
    }

    private FileModelData getFileModel(File file, String name) {
        File[] files = file.listFiles();
        if (files == null) return null;
        byte[] model = null;
        byte[] arm_model = null;
        Map<String, byte[]> images = new HashMap<>();
        String amin_json = "";
        for (File f : files) {
            String file_name = f.getName();
            switch (file_name) {
                case "main.gltf", "main.gld" -> model = IOManager.theFileToBytes(f);
                case "arm.gltf", "arm.gld" -> arm_model = IOManager.theFileToBytes(f);
                case "main.animation.json" -> amin_json = IOManager.readJsonString(f);
                default -> {
                    if (file_name.endsWith(".png") || file_name.endsWith(".jpg"))
                        images.put(file_name, IOManager.theFileToBytes(f));
                }
            }
        }
        if (model == null) return null;
        FileModelData data = new FileModelData(name, model, arm_model);
        data.addImages(images);
        data.setAminJson(amin_json);
        return data;
    }

}
