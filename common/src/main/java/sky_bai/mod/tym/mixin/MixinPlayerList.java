package sky_bai.mod.tym.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sky_bai.mod.tym.lib.badpackets.impl.handler.ServerPacketHandler;

@Mixin(PlayerList.class)
public class MixinPlayerList {

    @Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)V"))
    private void badpackets_initServerPlayPacketHandler(Connection netManager, ServerPlayer player, CallbackInfo ci) {
        ServerPacketHandler.get(player).sendInitialChannelSyncPacket();
    }

}
