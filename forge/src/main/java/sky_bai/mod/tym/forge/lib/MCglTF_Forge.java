package sky_bai.mod.tym.forge.lib;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.jgltf.model.io.Buffers;
import sky_bai.mod.tym.lib.mcgltf.MCglTF;
import sky_bai.mod.tym.manager.GlTFModelManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MCglTF_Forge extends MCglTF {

    public MCglTF_Forge() {
        MCglTF.setINSTANCE(this);
        Minecraft.getInstance().execute(() -> {
            //Since max OpenGL version on Windows from GLCapabilities will always return 3.2 as of Minecraft 1.17, this is a workaround to check if OpenGL 4.3 is available.
            get().createSkinningProgram();
        });
    }

    public static MCglTF get() {
        return MCglTF.getInstance();
    }

    //Configs from ForgeConfigSpec is not yet loaded at this event.
    @SubscribeEvent
    public static void onEvent(RegisterClientReloadListenersEvent event) {
        get().setLightTexture(Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation("dynamic/light_map_1")));

        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        get().setDefaultColorMap(GL11.glGenTextures());
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, get().getDefaultColorMap());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

        get().setDefaultNormalMap(GL11.glGenTextures());
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, get().getDefaultNormalMap());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1}));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);

        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            List<Runnable> gltfRenderData = get().getGltfRenderData();
            gltfRenderData.forEach(Runnable::run);
            gltfRenderData.clear();

            GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

            int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            Map<String, MutablePair<GltfModel, List<GlTFModelManager.ModelData>>> lookup = new HashMap<>();

            GlTFModelManager.getManager().getGlTF().forEach(data -> lookup(lookup, data));
            lookup(lookup, GlTFModelManager.getManager().getDefaultModelData());

            get().processRenderedGltfModels(lookup);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL30.glBindVertexArray(0);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);

            get().getLoadedBufferResources().clear();
            get().getLoadedImageResources().clear();
        });
    }

}
