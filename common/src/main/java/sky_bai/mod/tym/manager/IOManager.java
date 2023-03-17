package sky_bai.mod.tym.manager;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class IOManager {

    public final static ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(0);
    public final static Gson GSON = new Gson();

    public static <T> byte[] theObjectToBytes(T obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException ignored) {

        }
        return new byte[0];
    }

    public static <T> T theBytesToObject(byte[] bytes) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException ignored) {

        }
        return null;
    }

    public static FriendlyByteBuf getFriendlyByteBuf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    public static FriendlyByteBuf theStringToByteBuf(String str) {
        return getFriendlyByteBuf().writeByteArray(str.getBytes());
    }

    public static String theByteBufToString(FriendlyByteBuf buf) {
        return new String(buf.readByteArray());
    }

}
