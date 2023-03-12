package sky_bai.mod.tym.fabric;

import net.fabricmc.api.ModInitializer;
import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.fabric.lib.MCglTF_Fabric;

public class ThisYouModel_Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ThisYouModel_Main.init();
    }
}
