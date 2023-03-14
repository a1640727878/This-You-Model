package sky_bai.mod.tym.lib.mcgltf;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.List;

public class RenderedGltfScene {

    public final List<Runnable> skinningCommands = new ArrayList<Runnable>();

    public final List<Runnable> vanillaRenderCommands = new ArrayList<Runnable>();

    public final List<Runnable> shaderModRenderCommands = new ArrayList<Runnable>();

    ShaderInstance SHADER;

    public void setShader(ShaderInstance SHADER) {
        this.SHADER = SHADER;
    }

    public void renderForVanilla() {
        int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        if (!skinningCommands.isEmpty()) {
            GL20.glUseProgram(MCglTF.getInstance().getGlProgramSkinning());
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            skinningCommands.forEach(Runnable::run);
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
            GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
        }

        RenderedGltfModel.CURRENT_SHADER_INSTANCE = SHADER;
        int entitySolidProgram = RenderedGltfModel.CURRENT_SHADER_INSTANCE.getId();
        GL20.glUseProgram(entitySolidProgram);

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.PROJECTION_MATRIX.upload();

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.INVERSE_VIEW_ROTATION_MATRIX.upload();

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_START.set(RenderSystem.getShaderFogStart());
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_START.upload();

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_END.set(RenderSystem.getShaderFogEnd());
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_END.upload();

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_COLOR.upload();

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.FOG_SHAPE.upload();

        RenderedGltfModel.CURRENT_SHADER_INSTANCE.COLOR_MODULATOR.set(1.0F, 1.0F, 1.0F, 1.0F);
        RenderedGltfModel.CURRENT_SHADER_INSTANCE.COLOR_MODULATOR.upload();

        GL20.glUniform1i(GL20.glGetUniformLocation(entitySolidProgram, "Sampler0"), 0);
        GL20.glUniform1i(GL20.glGetUniformLocation(entitySolidProgram, "Sampler1"), 1);
        GL20.glUniform1i(GL20.glGetUniformLocation(entitySolidProgram, "Sampler2"), 2);

        RenderSystem.setupShaderLights(RenderedGltfModel.CURRENT_SHADER_INSTANCE);
        RenderedGltfModel.LIGHT0_DIRECTION = new Vector3f(RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(0), RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(1), RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT0_DIRECTION.getFloatBuffer().get(2));
        RenderedGltfModel.LIGHT1_DIRECTION = new Vector3f(RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(0), RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(1), RenderedGltfModel.CURRENT_SHADER_INSTANCE.LIGHT1_DIRECTION.getFloatBuffer().get(2));

        vanillaRenderCommands.forEach(Runnable::run);

        GL20.glUseProgram(currentProgram);

        RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
    }

}
