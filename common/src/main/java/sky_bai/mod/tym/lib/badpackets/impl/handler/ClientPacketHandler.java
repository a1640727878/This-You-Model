package sky_bai.mod.tym.lib.badpackets.impl.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import sky_bai.mod.tym.lib.badpackets.api.S2CPacketReceiver;
import sky_bai.mod.tym.lib.badpackets.api.event.PacketSenderReadyCallback;
import sky_bai.mod.tym.lib.badpackets.impl.registry.CallbackRegistry;
import sky_bai.mod.tym.lib.badpackets.impl.registry.ChannelRegistry;

public class ClientPacketHandler extends AbstractPacketHandler<S2CPacketReceiver> {

    private final Minecraft client;
    private final ClientPacketListener listener;

    public ClientPacketHandler(Minecraft client, ClientPacketListener listener) {
        super("ClientPlayPacketHandler", ChannelRegistry.S2C, ServerboundCustomPayloadPacket::new, listener.getConnection());

        this.client = client;
        this.listener = listener;
    }

    public static ClientPacketHandler get() {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            throw new IllegalStateException("Cannot get c2s sender when not in game!");
        }

        return ((Holder) listener).badpackets_getHandler();
    }

    @Override
    protected void onInitialChannelSyncPacketReceived() {
        for (PacketSenderReadyCallback.Client callback : CallbackRegistry.CLIENT_PLAYER_JOIN) {
            callback.onJoin(listener, this, Minecraft.getInstance());
        }
    }

    @Override
    protected void receive(S2CPacketReceiver receiver, FriendlyByteBuf buf) {
        receiver.receive(client, listener, buf, this);
    }

    public interface Holder {

        ClientPacketHandler badpackets_getHandler();

    }

}
