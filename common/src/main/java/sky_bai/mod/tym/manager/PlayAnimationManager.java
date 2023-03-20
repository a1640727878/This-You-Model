package sky_bai.mod.tym.manager;

import net.minecraft.world.entity.player.Player;
import sky_bai.mod.tym.api.PlayerState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PlayAnimationManager {

    private final static Map<Player, AnimationGroup> PLAYER_ANIMATION_GROUP_MAP = new HashMap<>();


    private static String[] nextAnimationsName(PlayerState state, int i, boolean isUpdate) {
        String[] name = state.getAnimName();
        AnimationGroup group = getAnimationGroup(state.getPlayer());
        switch (i) {
            case 0 -> {
                group.getMain().next(name);
                if (isUpdate) group.getMain().update();
                return group.getMain().getPresent();
            }
            case 1 -> {
                group.getUse().next(name);
                if (isUpdate) group.getUse().update();
                return group.getUse().getPresent();
            }
        }
        return name;
    }

    public static String[] getMainAnimationsName(Player player, float partialTick, boolean isUpdate) {
        PlayerState state = AnimationsManager.getManager().refreshPlayerState(player, partialTick);
        return nextAnimationsName(state, 0, isUpdate);
    }

    public static String[] getUseAnimationsMame(Player player, float partialTick, boolean isUpdate) {
        PlayerState state = AnimationsManager.getManager().refreshPlayerStateUse(player, partialTick);
        return nextAnimationsName(state, 1, isUpdate);
    }


    private static AnimationGroup getAnimationGroup(Player player) {
        AnimationGroup group = PLAYER_ANIMATION_GROUP_MAP.get(player);
        if (group == null) {
            group = new AnimationGroup();
            PLAYER_ANIMATION_GROUP_MAP.put(player, group);
        }
        return group;
    }


    public static class AnimationGroup {
        AnimationGroupName main = new AnimationGroupName();
        AnimationGroupName use = new AnimationGroupName();

        public AnimationGroupName getMain() {
            return main;
        }

        public AnimationGroupName getUse() {
            return use;
        }
    }


    public static class AnimationGroupName {
        private String[] present;
        private String[] next;

        private int time = 0;

        public String[] getPresent() {
            if (present == null) update();

            if (present == next) time = 0;
            else if (time < 10) time++;
            else update();

            return present;
        }

        public void next(String[] name) {
            next = name;
        }

        public void update() {
            if (next == null) return;
            time = 0;
            present = next;
            next = null;
        }
    }

}
