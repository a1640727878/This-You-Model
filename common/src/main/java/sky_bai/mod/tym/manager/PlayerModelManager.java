package sky_bai.mod.tym.manager;

import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import net.minecraft.world.entity.player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerModelManager {

    static PlayerModelManager playerModelManager;
    Map<String, String> player_model = new HashMap<>();

    Map<String, GlTFModelManager.ModelData> player_model_data = new HashMap<>();

    public static PlayerModelManager getManager() {
        if (playerModelManager == null) playerModelManager = read();
        return playerModelManager;
    }

    public static PlayerModelManager read() {
        PlayerModelManager manager = new PlayerModelManager();
        if (Files.exists(Manager_Dirs.PLAYER_CACHE_DIR)) {
            try {
                InputStream is = Files.newInputStream(Manager_Dirs.PLAYER_CACHE_DIR);
                ObjectInputStream ois = new ObjectInputStream(is);

                Object obj = ois.readObject();
                Map<?, ?> map = new HashMap<>();
                if (obj instanceof Map<?, ?>) map = (Map<?, ?>) obj;
                map.entrySet().forEach((entry -> {
                    Object k = entry.getKey();
                    Object v = entry.getValue();
                    if (k instanceof String && v instanceof String) manager.player_model.put((String) k, (String) v);
                }));
            } catch (IOException | ClassNotFoundException ignored) {

            }
        }
        return manager;
    }

    public void set(String player_uuid, String model_name) {
        player_model.put(player_uuid, model_name);
        async_write();
        set_player_data(player_uuid, model_name);
        ;
    }

    private GlTFModelManager.ModelData set_player_data(String uuid, String model_name) {
        GlTFModelManager.ModelData data = GlTFModelManager.getManager().getModelData(model_name);
        player_model_data.put(uuid, data);
        return data;
    }

    public GlTFModelManager.ModelData get(Player player) {
        String uuid = player.getStringUUID();
        GlTFModelManager.ModelData data = player_model_data.get(uuid);
        if (data == null) {
            String model_name = get_model_name(uuid);
            data = set_player_data(uuid, model_name);
        }
        return data;
    }

    public String get_model_name(String player_uuid) {
        String model_name = player_model.get(player_uuid);
        if (model_name == null) {
            model_name = "default";
            set(player_uuid, model_name);
        }
        return model_name;
    }

    public void async_write() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(this::write, 0, TimeUnit.MILLISECONDS);
        service.shutdown();
    }

    public synchronized void write() {
        if (Files.notExists(Manager_Dirs.PLAYER_CACHE_DIR)) {
            try {
                Files.createDirectories(Manager_Dirs.CACHE_DIR);
                Files.createFile(Manager_Dirs.PLAYER_CACHE_DIR);
            } catch (IOException ignored) {

            }
        }
        try {
            OutputStream os = Files.newOutputStream(Manager_Dirs.PLAYER_CACHE_DIR);
            // os = new ByteArrayOutputStream();
            ObjectEncoderOutputStream oos = new ObjectEncoderOutputStream(os);

            oos.writeObject(player_model);
            oos.flush();

            oos.close();
            os.close();

        } catch (IOException ignored) {

        }
    }

}
