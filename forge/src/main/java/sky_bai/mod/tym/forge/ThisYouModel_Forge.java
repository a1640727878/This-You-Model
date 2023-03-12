package sky_bai.mod.tym.forge;

import net.minecraftforge.fml.common.Mod;
import sky_bai.mod.tym.ThisYouModel_Main;
import sky_bai.mod.tym.forge.lib.MCglTF_Forge;

@Mod(ThisYouModel_Main.MOD_ID)
public class ThisYouModel_Forge {

    public ThisYouModel_Forge() {
        ThisYouModel_Main.init();
        new MCglTF_Forge();
    }

}
