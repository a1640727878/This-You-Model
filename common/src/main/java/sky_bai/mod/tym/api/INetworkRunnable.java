package sky_bai.mod.tym.api;

public interface INetworkRunnable {

    boolean sendToServer(String mes);

    boolean sendToPlayer(String player_uuid, String mes);

    boolean sendToALLPlayer(String mes);

    System receiveToServer();

    String receiveToPlayer();

}
