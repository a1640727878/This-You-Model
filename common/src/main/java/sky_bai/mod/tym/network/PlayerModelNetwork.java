package sky_bai.mod.tym.network;

import net.minecraft.resources.ResourceLocation;
import sky_bai.mod.tym.lib.badpackets.api.C2SPacketReceiver;
import sky_bai.mod.tym.lib.badpackets.api.S2CPacketReceiver;
import sky_bai.mod.tym.lib.badpackets.api.event.PacketSenderReadyCallback;
import sky_bai.mod.tym.lib.badpackets.impl.Constants;

public class PlayerModelNetwork {

    public static final ResourceLocation SERVER_TO_CLIENT = Constants.id("tym/player_model/client");

    public static final ResourceLocation CLIENT_TO_SERVER = Constants.id("tym/player_model/server");

    public static void server() {
        C2SPacketReceiver.register(CLIENT_TO_SERVER, (server, player, handler, buf, responseSender) -> {

        });

        PacketSenderReadyCallback.registerServer((handler, sender, server) -> {

        });
    }

    public static void client() {
        S2CPacketReceiver.register(SERVER_TO_CLIENT, (client, handler, buf, responseSender) -> {

        });

        PacketSenderReadyCallback.registerClient((handler, sender, client) -> {

        });
    }

}
