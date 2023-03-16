package sky_bai.mod.tym.manager;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class IOManager {

    public final static ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(0);
    public final static Gson GSON = new Gson();

}
