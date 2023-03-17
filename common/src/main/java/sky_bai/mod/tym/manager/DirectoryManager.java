package sky_bai.mod.tym.manager;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryManager {

    public static final Path GlTF_DIR = Paths.get("").resolve("GlTF_Model_Dir");

    public static final Path MODELS_DIR = GlTF_DIR.resolve("Model");

    public static final Path CACHE_DIR = GlTF_DIR.resolve("Cache");

    public static final Path PLAYER_CACHE_DIR = CACHE_DIR.resolve("player.cache");

    public static final Path PLAYER_OPEN_CACHE_DIR = CACHE_DIR.resolve("player_open.cache");

    public static final Path SERVER_MODEL_CACHE_DIR = CACHE_DIR.resolve("SeverCache");

    public static final Path SERVER_KEY_DIR = CACHE_DIR.resolve(".key");

}
