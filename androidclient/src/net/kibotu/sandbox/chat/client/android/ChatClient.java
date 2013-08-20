package net.kibotu.sandbox.chat.client.android;

import android.app.Activity;
import android.os.Bundle;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChatClient extends Activity {

    public static final long TIMEOUT = 10000L;
    private static final String url = "http://178.5.164.68:7331/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        System.out.println("Starting client");
        try {
            //testMessageToChat();
            testSendAcknowledge();
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testMessageToChat() throws Exception {
        final JSONObject message = new JSONObject();
        try {
            JSONArray args = new JSONArray();
            args.put(new JSONObject().put("message", "hallo").put("username", "android"));
            message.put("send", "message");
            message.put("args", args);
        } catch (JSONException e) {
            System.out.println("JSONException: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("connect to " + url);

        final ConnectCallback cc = new ConnectCallback() {
            @Override
            public void onConnectCompleted(final Exception e, final SocketIOClient client) {
                System.out.println("onConnectCompleted");

                client.on("message", new EventCallback() {
                    @Override
                    public void onEvent(JSONArray argument, Acknowledge acknowledge) {
                        System.out.println("onEvent " + argument + " " + acknowledge);
                        acknowledge.acknowledge(argument);
                        client.emit("send", acknowledge);
                    }
                });

                client.emit(message);
            }
        };

        final SocketIOClient client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), url, cc).get();
    }

    public void testSendAcknowledge() throws Exception {

        System.out.println("connect to " + url);

        SocketIOClient client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), url, null).get();

        client.setStringCallback(new StringCallback() {

            @Override
            public void onString(String string, Acknowledge acknowledge) {
                System.out.println("onString " + string + " " + acknowledge);
                acknowledge.acknowledge(new JSONArray().put(string));
            }
        });

        client.setJSONCallback(new JSONCallback() {

            @Override
            public void onJSON(final JSONObject jsonObject, final Acknowledge acknowledge) {
                System.out.println("onString " + jsonObject + " " + acknowledge);
                acknowledge.acknowledge(jsonObject.names());
            }
        });

        client.emit("send", new JSONArray().put(getJsonObject()));
    }

    private static JSONObject getJsonObject() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put("name", "message");
        jObject.put("message", "hallo welt").put("username", "android");
        return jObject;
    }

    @Override
    public void onDestroy() {
        AsyncServer.getDefault().stop();
    }


}
