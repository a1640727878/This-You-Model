package sky_bai.mod.tym.manager;

import net.minecraft.world.entity.player.Player;
import sky_bai.mod.tym.api.PlayerState;

import java.util.HashMap;
import java.util.Map;

public class AnimationsManager {

    private static AnimationsManager manager;

    private final Map<Player, PlayerState> player_state_map = new HashMap<>();

    private final Map<Player, PlayerState> player_state_map_use = new HashMap<>();

    public static AnimationsManager getManager() {
        if (manager == null) manager = new AnimationsManager();
        return manager;
    }

    public PlayerState refreshPlayerState(Player player, float partialTick) {
        PlayerState state = getPlayerState(player);
        state.reload(partialTick);
        return state;
    }

    public PlayerState refreshPlayerStateUse(Player player, float partialTick) {
        PlayerState state = getPlayerStateUse(player);
        state.reload(partialTick);
        return state;
    }


    private PlayerState getPlayerStateUse(Player player) {
        return getPlayerState(player_state_map_use, player, 1);
    }

    private PlayerState getPlayerState(Player player) {
        return getPlayerState(player_state_map, player, 0);
    }

    private PlayerState getPlayerState(Map<Player, PlayerState> map, Player player, int i) {
        PlayerState state = map.get(player);
        if (state == null) {
            switch (i) {
                case 0 -> state = PlayerState.getStateGlobal(player);
                case 1 -> state = PlayerState.getStateUse(player);
                default -> state = new PlayerState(player);
            }
            map.put(player, state);
        }
        return state;
    }

}
