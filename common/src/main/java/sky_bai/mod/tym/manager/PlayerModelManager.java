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
    Map<String, String> player_model_old = new HashMap<>();

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

    public void set(Player player, String model_name) {
        player_model.put(player.getStringUUID(), model_name);
        async_write();
    }

    public void set_old(Player player, String model_name) {
        player_model_old.put(player.getStringUUID(), model_name);
    }

    public String get(Player player) {
        String model_name = player_model.get(player.getStringUUID());
        if (model_name == null) {
            model_name = "default";
            set(player, model_name);
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
