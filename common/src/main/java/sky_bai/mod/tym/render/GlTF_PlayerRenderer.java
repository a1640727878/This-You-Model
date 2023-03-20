package sky_bai.mod.tym.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import sky_bai.mod.tym.manager.PlayerModelManager;
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

            data.renderModel(player, entityYaw, partialTicks, matrixStack, packedLight, netHeadPitch, netHeadYaw);
            data.renderItem(player, entityYaw, partialTicks, matrixStack, buffer, packedLight, netHeadPitch, netHeadYaw);
            if (this.shouldShowName(player))
                renderNameTag(player, player.getDisplayName(), matrixStack, buffer, packedLight);
            data.resetAnimations();
        } else {
            super.render(player, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        }
    }


    public ModelRendererData getData(AbstractClientPlayer entity) {
        return PlayerModelManager.getManager().get(entity).getModelRenderer();
    }

    public ModelRendererData getArmData(AbstractClientPlayer player, boolean isLeft) {
        if (isLeft) return PlayerModelManager.getManager().get(player).getArmModelLeftRenderer();
        else return PlayerModelManager.getManager().get(player).getArmModelRightRenderer();
    }

    @Override
    public void renderLeftHand(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player) {
        if (PlayerModelManager.getManager().isOpen(player)) {
            getArmData(player, true).renderModel(player, 0, 0, matrixStack, combinedLight, 0, 0);
            // getArmData(player, true).renderItem(player, 0, 0, matrixStack, buffer, combinedLight, 0, 0);
        } else {
            super.renderLeftHand(matrixStack, buffer, combinedLight, player);
        }
    }

    @Override
    public void renderRightHand(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, AbstractClientPlayer player) {
        if (PlayerModelManager.getManager().isOpen(player)) {
            getArmData(player, false).renderModel(player, 0, 0, matrixStack, combinedLight, 0, 0);
            // getArmData(player, false).renderItem(player, 0, 0, matrixStack, buffer, combinedLight, 0, 0);
        } else {
            super.renderRightHand(matrixStack, buffer, combinedLight, player);
        }
    }
}
