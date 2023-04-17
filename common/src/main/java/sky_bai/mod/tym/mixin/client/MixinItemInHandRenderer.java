package sky_bai.mod.tym.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sky_bai.mod.tym.lib.jgltf.model.NodeModel;
import sky_bai.mod.tym.lib.mcgltf.RenderedGltfModel;
import sky_bai.mod.tym.manager.PlayerModelManager;
import sky_bai.mod.tym.manager.data.ModelRendererData;
import sky_bai.mod.tym.render.GlTF_ArmRenderer;

@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {

    @Inject(
            method = "renderPlayerArm(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IFFLnet/minecraft/world/entity/HumanoidArm;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void inject_renderPlayerArm(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, float equippedProgress, float swingProgress, HumanoidArm side, CallbackInfo ci) {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (PlayerModelManager.getManager().isOpen(player)) {
            boolean isRight = (side != HumanoidArm.LEFT);
            ModelRendererData data = GlTF_ArmRenderer.getArmData(player);

            matrixStack.pushPose();

            GlTF_ArmRenderer.renderArm(player, matrixStack, equippedProgress, swingProgress, isRight);

            NodeModel arm;

            RenderedGltfModel.CURRENT_POSE = matrixStack.last().pose();
            RenderedGltfModel.CURRENT_NORMAL = matrixStack.last().normal();

            matrixStack.popPose();

            data.renderGL(combinedLight);

            ci.cancel();
        }
    }

}
