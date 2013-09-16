package net.kibotu.sandbox.chat.client.android;

import android.util.Log;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SocketClient {

    private static final String TAG = SocketClient.class.getSimpleName();
    public SocketIOClient client;

    public SocketClient(@NotNull String url) throws ExecutionException, InterruptedException {
        client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), url, null).get();
    }

    private static JSONObject getJsonObject() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put("name", "message");
        jObject.put("message", "hallo welt").put("username", "android");
        return jObject;
    }

    public void testMessageToChat() throws Exception {

        client.setStringCallback(new StringCallback() {
            @Override
            public void onString(String string, Acknowledge acknowledge) {
                Log.v(TAG, "onString " + string + " " + acknowledge);
                acknowledge.acknowledge(new JSONArray().put(string));
            }
        });

        client.setJSONCallback(new JSONCallback() {
            @Override
            public void onJSON(final JSONObject jsonObject, final Acknowledge acknowledge) {
                Log.v(TAG, "onJson " + jsonObject + " " + acknowledge);
                acknowledge.acknowledge(jsonObject.names());
            }
        });

        client.setDisconnectCallback(new DisconnectCallback() {
            @Override
            public void onDisconnect(final Exception e) {
                Log.v(TAG, "onDisconnect " + e.getMessage());
            }
        });

        client.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(final String error) {
                Log.v(TAG, "onError " + error);
            }
        });

        client.setReconnectCallback(new ReconnectCallback() {
            @Override
            public void onReconnect() {
                Log.v(TAG, "onReconnect");
            }
        });

        client.emit("send", new JSONArray().put(getJsonObject()));
    }
}
