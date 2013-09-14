package net.kibotu.sandbox.chat.client.android;

import android.util.Log;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.Acknowledge;
import com.koushikdutta.async.http.socketio.JSONCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.koushikdutta.async.http.socketio.StringCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class SocketClient {

    private static final String TAG = SocketClient.class.getSimpleName();
    public String url;
    public SocketIOClient client;

    public SocketClient() {
        url = "http://localhost/";
    }

    public void init() throws ExecutionException, InterruptedException {
        if (client == null) client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), url, null).get();
    }

    private static JSONObject getJsonObject() throws JSONException {
        JSONObject jObject = new JSONObject();
        jObject.put("name", "message");
        jObject.put("message", "hallo welt").put("username", "android");
        return jObject;
    }

    public void testMessageToChat() throws Exception {

        Log.v(TAG, "connect to " + url);

        init();

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
                Log.v(TAG, "onString " + jsonObject + " " + acknowledge);
                acknowledge.acknowledge(jsonObject.names());
            }
        });

        client.emit("send", new JSONArray().put(getJsonObject()));
    }
}
