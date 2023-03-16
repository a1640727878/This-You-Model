package sky_bai.mod.tym.manager;

import com.google.gson.reflect.TypeToken;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PlayerModelManager {

    public static final String YES = "yes";
    public static final String NO = "no";
    static PlayerModelManager playerModelManager;
    Map<String, String> playerModel = new HashMap<>();
    Map<String, String> playerOpenModel = new HashMap<>();
    Map<String, GlTFModelManager.ModelData> playerModelData = new HashMap<>();

    public static PlayerModelManager getManager() {
        if (playerModelManager == null) playerModelManager = read();
        return playerModelManager;
    }

    public static PlayerModelManager read() {
        PlayerModelManager manager = new PlayerModelManager();
        if (Files.exists(DirectoryManager.PLAYER_CACHE_DIR)) {
            read(DirectoryManager.PLAYER_CACHE_DIR, manager.playerModel);
        }
        if (Files.exists(DirectoryManager.PLAYER_OPEN_CACHE_DIR)) {
            read(DirectoryManager.PLAYER_OPEN_CACHE_DIR, manager.playerOpenModel);
        }

        return manager;
    }

    private static <K, V> void read(Path path, Map<K, V> manager_map) {
        if (Files.exists(path)) {
            try {
                String json = Files.readString(path);
                Map<K, V> map = IOManager.GSON.fromJson(json, new TypeToken<Map<K, V>>() {
                }.getType());
                manager_map.putAll(map);
            } catch (IOException ignored) {

            }
        }
    }

    public void setOpen(Player player, boolean b) {
        playerOpenModel.put(player.getStringUUID(), b ? YES : NO);
        asyncWriteCache(this::writeOpenCache);
    }

    public boolean isOpen(Player player) {
        String uuid = player.getStringUUID();
        String str = playerOpenModel.get(uuid);
        if (str == null) {
            str = YES;
            setOpen(player, true);
        }
        return str.equals(YES);
    }

    public void set(String player_uuid, String model_name) {
        playerModel.put(player_uuid, model_name);
        asyncWriteCache(this::writeCache);
        setPlayerData(player_uuid, model_name);
    }


    private GlTFModelManager.ModelData setPlayerData(String uuid, String model_name) {
        GlTFModelManager.ModelData data = GlTFModelManager.getManager().getModelData(model_name);
        playerModelData.put(uuid, data);
        return data;
    }

    public GlTFModelManager.ModelData get(Player player) {
        String uuid = player.getStringUUID();
        GlTFModelManager.ModelData data = playerModelData.get(uuid);
        if (data == null) {
            String model_name = getModelName(uuid);
            data = setPlayerData(uuid, model_name);
        }
        return data;
    }

    public String getModelName(String player_uuid) {
        String model_name = playerModel.get(player_uuid);
        if (model_name == null) {
            model_name = "default";
            set(player_uuid, model_name);
        }
        return model_name;
    }

    public void asyncWriteCache(Runnable command) {
        IOManager.SERVICE.execute(command);
    }

    public synchronized void writeCache() {
        write(DirectoryManager.PLAYER_CACHE_DIR, playerModel);
    }

    public synchronized void writeOpenCache() {
        write(DirectoryManager.PLAYER_OPEN_CACHE_DIR, playerOpenModel);
    }

    public <K, V> void write(Path path, Map<K, V> map) {
        if (Files.notExists(path)) {
            try {
                if (Files.notExists(DirectoryManager.CACHE_DIR))
                    Files.createDirectories(DirectoryManager.CACHE_DIR);
                Files.createFile(path);
            } catch (IOException ignored) {

            }
        }
        String json = IOManager.GSON.toJson(map);
        try {
            Files.writeString(path, json);
        } catch (IOException ignored) {

        }
    }

}
