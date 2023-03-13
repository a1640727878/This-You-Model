package sky_bai.mod.tym.api;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayerState {

    public static final String NULL = "null";
    public static final String IDLE = "idle";
    private static final List<StateDataI> MAIN_ANIM_NAME_I = new ArrayList<>();
    private static final List<StateDataG> MAIN_ANIM_NAME_G = new ArrayList<>();

    final Player player;

    float limbSwingAmount;
    float limbSwing;

    public PlayerState(Player player) {
        this.player = player;
        reload(0);
        registerALL();
    }

    public void reload(float partialTick) {
        Entity vehicle = player.getVehicle();

        limbSwingAmount = 0;
        limbSwing = 0;
        if (!(player.isPassenger() && vehicle != null) && player.isAlive()) {
            limbSwingAmount = Mth.lerp(partialTick, player.animationSpeedOld, player.animationSpeed);
            limbSwing = player.animationPosition - player.animationSpeed * (1 - partialTick);
            if (limbSwingAmount > 1) limbSwingAmount = 1f;
        }
    }

    public String[] getMainAnimName() {
        for (StateDataG g : MAIN_ANIM_NAME_G) {
            String key = g.get(player, this);
            if (!Objects.equals(key, NULL)) return new String[]{key, g.name};
        }
        for (StateDataI i : MAIN_ANIM_NAME_I) {
            if (i.is(player,this)) return new String[]{i.name};
        }
        return new String[]{IDLE};
    }

    private void registerALL() {
        registerIs("sleep", ((p, state) -> p.isSleeping()));
        registerIs("attacked", (p, state) -> p.hurtTime > 0);

        registerGet("swim", (player, state) -> {
            Pose pose = player.getPose();
            if (pose != Pose.SWIMMING) return NULL;
            if (player.isSwimming()) return "swim";
            else if (player.isOnGround()) return "climbing";
            else return "swim_stand";
        });
        registerGet("sit", (p, state) -> {
            if (!p.isPassenger()) return NULL;
            Entity entity = p.getVehicle();
            if (entity == null || entity instanceof Minecart) return "sit";
            else if (entity instanceof Boat) return "boat";
            else if (entity instanceof Pig) return "ride_pig";
            else if (entity instanceof Saddleable) return "ride";
            else return "ride_" + entity.getType().getDescriptionId();
        });
        registerGet("jump", (p, state) -> {
            if (p.isOnGround()) return NULL;
            if (p.getAbilities().flying) return "fly";
            else if (p.isFallFlying()) return "fall";
            else if (!p.isInWater()) return "jump";
            return NULL;
        });
        registerGet("walk", (p, state) -> {
            if (!p.isOnGround()) return NULL;
            Pose pose = p.getPose();
            if (limbSwingAmount > 0.15d) {
                if (pose == Pose.CROUCHING) return "sneak";
                else if (p.isSprinting()) return "rua";
                else return "walk";
            } else if (pose == Pose.CROUCHING) return "sneaking";
            return NULL;
        });
    }

    public static void registerGet(String name, GetState get) {
        MAIN_ANIM_NAME_G.add(new StateDataG(name, get));
    }

    public static void registerIs(String name, IsState is) {
        MAIN_ANIM_NAME_I.add(new StateDataI(name, is));
    }

    public static class StateDataI {

        final String name;
        final IsState is;

        private StateDataI(String name, IsState is) {
            this.name = name;
            this.is = is;
        }

        public boolean is(Player player, PlayerState state) {
            return is.is(player, state);
        }

        public String getName() {
            return name;
        }
    }

    public static class StateDataG {
        final String name;
        final GetState get;

        private StateDataG(String name, GetState get) {
            this.name = name;
            this.get = get;
        }

        public String get(Player player, PlayerState state) {
            return get.get(player, state);
        }

        public String getName() {
            return name;
        }
    }

    public interface IsState {
        boolean is(Player player, PlayerState state);
    }

    public interface GetState {
        String get(Player player, PlayerState state);
    }

}
