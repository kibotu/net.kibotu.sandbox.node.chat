package net.kibotu.sandbox.chat.client.android.network;

import com.koushikdutta.async.http.socketio.Acknowledge;
import net.kibotu.sandbox.chat.client.android.ChatClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AndroidSocketHandler implements SocketHandler {

    private static final String TAG = AndroidSocketHandler.class.getSimpleName();

    public static JSONObject getJsonObject(String name, String message) {

        JSONObject jObject = new JSONObject();
        try {
            jObject.put("name", "message");
            jObject.put("message", message).put("username", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject;
    }

    private boolean udp_isRunning = false;

    @Override
    public void EventCallback(final JSONArray argument, final Acknowledge acknowledge) {
        ChatClient.appendText("onEvent 'send' ack = " + acknowledge + " args = " + argument);

        if(!udp_isRunning) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String messageStr = "message";
                    int server_port = 7788;
                    String ip = "192.168.2.101";
                    try {
                        DatagramSocket s = new DatagramSocket();
                        InetAddress local = InetAddress.getByName(ip);
                        int msg_length = messageStr.length();
                        byte[] message = messageStr.getBytes();
                        DatagramPacket p = new DatagramPacket(message, msg_length, local,server_port);
                        s.send(p);
                        ChatClient.appendText(p.toString());
                    } catch (Exception e) {
                         e.printStackTrace();
                    }
                }
            }).start();
            udp_isRunning = true;
        }
    }

    @Override
    public void StringCallback(final String message, final Acknowledge acknowledge) {
        ChatClient.appendText("onString " + message + " " + acknowledge);
    }

    @Override
    public void JSONCallback(final JSONObject jsonObject, final Acknowledge acknowledge) {
        ChatClient.appendText("onJson " + jsonObject + " " + acknowledge);
    }

    @Override
    public void DisconnectCallback(final Exception e) {
        ChatClient.appendText("onDisconnect " + e.getMessage());
    }

    @Override
    public void ErrorCallback(final String error) {
        ChatClient.appendText("onError " + error);
    }

    @Override
    public void ReconnectCallback() {
        ChatClient.appendText("onReconnect");
    }
}
