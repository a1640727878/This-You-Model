package sky_bai.mod.tym;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sky_bai.mod.tym.manager.PlayerModelManager;

public class ThisYouModel_Main {
    public static final String MOD_ID = "this_you_model";
    public static final Logger LOGGER = LogManager.getLogger(ThisYouModel_Main.MOD_ID);

    public static void init() {
        PlayerModelManager.getManager().write();
    }
}

