package sky_bai.mod.tym.manager;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Manager_Dirs {

    public static final Path GlTF_DIR = Paths.get(Minecraft.getInstance().gameDirectory.toURI()).resolve("GlTF_Model_Dir");

    public static final Path MODELS_DIR = GlTF_DIR.resolve("Model");

    public static final Path CACHE_DIR = GlTF_DIR.resolve("Cache");

    public static final Path PLAYER_CACHE_DIR = CACHE_DIR.resolve("player.cache");

}
