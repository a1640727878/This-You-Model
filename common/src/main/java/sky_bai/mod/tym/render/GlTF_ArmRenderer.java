package sky_bai.mod.tym.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import sky_bai.mod.tym.manager.PlayerModelManager;
import sky_bai.mod.tym.manager.data.ModelRendererData;

public class GlTF_ArmRenderer {


    public static void renderArm(AbstractClientPlayer player, PoseStack matrixStack, float equippedProgress, float swingProgress, boolean isRight) {
        float f = isRight ? 1.0f : -1.0f;
        float g = Mth.sqrt(swingProgress);
        float h = -0.3f * Mth.sin(g * (float) Math.PI);
        float i = 0.4f * Mth.sin(g * ((float) Math.PI * 2));
        float j = -0.4f * Mth.sin(swingProgress * (float) Math.PI);
        // matrixStack.translate(f * (h + 0.64000005f), i + -0.6f + equippedProgress * -0.6f, j + -0.71999997f);
        // matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 45.0f));
        float k = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float l = Mth.sin(g * (float) Math.PI);
        // matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * l * 70.0f));
        // matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * k * -20.0f));
        matrixStack.translate(f * -2.0f, 3.6f - 0.6, 3.5 - 0.5);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * 120.0f));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(f * -120.0f));
        matrixStack.translate(f * (5.6f - 0.6), 0.0, 0.0);
    }

    private static float toFloat(float f) {
        return (float) Math.toRadians(f);
    }

    public static ModelRendererData getArmData(AbstractClientPlayer entity) {
        return PlayerModelManager.getManager().get(entity).getArmModelRenderer();
    }
}
