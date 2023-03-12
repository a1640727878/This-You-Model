package sky_bai.mod.tym.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import sky_bai.mod.lib.jgltf.model.NodeModel;
import sky_bai.mod.lib.mcgltf.MCglTF;
import sky_bai.mod.lib.mcgltf.RenderedGltfModel;
import sky_bai.mod.lib.mcgltf.animation.InterpolatedChannel;
import sky_bai.mod.tym.manager.GlTFModelManager;
import sky_bai.mod.tym.manager.PlayerModelManager;

import java.util.List;
import java.util.Map;

public class GlTF_PlayerRenderer extends PlayerRenderer {

    boolean is_open = true;

    public GlTF_PlayerRenderer(EntityRendererProvider.Context context, boolean b) {
        super(context, b);
    }

    @Override
    public void render(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        if (is_open) {
            render_model(entity, entityYaw, partialTicks, matrixStack, packedLight);
            render_item(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
            if (this.shouldShowName(entity))
                renderNameTag(entity, entity.getDisplayName(), matrixStack, buffer, packedLight);
        } else {
            super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
        }
    }

    public void setOpen(boolean b) {
        is_open = b;
    }

    public String getModelName(AbstractClientPlayer entity) {
        return PlayerModelManager.getManager().get(entity);
    }

    public GlTFModelManager.RendererData getData(AbstractClientPlayer entity) {
        return GlTFModelManager.getManager().getRendererData(getModelName(entity));
    }

    private Matrix4f getMatrix4f(NodeModel node) {
        NodeModel n = node;
        int s = 0;
        while (n.getChildren().size() > 0) {
            n = n.getChildren().get(0);
            s++;
        }
        org.joml.Matrix4f matrix = new org.joml.Matrix4f();
        for (int i = 0; i < s; i++) {
            org.joml.Matrix4f m = new org.joml.Matrix4f();
            float[] f = RenderedGltfModel.findGlobalTransform(n);
            m.set(f);
            matrix.add(m);
            n = n.getParent();
        }
        return matrix;
    }

    // 设置模型大小
    private void scale(PoseStack matrixStack, float size) {
        matrixStack.scale(size, size, size);
    }


    private static float sleepDirectionToRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0f;
            case NORTH -> 270.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }

    private void setRotations(AbstractClientPlayer player, PoseStack matrixStack, float rotationYaw, float partialTicks) {

        float swimAmount;
        Pose pose = player.getPose();

        if (pose != Pose.SLEEPING) { // 正常旋转
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-rotationYaw));
        }
        if (player.deathTime > 0) { // 死亡
            float f = (player.deathTime + partialTicks - 1.0f) / 20.0f * 1.6f;
            if ((f = Mth.sqrt(f)) > 1.0f) {
                f = 1.0f;
            }
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * -this.getFlipDegrees(player)));
        } else if (pose == Pose.SLEEPING) { // 睡觉
            Direction direction = player.getBedOrientation();
            boolean b = direction != null;
            if (b) {
                var eyeHeight = player.getEyeHeight(Pose.STANDING) - 0.1;
                float x = -direction.getStepX();
                float z = -direction.getStepZ();
                matrixStack.translate(x * eyeHeight, 0.0, z * eyeHeight);
            }
            float f = b ? sleepDirectionToRotation(direction) : rotationYaw;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(this.getFlipDegrees(player)));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(270.0f));
        } else if ((swimAmount = player.getSwimAmount(partialTicks)) > 0f) { // 游泳

            float g = player.isInWater() ? 90.0f - player.getXRot() : 90.0f;
            float h = Mth.lerp(swimAmount, 0.0f, g);
            matrixStack.mulPose(Vector3f.XP.rotationDegrees(h));
            if (player.isVisuallySwimming()) {
                matrixStack.translate(0.0, -1.0, -0.3f);
            }
        }
    }

    private ItemStack getItemStack(AbstractClientPlayer player, boolean left_hand) {
        return left_hand ? player.getMainHandItem() : player.getOffhandItem();
    }

    private ItemTransforms.TransformType getTransformType(boolean left_hand) {
        return left_hand ? ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
    }

    private void setHandItem(NodeModel node, ItemStack itemStack, AbstractClientPlayer player, boolean left_hand, PoseStack matrixStack, float rotationYaw, float partialTicks, MultiBufferSource buffer, int packedLight) {
        ItemInHandRenderer item_renderer = Minecraft.getInstance().getItemInHandRenderer();

        org.joml.Matrix4f matrix4f = getMatrix4f(node);
        float[] matrix_4x4 = RenderedGltfModel.findGlobalTransform(node);
        matrix4f.add(new Matrix4f().set(matrix_4x4));
        matrixStack.pushPose();
        setRotations(player, matrixStack, rotationYaw, partialTicks);
        org.joml.Vector3f translation = new org.joml.Vector3f();
        matrix4f.getTranslation(translation);
        matrixStack.translate(translation.x, translation.y, translation.z);
        Quaternionf q = new Quaternionf();
        matrix4f.getNormalizedRotation(q);
        Quaternion qq = new Quaternion(q.x, q.y, q.z, q.w);
        matrixStack.mulPose(qq);
        /**org.joml.Vector3f scale = new org.joml.Vector3f();
         matrix4f.getScale(scale);
         matrixStack.scale(scale.x, scale.x, scale.x);*/

        item_renderer.renderItem(player, itemStack
                , getTransformType(left_hand), left_hand, matrixStack, buffer, packedLight);

        matrixStack.popPose();
    }

    private void render_item(AbstractClientPlayer player, float rotationYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        for (Map.Entry<String, NodeModel> entry : getData(player).getCoreNode().entrySet()) {
            NodeModel node = entry.getValue();
            String key = entry.getKey();
            ItemStack itemStack;
            boolean left_hand = player.getMainArm() == HumanoidArm.LEFT;

            if (key.equals("LeftHandLocator") && (itemStack = getItemStack(player, left_hand)) != null) {
                setHandItem(node, itemStack, player, left_hand, matrixStack, rotationYaw, partialTicks, buffer, packedLight);
            } else if (key.equals("RightHandLocator") && (itemStack = getItemStack(player, !left_hand)) != null) {
                setHandItem(node, itemStack, player, !left_hand, matrixStack, rotationYaw, partialTicks, buffer, packedLight);
            }
        }
    }

    private void render_model(AbstractClientPlayer player, float rotationYaw, float partialTicks, PoseStack matrixStack, int packedLight) {

        if (player.isSpectator()) return;

        float time = (player.level.getGameTime() + partialTicks) / 20;
        //Play every animation clips simultaneously
        for (List<InterpolatedChannel> animation : getData(player).getAnimations().values()) {
            animation.parallelStream().forEach((channel) -> {
                float[] keys = channel.getKeys();
                channel.update(time % keys[keys.length - 1]);
            });
        }

        // 头上下
        float netHeadPitch = Mth.rotLerp(partialTicks, player.xRotO, player.getXRot());
        float head_pitch = netHeadPitch * (float) (Math.PI / 180);
        // 头左右
        float netHeadYaw = Mth.rotLerp(partialTicks, player.yHeadRotO, player.yHeadRot) - Mth.rotLerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        float head_yaw = netHeadYaw * (float) (Math.PI / 180);
        // 硬编码动画 - 转头
        if (!player.isVisuallySwimming()) {
            // 头上下
            for (Map.Entry<String, NodeModel> entry : getData(player).getCoreNode().entrySet()) {
                NodeModel node = entry.getValue();
                if (entry.getKey().equals("Head")) node.setRotation(new float[]{-head_pitch * 0.5f, 0.0f, 0.0f, 1.0f});
                else if (entry.getKey().equals("Body_ALL")) node.setRotation(new float[]{0, head_yaw * 0.5f, 0, 1});
            }
        } else {
            for (Map.Entry<String, NodeModel> entry : getData(player).getCoreNode().entrySet()) {
                NodeModel node = entry.getValue();
                if (entry.getKey().equals("Head")) node.setRotation(new float[]{-0.3f, head_yaw * 0.5f, 0, 1});
                else if (entry.getKey().equals("Body_ALL")) node.setRotation(new float[]{0, 0, 0, 1});
            }

        }

        int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);

        boolean currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        matrixStack.pushPose();
        // 设置旋转
        setRotations(player, matrixStack, rotationYaw, partialTicks);

        // 设置结果
        RenderedGltfModel.CURRENT_POSE = matrixStack.last().pose();
        RenderedGltfModel.CURRENT_NORMAL = matrixStack.last().normal();

        // 结束
        matrixStack.popPose();

        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, packedLight & '\uffff', packedLight >> 16 & '\uffff');

        {
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            int currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, MCglTF.getInstance().getLightTexture().getId());

            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            int currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            getData(player).getRenderedScene().renderForVanilla();

            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0);
        }

        GL30.glVertexAttribI2i(RenderedGltfModel.vaUV2, 0, 0);


        if (!currentDepthTest) GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (!currentBlend) GL11.glDisable(GL11.GL_BLEND);

        if (currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
        else GL11.glDisable(GL11.GL_CULL_FACE);

        GL30.glBindVertexArray(currentVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);

    }


}
