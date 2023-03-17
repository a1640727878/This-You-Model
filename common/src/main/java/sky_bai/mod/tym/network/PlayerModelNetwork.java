package sky_bai.mod.tym.network;

import sky_bai.mod.tym.manager.NetworkManager;

public class PlayerModelNetwork {

    public static void server() {
        NetworkManager.getManager().registerC2S();
    }

    public static void client() {
        NetworkManager.getManager().registerS2C();
    }

}
