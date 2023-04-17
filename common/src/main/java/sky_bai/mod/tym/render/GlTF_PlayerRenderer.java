package sky_bai.mod.tym.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.tuple.Triple;
import sky_bai.mod.tym.manager.PlayerModelManager;
import sky_bai.mod.tym.manager.RenderManager;
import sky_bai.mod.tym.manager.data.ModelRendererData;

@Environment(value = EnvType.CLIENT)
public class GlTF_PlayerRenderer extends PlayerRenderer {

    public GlTF_PlayerRenderer(EntityRendererProvider.Context context, boolean b) {
        super(context, b);
    }

    @Override
    public void render(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        float netHeadPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
        float netHeadYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot) - Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        if (PlayerModelManager.getManager().isOpen(player)) {
            ModelRendererData data = getData(player);
            boolean spectator = player.isSpectator();
            PoseStack stack = new PoseStack();
            stack.mulPoseMatrix(matrixStack.last().pose());

            if (!spectator) {
                data.renderItem(player, entityYaw, partialTicks, stack, buffer, packedLight, netHeadPitch, netHeadYaw);
                data.renderModel(player, entityYaw, partialTicks, stack, packedLight, netHeadPitch, netHeadYaw);
            }
            if (this.shouldShowName(player)) {
                renderNameTag(player, player.getDisplayName(), stack, buffer, packedLight);
            }

            ModelRendererData t_data = getDataTranslucence(player);

            Runnable run_2 = () -> {
                if (!spectator)
                    t_data.renderModel(player, entityYaw, partialTicks, stack, packedLight, netHeadPitch, netHeadYaw);
                t_data.resetAnimations();
            };
            RenderManager.addRun(player, run_2);

            //run.run();
        } else {
            super.render(player, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        }
    }


    public ModelRendererData getData(AbstractClientPlayer entity) {
        return PlayerModelManager.getManager().get(entity).getModelRenderer();
    }

    public ModelRendererData getDataTranslucence(AbstractClientPlayer player) {
        return PlayerModelManager.getManager().get(player).getTranslucenceModelRenderer();
    }

    public ModelRendererData getDataArm(AbstractClientPlayer entity) {
        return PlayerModelManager.getManager().get(entity).getArmModelRenderer();
    }


    @Override
    public void renderRightHand(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player) {
        super.renderRightHand(matrixStack, buffer, combinedLight, player);
    }
}
