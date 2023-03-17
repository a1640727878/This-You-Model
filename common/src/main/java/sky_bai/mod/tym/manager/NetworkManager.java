package sky_bai.mod.tym.manager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import sky_bai.mod.tym.lib.badpackets.api.C2SPacketReceiver;
import sky_bai.mod.tym.lib.badpackets.api.PacketSender;
import sky_bai.mod.tym.lib.badpackets.api.S2CPacketReceiver;
import sky_bai.mod.tym.lib.badpackets.api.event.PacketSenderReadyCallback;
import sky_bai.mod.tym.lib.badpackets.impl.Constants;
import sky_bai.mod.tym.lib.badpackets.impl.handler.ClientPacketHandler;
import sky_bai.mod.tym.lib.badpackets.impl.handler.ServerPacketHandler;
import sky_bai.mod.tym.lib.mcgltf.MCglTF;

import java.util.Map;

public class NetworkManager {

    static NetworkManager manager;

    // 收实例 解压加载
    public final S2C S2C_SEND_SERVER_MODEL = new S2C("s2c/send_server_model", (client, handler, buf, responseSender) -> {
        Map<String, byte[]> server_models = GlTFModelManager.getManager().toBytesFoModels(buf.readByteArray());
        for (Map.Entry<String, byte[]> entry : server_models.entrySet()) {
            GlTFModelManager.getManager().addModelByte(entry.getKey(), entry.getValue());
        }
        MCglTF.getInstance().reloadManager();
    });

    // 收到目录发生模型实例
    public final C2S C2S_GET_SERVER_MODEL = new C2S("c2s/get_server_model", (server, player, handler, buf, responseSender) -> {
        byte[] server_models = GlTFModelManager.getManager().getServerModelsBytes(IOManager.theByteBufToString(buf));
        S2C_SEND_SERVER_MODEL.send(player, IOManager.getFriendlyByteBuf().writeByteArray(server_models));
    });

    // 客户端接收到 并向发送没有的模型目录
    public final S2C S2C_SEND_S2C_MODELS = new S2C("s2c/send_server_models", (client, handler, buf, responseSender) -> {
        String names = IOManager.theByteBufToString(buf);
        GlTFModelManager manager = GlTFModelManager.getManager();
        String noModel = manager.removeModelAndGetNoModel(names);
        C2S_GET_SERVER_MODEL.send(IOManager.theStringToByteBuf(noModel));
    });

    public final S2C S2C_GET_PLAYER_MODEL = new S2C("s2c/get_payer_model", (client, handler, buf, responseSender) -> {

    });

    public final S2C S2C_SEND_KEY = new S2C("s2c/send_key", (client, handler, buf, responseSender) -> {

    });

    // 设置玩家模型
    public final S2C S2C_SET_PLAYER_MODEL = new S2C("s2c/set_player_model", (client, handler, buf, responseSender) -> {
        String[] keys = IOManager.theByteBufToString(buf).split("<->");
        PlayerModelManager.getManager().set(keys[0], keys[1]);
    });

    public static NetworkManager getManager() {
        if (manager == null) manager = new NetworkManager();
        return manager;
    }

    public void registerC2S() {
        C2S_GET_SERVER_MODEL.register();

        // 玩家进入服务器 服务器向客户端发送服务器的模型目录
        PacketSenderReadyCallback.registerServer((handler, sender, server) -> {
            String names = GlTFModelManager.getManager().getModelNamesString();
            S2C_SEND_S2C_MODELS.send(sender, IOManager.theStringToByteBuf(names));
        });
    }

    public void registerS2C() {
        S2C_SET_PLAYER_MODEL.register();
        S2C_GET_PLAYER_MODEL.register();
        S2C_SEND_KEY.register();

        PacketSenderReadyCallback.registerClient((handler, sender, client) -> {

        });
    }

    public interface IPackageData {
        ResourceLocation getID();

        void register();

        void send(PacketSender sender, FriendlyByteBuf buf);
    }

    public abstract class NetworkData implements IPackageData {

        final ResourceLocation id;

        public NetworkData(String id) {
            this.id = Constants.id(id);
        }

        @Override
        public ResourceLocation getID() {
            return id;
        }
    }

    public class S2C extends NetworkData {
        final S2CPacketReceiver receiver;

        public S2C(String id, S2CPacketReceiver receiver) {
            super(id);
            this.receiver = receiver;
        }

        @Override
        public void register() {
            S2CPacketReceiver.register(getID(), receiver);
        }

        @Override
        public void send(PacketSender sender, FriendlyByteBuf buf) {
            sender.send(getID(), buf);
        }

        public void send(ServerPlayer player, FriendlyByteBuf buf) {
            ServerPacketHandler.get(player).send(getID(), buf);
        }
    }

    public class C2S extends NetworkData {

        final C2SPacketReceiver receiver;

        public C2S(String id, C2SPacketReceiver receiver) {
            super(id);
            this.receiver = receiver;
        }

        @Override
        public void register() {
            C2SPacketReceiver.register(getID(), receiver);
        }

        @Override
        public void send(PacketSender sender, FriendlyByteBuf buf) {
            sender.send(getID(), buf);
        }

        public void send(FriendlyByteBuf buf) {
            ClientPacketHandler.get().send(getID(), buf);
        }

    }

}
