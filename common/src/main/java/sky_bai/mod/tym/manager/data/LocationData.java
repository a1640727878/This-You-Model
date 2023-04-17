package sky_bai.mod.tym.manager.data;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class LocationData {

    final double x;
    final double y;
    final double z;

    public LocationData(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public LocationData(Entity entity){
        this(entity.getX(), entity.getY(), entity.getZ());
    }

    public double distance(LocationData data){
        double x1 = this.x - data.x;
        double y1 = this.y - data.y;
        double z1 = this.z - data.z;
        return Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);

    }
}
