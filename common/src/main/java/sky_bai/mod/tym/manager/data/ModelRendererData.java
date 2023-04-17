package sky_bai.mod.tym.manager.data;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.*;
import sky_bai.mod.tym.api.PlayerState;
import sky_bai.mod.tym.lib.jgltf.model.AnimationModel;
import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.jgltf.model.NodeModel;
import sky_bai.mod.tym.lib.mcgltf.MCglTF;
import sky_bai.mod.tym.lib.mcgltf.RenderedGltfModel;
import sky_bai.mod.tym.lib.mcgltf.RenderedGltfScene;
import sky_bai.mod.tym.lib.mcgltf.animation.GltfAnimationCreator;
import sky_bai.mod.tym.lib.mcgltf.animation.InterpolatedChannel;
import sky_bai.mod.tym.manager.PlayAnimationManager;

import java.util.*;

public class ModelRendererData {

    protected Map<String, NodeModel> coreNode;
    private RenderedGltfScene scene;
    private Map<String, List<InterpolatedChannel>> animations;
    private Set<NodeModel> animNode;

    private final NodeModel[] armNodes = new NodeModel[2];

    boolean main_is_update = false;
    boolean use_is_update = false;

    public void initialize(RenderedGltfModel renderedModel) {
        scene = renderedModel.renderedGltfScenes.get(0);

        GltfModel model = renderedModel.gltfModel;

        animations = new HashMap<>();
        animNode = new HashSet<>();
        List<AnimationModel> animationModels = model.getAnimationModels();
        for (AnimationModel animation : animationModels) {
            animations.put(animation.getName(), GltfAnimationCreator.createGltfAnimation(animation));
            animation.getChannels().forEach(channel -> animNode.add(channel.getNodeModel()));
        }
        animNode.forEach(NodeModel::record);

        List<NodeModel> nodes = model.getNodeModels();
        coreNode = new HashMap<>();
        for (NodeModel node : nodes) {
            String name = node.getName();
            if (name == null) continue;
            switch (name) {
                case "Head", "LeftHandLocator", "RightHandLocator" -> coreNode.put(name, node);
                case "LeftArm" -> armNodes[0] = node;
                case "RightArm" -> armNodes[1] = node;
            }
        }
    }

    public boolean isReceiveSharedModel(GltfModel gltfModel, List<Runnable> gtfRenderData) {
        return true;
    }

    private void renderForVanilla() {
        if (scene != null) scene.renderForVanilla();
    }

    public Map<String, List<InterpolatedChannel>> getAnimations() {
        return animations;
    }

    private List<InterpolatedChannel> getAnimations(String[] names) {
        if (names == null || animations == null) return new ArrayList<>();
        List<InterpolatedChannel> list = animations.get(names[0]);
        if (list == null && names.length > 1) list = animations.get(names[0]);
        if (list == null) list = animations.get(PlayerState.IDLE);
        return list != null ? list : new ArrayList<>();
    }

    public List<InterpolatedChannel> getMainAnimations(Player player, float partialTick) {
        String[] names = PlayAnimationManager.getMainAnimationsName(player, partialTick, main_is_update);
        return getAnimations(names);
    }

    public List<InterpolatedChannel> getUseAnimations(Player player, float partialTick) {
        String[] names = PlayAnimationManager.getUseAnimationsMame(player, partialTick, use_is_update);
        return getAnimations(names);
    }

    public Map<String, NodeModel> getCoreNode() {
        return coreNode;
    }

    public NodeModel[] getArmNodes() {
        return armNodes;
    }

    public void resetAnimations() {
        if (animNode != null) animNode.forEach(NodeModel::reset);
    }

    private float sleepDirectionToRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0f;
            case NORTH -> 270.0f;
            case EAST -> 180.0f;
            default -> 0.0f;
        };
    }

    private void scale(PoseStack matrixStack, float size) {
        matrixStack.scale(size, size, size);
    }

    private void setRotations(LivingEntity entity, PoseStack matrixStack, float rotationYaw, float partialTicks, float netHeadPitch, float netHeadYaw) {

        float swimAmount;
        Pose pose = entity.getPose();

        if (pose != Pose.SLEEPING) { // 正常旋转
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(-(rotationYaw - netHeadYaw)));
        }

        if (entity.deathTime > 0) { // 死亡
            float f = (entity.deathTime + partialTicks - 1.0f) / 20.0f * 1.6f;
            if ((f = Mth.sqrt(f)) > 1.0f) {
                f = 1.0f;
            }
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f * -90.0f));
        } else if (pose == Pose.SLEEPING) { // 睡觉
            Direction direction = entity.getBedOrientation();
            boolean b = direction != null;
            if (b) {
                var eyeHeight = entity.getEyeHeight(Pose.STANDING) - 0.1;
                float x = -direction.getStepX();
                float z = -direction.getStepZ();
                matrixStack.translate(x * eyeHeight, 0.0, z * eyeHeight);
            }
            float f = b ? sleepDirectionToRotation(direction) : rotationYaw;
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
            matrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
            matrixStack.mulPose(Vector3f.YP.rotationDegrees(270.0f));
        } /**else if ((swimAmount = entity.getSwimAmount(partialTicks)) > 0f) { // 游泳
         float g = entity.isInWater() ? 90.0f - entity.getXRot() : 90.0f;
         float h = Mth.lerp(swimAmount, 0.0f, g);
         matrixStack.mulPose(Vector3f.XP.rotationDegrees(h));
         if (entity.isVisuallySwimming()) {
         matrixStack.translate(0.0, -1.0, -0.3f);
         }
         }*/
        else if (pose == Pose.CROUCHING) matrixStack.translate(0, 0.15, 0);

    }

    private boolean rangeNumber(float[] keys, float key) {
        int getMin = Math.max(keys.length - 2, 0);
        int getMax = Math.max(keys.length - 1, 0);
        if (getMax == getMin) return true;
        float min = keys[getMin];
        float max = keys[getMax];
        return key >= min && key <= max;
    }

    private void playAnimation(InterpolatedChannel channel, float time, int i) {
        float[] keys = channel.getKeys();
        float key = time % keys[keys.length - 1];
        switch (i) {
            case 0 -> main_is_update = rangeNumber(keys, key);
            case 1 -> use_is_update = rangeNumber(keys, key);
        }
        if (keys.length > 1) channel.update(key);
        else channel.update(0);
    }

    public void renderModel(LivingEntity entity, float rotationYaw, float partialTicks, PoseStack matrixStack, int packedLight, float netHeadPitch, float netHeadYaw) {
        if (entity.isSpectator()) return;

        float head_pitch = netHeadPitch * (float) (Math.PI / 180);
        float head_yaw = netHeadYaw * (float) (Math.PI / 180);

        float time = (entity.level.getGameTime() + partialTicks) / 20;

        if (entity instanceof Player player) {
            // 播放基础动画
            for (InterpolatedChannel channel : getMainAnimations(player, partialTicks)) {
                channel.setPlayer(player);
                playAnimation(channel, time, 0);
            }
            // 播放手部动画
            for (InterpolatedChannel channel : getUseAnimations(player, partialTicks)) {
                channel.setPlayer(player);
                playAnimation(channel, time, 1);
            }
        }

        // 硬编码动画 - 转头
        if (getCoreNode() != null) {
            if (entity.isVisuallySwimming() || entity.isFallFlying() || (entity instanceof Player player && player.getAbilities().flying)) {
                for (Map.Entry<String, NodeModel> entry : getCoreNode().entrySet()) {
                    NodeModel node = entry.getValue();
                    if (entry.getKey().equals("Head"))
                        node.setRotation(new float[]{0.3f, head_yaw * 0.5f, 0, 1});
                }
            } else {
                for (Map.Entry<String, NodeModel> entry : getCoreNode().entrySet()) {
                    NodeModel node = entry.getValue();
                    if (entry.getKey().equals("Head"))
                        node.setRotation(new float[]{-head_pitch * 0.5f, -head_yaw * 0.5f, 0.0f, 1.0f});
                }
            }
        }

        matrixStack.pushPose();
        // 设置旋转
        setRotations(entity, matrixStack, rotationYaw, partialTicks, netHeadPitch, netHeadYaw);

        // 设置结果
        RenderedGltfModel.CURRENT_POSE = matrixStack.last().pose();
        RenderedGltfModel.CURRENT_NORMAL = matrixStack.last().normal();

        // 结束
        matrixStack.popPose();

        renderGL(packedLight);

    }


    public void renderGL(int packedLight) {
        int currentVAO = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        int currentArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        int currentElementArrayBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);

        boolean currentCullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);

        boolean currentDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boolean currentBlend = GL11.glGetBoolean(GL11.GL_BLEND);
        boolean currentSampleMask = GL11.glGetBoolean(GL32.GL_SAMPLE_MASK);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL32.GL_SAMPLE_MASK);

        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

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

            renderForVanilla();

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
        if (!currentSampleMask) GL11.glDisable(GL32.GL_SAMPLE_MASK);

        if (currentCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
        else GL11.glDisable(GL11.GL_CULL_FACE);

        GL30.glBindVertexArray(currentVAO);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentArrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer);
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

    private ItemStack getItemStack(LivingEntity entity, boolean left_hand) {
        return left_hand ? entity.getMainHandItem() : entity.getOffhandItem();
    }

    private ItemTransforms.TransformType getTransformType(boolean left_hand) {
        return left_hand ? ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND : ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
    }

    private void renderHandItem(NodeModel node, ItemStack itemStack, LivingEntity entity, boolean left_hand, PoseStack matrixStack, float rotationYaw, float partialTicks, MultiBufferSource buffer, int packedLight, float netHeadPitch, float netHeadYaw) {
        ItemRenderer item_renderer = Minecraft.getInstance().getItemRenderer();

        org.joml.Matrix4f matrix4f = getMatrix4f(node);
        float[] matrix_4x4 = RenderedGltfModel.findGlobalTransform(node);
        matrix4f.add(new Matrix4f().set(matrix_4x4));
        matrixStack.pushPose();

        setRotations(entity, matrixStack, rotationYaw, partialTicks, netHeadPitch, netHeadYaw);

        org.joml.Vector3f translation = new org.joml.Vector3f();
        matrix4f.getTranslation(translation);
        matrixStack.translate(translation.x, translation.y, translation.z);

        Quaternionf q = new Quaternionf();
        matrix4f.getNormalizedRotation(q);
        Quaternion qq = new Quaternion(q.x, q.y, q.z, q.w);
        matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));
        matrixStack.mulPose(Vector3f.XP.rotationDegrees(90));
        matrixStack.mulPose(qq);

        /**org.joml.Vector3f scale = new org.joml.Vector3f();
         matrix4f.getScale(scale);
         matrixStack.scale(scale.x, scale.x, scale.x);*/

        ItemTransforms.TransformType transformType = getTransformType(left_hand);
        item_renderer.renderStatic(entity, itemStack
                , transformType, left_hand, matrixStack, buffer, entity.level, packedLight, OverlayTexture.NO_OVERLAY, entity.getId() + transformType.ordinal());

        matrixStack.popPose();
    }

    public void renderItem(LivingEntity entity, float rotationYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, float netHeadPitch, float netHeadYaw) {
        if (getCoreNode() == null) return;
        for (Map.Entry<String, NodeModel> entry : getCoreNode().entrySet()) {
            NodeModel node = entry.getValue();
            String name = entry.getKey();
            ItemStack item;
            boolean left_hand = entity.getMainArm() == HumanoidArm.LEFT;

            if (name.equals("LeftHandLocator") && (item = getItemStack(entity, left_hand)) != null) {
                renderHandItem(node, item, entity, left_hand, matrixStack, rotationYaw, partialTicks, buffer, packedLight, netHeadPitch, netHeadYaw);
            } else if (name.equals("RightHandLocator") && (item = getItemStack(entity, !left_hand)) != null) {
                renderHandItem(node, item, entity, !left_hand, matrixStack, rotationYaw, partialTicks, buffer, packedLight, netHeadPitch, netHeadYaw);
            }
        }
    }
}
