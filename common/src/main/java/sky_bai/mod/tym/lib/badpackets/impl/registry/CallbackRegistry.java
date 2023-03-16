package sky_bai.mod.tym.lib.badpackets.impl.registry;

import sky_bai.mod.tym.lib.badpackets.api.event.PacketSenderReadyCallback;

import java.util.ArrayList;

public class CallbackRegistry {

    public static final ArrayList<PacketSenderReadyCallback.Client> CLIENT_PLAYER_JOIN = new ArrayList<>();
    public static final ArrayList<PacketSenderReadyCallback.Server> SERVER_PLAYER_JOIN = new ArrayList<>();

}
