package sky_bai.mod.tym.lib.badpackets.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import sky_bai.mod.tym.lib.badpackets.impl.marker.ApiSide;
import sky_bai.mod.tym.lib.badpackets.impl.registry.ChannelRegistry;

@ApiSide.ClientOnly
@FunctionalInterface
public interface S2CPacketReceiver {

    @ApiSide.ClientOnly
    static void register(ResourceLocation id, S2CPacketReceiver receiver) {
        ChannelRegistry.S2C.register(id, receiver);
    }

    void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender);

}
