package sky_bai.mod.tym.forge.lib;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import sky_bai.mod.tym.lib.jgltf.model.GltfModel;
import sky_bai.mod.tym.lib.mcgltf.MCglTF;
import sky_bai.mod.tym.manager.GlTFModelManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MCglTF_Forge extends MCglTF {

    public MCglTF_Forge() {
        MCglTF.setINSTANCE(this);
    }

    public static MCglTF get() {
        return MCglTF.getInstance();
    }

    @SubscribeEvent
    public static void onEvent(RegisterClientReloadListenersEvent event) {

        get().initializeGL();

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
