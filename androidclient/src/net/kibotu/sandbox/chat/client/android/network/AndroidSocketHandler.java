package net.kibotu.sandbox.chat.client.android.network;

import com.koushikdutta.async.http.socketio.Acknowledge;
import net.kibotu.sandbox.chat.client.android.ChatClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class AndroidSocketHandler implements SocketHandler {

    @Override
    public void EventCallback(final JSONArray argument, final Acknowledge acknowledge) {
        ChatClient.appendText(""+argument);
    }

    @Override
    public void StringCallback(final String message, final Acknowledge acknowledge) {
        ChatClient.appendText(""+message);
    }

    @Override
    public void JSONCallback(final JSONObject jsonObject, final Acknowledge acknowledge) {
        ChatClient.appendText(""+jsonObject);
    }

    @Override
    public void DisconnectCallback(final Exception e) {
        ChatClient.appendText("Disconnected " + ((e != null) ? e.getMessage() : ""));
    }

    @Override
    public void ErrorCallback(final String error) {
        ChatClient.appendText("Error " + error);
    }

    @Override
    public void ReconnectCallback() {
        ChatClient.appendText("Reconnect");
    }
}
