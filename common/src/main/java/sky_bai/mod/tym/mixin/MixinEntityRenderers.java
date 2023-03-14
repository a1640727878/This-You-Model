package sky_bai.mod.tym.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sky_bai.mod.tym.render.GlTF_PlayerRenderer;

import java.util.Map;

@Mixin(EntityRenderers.class)
public class MixinEntityRenderers {
    /**
     * @author
     * @reason
     */
    @Overwrite
    public static Map<String, EntityRenderer<? extends Player>> createPlayerRenderers(EntityRendererProvider.Context context) {
        return Map.of(EntityRenderers.DEFAULT_PLAYER_MODEL, new GlTF_PlayerRenderer(context, false), "slim", new GlTF_PlayerRenderer(context, true));
    }
}
