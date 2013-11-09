package net.kibotu.sandbox.chat.client.android.network;

import com.koushikdutta.async.http.socketio.Acknowledge;
import net.kibotu.sandbox.chat.client.android.ChatClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    @Override
    public void EventCallback(final JSONArray argument, final Acknowledge acknowledge) {
        ChatClient.appendText("onEvent 'send' ack = " + acknowledge + " args = " + argument);
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
        ChatClient.appendText("onDisconnect " + ((e != null) ? e.getMessage() : ""));
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
