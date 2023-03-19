package sky_bai.mod.tym.forge;

import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.lib.mcgltf.MCglTF;
import sky_bai.mod.tym.network.PlayerModelNetwork;

@Mod(ThisYouModel_Main.MOD_ID)
public class ThisYouModel_Forge {
    public ThisYouModel_Forge() {
        ThisYouModel_Main.init();
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Client {
        @SubscribeEvent
        public static void onClientReloadLoad(RegisterClientReloadListenersEvent event) {

            MCglTF.getInstance().initializeGL();

            event.registerReloadListener((ResourceManagerReloadListener) manager -> {
                MCglTF.getInstance().reloadALLModel();
            });
        }

        @SubscribeEvent
        static void onSetup(FMLClientSetupEvent event) {
            PlayerModelNetwork.client();
        }

    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Server {
        @SubscribeEvent
        public static void onSetup(FMLCommonSetupEvent event) {
            PlayerModelNetwork.server();
        }


    }

}
