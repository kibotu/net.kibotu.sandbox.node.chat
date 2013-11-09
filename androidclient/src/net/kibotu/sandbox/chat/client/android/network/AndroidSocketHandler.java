package net.kibotu.sandbox.chat.client.android.network;

import android.util.Log;
import com.koushikdutta.async.http.socketio.*;
import net.kibotu.sandbox.chat.client.android.ChatClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AndroidSocketHandler extends SocketHandler {

    private static final String TAG = AndroidSocketHandler.class.getSimpleName();

    @Override
    protected void EventCallback(final JSONArray argument, final Acknowledge acknowledge) {
        Log.v(TAG, "onEvent 'send' ack = " + acknowledge.toString() + " args = " + argument.toString());
        ChatClient.appendText("onEvent 'send' ack = " + acknowledge.toString() + " args = " + argument.toString());
    }

    @Override
    protected void StringCallback(final String message, final Acknowledge acknowledge) {
        Log.v(TAG, "onString " + message + " " + acknowledge);
        ChatClient.appendText("onString " + message + " " + acknowledge);
    }

    @Override
    protected void JSONCallback(final JSONObject jsonObject, final Acknowledge acknowledge) {
        Log.v(TAG, "onJson " + jsonObject + " " + acknowledge);
        ChatClient.appendText("onJson " + jsonObject + " " + acknowledge);
    }

    @Override
    protected void DisconnectCallback(final Exception e) {
        Log.v(TAG, "onDisconnect " + e.getMessage());
        ChatClient.appendText("onDisconnect " + e.getMessage());
    }

    @Override
    protected void ErrorCallback(final String error) {
        Log.v(TAG, "onError " + error);
        ChatClient.appendText("onError " + error);
    }

    @Override
    protected void ReconnectCallback() {
        Log.v(TAG, "onReconnect");
        ChatClient.appendText("onReconnect");
    }

    public static JSONObject getJsonObject() {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("name", "message");
            jObject.put("message", "hallo welt").put("username", "android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
