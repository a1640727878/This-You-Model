package sky_bai.mod.tym.manager;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.phys.Vec3;
import sky_bai.mod.tym.manager.data.LocationData;

import java.util.*;

public class RenderManager {

    static Map<LocationData, Runnable> run_map = new HashMap<>();

    public static void runAll() {
        sortRun().forEach(Runnable::run);
        run_map.clear();
    }

    public static void addRun(Player player, Runnable run) {
        run_map.put(new LocationData(player), run);
    }

    private static List<Runnable> sortRun() {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 vec3 = camera.getPosition();
        LocationData data = new LocationData(vec3.x(), vec3.y(), vec3.z());

        List<Map.Entry<LocationData, Runnable>> list_run = new ArrayList<>(run_map.entrySet());
        Comparator<Map.Entry<LocationData, Runnable>> comparator = Comparator.comparing(entry -> entry.getKey().distance(data));
        list_run = list_run.stream()
                .sorted(comparator.reversed())
                .toList();
        List<Runnable> list = new ArrayList<>();
        list_run.forEach(entry -> list.add(entry.getValue()));

        return list;
    }

}
