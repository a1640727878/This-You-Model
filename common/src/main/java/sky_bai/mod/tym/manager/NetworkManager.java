package sky_bai.mod.tym.manager;

import net.minecraft.client.Minecraft;
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

public class NetworkManager {
    static NetworkManager manager;

    // 设置玩家模型
    public final S2C S2C_SET_PLAYER_MODEL = new S2C("s2c/set_player_model", (client, handler, buf, responseSender) -> {
        String[] keys = IOManager.theByteBufToString(buf).split("<->");
        PlayerModelManager.getManager().set(keys[0], keys[1]);
    });

    // 服务器知道好了~ 广播全服玩家这个玩家在服务器的模型
    public final C2S C2S_SEND_SERVER_MODEL_OK = new C2S("c2s/send_server_model_ok", (server, player, handler, buf, responseSender) -> {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            String p_uuid = p.getStringUUID();
            String model_name = PlayerModelManager.getManager().getModelName(p_uuid);
            S2C_SET_PLAYER_MODEL.send(p,
                    IOManager.theStringToByteBuf(p_uuid + "<->" + model_name));
        }
    });

    // 收实例 解压加载 然后告诉服务器好了~
    public final S2C S2C_SEND_SERVER_MODEL_DATA = new S2C("s2c/send_server_model", (client, handler, buf, responseSender) -> {
        byte[] bytes = buf.readByteArray();
        byte[] un_gz = GzipManager.unGzip(bytes);
        FileModelManager manager = FileModelManager.getManager();

        manager.loadSeverFileModels(un_gz);

        GlTFModelManager.getManager().loadServerModels();
        C2S_SEND_SERVER_MODEL_OK.send(IOManager.getFriendlyByteBuf());

        Minecraft.getInstance().reloadResourcePacks();
    });

    // 收到目录发生模型实例
    public final C2S C2S_GET_SERVER_MODEL_DATA = new C2S("c2s/get_server_model", (server, player, handler, buf, responseSender) -> {
        String noModel = new String(buf.readByteArray());
        byte[] server_models = FileModelManager.getManager().generateNoModelBytes(noModel);
        byte[] gz_server_models = GzipManager.gzip(server_models);
        S2C_SEND_SERVER_MODEL_DATA.send(player, IOManager.getFriendlyByteBuf().writeByteArray(gz_server_models));
    });

    // 客户端接收到 并向发送没有的模型目录
    public final S2C S2C_SEND_SERVER_MODELS = new S2C("s2c/send_server_models", (client, handler, buf, responseSender) -> {
        String names = IOManager.theByteBufToString(buf);
        String noModel = FileModelManager.getManager().getNoModelString(names);
        FriendlyByteBuf byteBuf = IOManager.theStringToByteBuf(noModel);
        C2S_GET_SERVER_MODEL_DATA.send(byteBuf);
    });

    // 知道玩家OK 发生服务器模型目录
    public final C2S C2S_KEY_OK = new C2S("s2c/key_ok", (server, player, handler, buf, responseSender) -> {
        // String names = GlTFModelManager_Old.getManager().getModelNamesString();
        String names = FileModelManager.getManager().generateServerFileNames();
        S2C_SEND_SERVER_MODELS.send(player, IOManager.theStringToByteBuf(names));
    });

    // 收到key 检查本身缓存 并告诉服务器收到
    public final S2C S2C_SEND_KEY = new S2C("s2c/send_key", (client, handler, buf, responseSender) -> {
        ServerManage.setSeverKey(IOManager.theByteBufToString(buf));
        FileModelManager manager = FileModelManager.getManager();
        manager.loadSeverFileModels(manager.getCacheSeverFileModels());
        C2S_KEY_OK.send(IOManager.getFriendlyByteBuf());
    });

    public static NetworkManager getManager() {
        if (manager == null) manager = new NetworkManager();
        return manager;
    }

    public void registerC2S() {
        C2S_GET_SERVER_MODEL_DATA.register();
        C2S_KEY_OK.register();
        C2S_SEND_SERVER_MODEL_OK.register();

        // 玩家进入服务器 向玩家发生服务器的Key
        PacketSenderReadyCallback.registerServer((handler, sender, server) -> {
            S2C_SEND_KEY.send(sender, IOManager.theStringToByteBuf(ServerManage.getManage().getKey()));
        });
    }

    public void registerS2C() {
        S2C_SEND_SERVER_MODEL_DATA.register();
        S2C_SEND_SERVER_MODELS.register();
        S2C_SET_PLAYER_MODEL.register();
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
