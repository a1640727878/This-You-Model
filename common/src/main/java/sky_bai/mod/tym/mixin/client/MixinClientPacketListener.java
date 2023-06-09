package sky_bai.mod.tym.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sky_bai.mod.tym.lib.badpackets.impl.handler.ClientPacketHandler;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener implements ClientPacketHandler.Holder {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private ClientPacketHandler badpacket_packetHandler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void badpackets_createClientPacketHandler(Minecraft minecraft, Screen screen, Connection connection, GameProfile gameProfile, ClientTelemetryManager clientTelemetryManager, CallbackInfo ci) {
        badpacket_packetHandler = new ClientPacketHandler(minecraft, (ClientPacketListener) (Object) this);
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void badpackets_removeClientPacketHandler(Component reason, CallbackInfo ci) {
        badpacket_packetHandler.onDisconnect();
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void badpackets_initClientPacketHandler(ClientboundLoginPacket packet, CallbackInfo ci) {
        badpacket_packetHandler.sendInitialChannelSyncPacket();
    }

    @Inject(method = "handleCustomPayload", at = @At("HEAD"), cancellable = true)
    private void badpackets_receiveS2CPacket(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {
        if (!minecraft.isSameThread()) {
            FriendlyByteBuf buf = packet.getData();
            try {
                if (badpacket_packetHandler.receive(packet.getIdentifier(), buf)) {
                    ci.cancel();
                }
            } finally {
                buf.release();
            }
        }
    }

    @Override
    public ClientPacketHandler badpackets_getHandler() {
        return badpacket_packetHandler;
    }

}
