package sky_bai.mod.tym.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class ServerManage {

    private static ServerManage manage;

    private String key;

    private static String sever_key;

    private ServerManage() {
        initialize();
    }

    public static ServerManage getManage() {
        if (manage == null) manage = new ServerManage();
        return manage;
    }

    public static String getSeverKey() {
        return sever_key;
    }

    public static void setSeverKey(String sever_key) {
        ServerManage.sever_key = sever_key;
    }

    public String getKey() {
        return key;
    }

    private void initialize() {
        if (Files.exists(DirectoryManager.SERVER_KEY_DIR)) key = rend();
        else key = create();
    }

    private String rend() {
        try {
            return Files.readString(DirectoryManager.SERVER_KEY_DIR);
        } catch (IOException e) {
            return create();
        }
    }

    private String create() {
        String key = UUID.randomUUID().toString();
        asyncWrite(key);
        return key;
    }

    private void asyncWrite(String key) {
        IOManager.SERVICE.execute(() -> write(key));
    }

    private void write(String key) {
        if (Files.notExists(DirectoryManager.SERVER_KEY_DIR)) {
            IOManager.createFile(DirectoryManager.SERVER_KEY_DIR);
        }
        try {
            Files.writeString(DirectoryManager.SERVER_KEY_DIR, key);
        } catch (IOException ignored) {

        }
    }

}
