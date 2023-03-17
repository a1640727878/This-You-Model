package sky_bai.mod.tym.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.network.PlayerModelNetwork;

public class ThisYouModel_Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ThisYouModel_Main.init();
        PlayerModelNetwork.server();
    }
}
