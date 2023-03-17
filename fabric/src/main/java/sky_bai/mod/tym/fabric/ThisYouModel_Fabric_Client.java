package sky_bai.mod.tym.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.lib.mcgltf.MCglTF;
import sky_bai.mod.tym.network.PlayerModelNetwork;

public class ThisYouModel_Fabric_Client implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PlayerModelNetwork.client();

        MCglTF.getInstance().initializeGL();
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(ThisYouModel_Main.MOD_ID, "gltf_reload_listener");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                MCglTF.getInstance().reloadManager();
            }
        });
    }
}
