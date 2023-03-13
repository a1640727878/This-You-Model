package sky_bai.mod.tym.manager;

import net.minecraft.world.entity.player.Player;
import sky_bai.mod.tym.api.PlayerState;

import java.util.HashMap;
import java.util.Map;

public class AnimationsManager {

    private static AnimationsManager animationsManager;

    private final Map<Player, PlayerState> player_state_map = new HashMap<>();

    public static AnimationsManager getManager() {
        if (animationsManager == null) animationsManager = new AnimationsManager();
        return animationsManager;
    }

    public PlayerState refreshPlayerState(Player player, float partialTick) {
        PlayerState state = getPlayerState(player);
        state.reload(partialTick);
        return state;
    }

    private PlayerState getPlayerState(Player player) {
        PlayerState state = player_state_map.get(player);
        if (state == null) {
            state = new PlayerState(player);
            player_state_map.put(player, state);
        }
        return state;
    }

}
