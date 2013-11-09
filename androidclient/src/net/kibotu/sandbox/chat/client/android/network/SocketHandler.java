package net.kibotu.sandbox.chat.client.android.network;

import android.util.Log;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.*;
import net.kibotu.sandbox.chat.client.android.ChatClient;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public abstract class SocketHandler {

    private static final String TAG = SocketHandler.class.getSimpleName();
    public static SocketIOClient client;
    public static ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public SocketHandler() {
    }

    public static void connect(@NotNull final String url) {
        try {
            Log.v(TAG, "connect to: " + url);
            ChatClient.appendText("connect to: " + url);
            connectionState = ConnectionState.CONNECTING;
            client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), url, new ConnectCallback() {
                @Override
                public void onConnectCompleted(final Exception ex, final SocketIOClient client) {
                    ChatClient.appendText("Connected.");
                    connectionState = ConnectionState.CONNECTED;
                    client.addListener("message", new EventCallback() {
                        @Override
                        public void onEvent(final JSONArray argument, final Acknowledge acknowledge) {
                            Log.v(TAG, "onEvent 'send' ack = " + acknowledge.toString() + " args = " + argument.toString());
                            ChatClient.appendText("onEvent 'send' ack = " + acknowledge.toString() + " args = " + argument.toString());
                        }
                    });
                }
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            connectionState = ConnectionState.DISCONNECTED;
            e.printStackTrace();
        }
    }

    public static void Emit(@NotNull final String name, @NotNull final JSONArray args) {
        //if (connectionState != ConnectionState.CONNECTED) return;
        Log.v(TAG, "Emit: " + name);
        ChatClient.appendText("Emit: " + name);
        client.emit(name, args);
    }

    public static void Emit(@NotNull final String name, @NotNull final JSONObject args) {
        Emit(name, new JSONArray().put(args));
    }

    public static void Emit(@NotNull final String name, @NotNull final String args) {
        try {
            Emit(name, new JSONObject(args));
        } catch (JSONException e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract void EventCallback(final JSONArray argument, final Acknowledge acknowledge);

    protected abstract void StringCallback(final String message, final Acknowledge acknowledge);

    protected abstract void JSONCallback(final JSONObject jsonObject, final Acknowledge acknowledge);

    protected abstract void DisconnectCallback(final Exception e);

    protected abstract void ErrorCallback(final String error);

    protected abstract void ReconnectCallback();

    public void initCallbacks() {

        client.addListener("message", new EventCallback() {
            @Override
            public void onEvent(final JSONArray argument, final Acknowledge acknowledge) {
                EventCallback(argument, acknowledge);
            }
        });

        /*client.on("send", new EventCallback() {
            @Override
            public void onEvent(final JSONArray argument, final Acknowledge acknowledge) {
                EventCallback(argument, acknowledge);
            }
        });



        client.on("message", new EventCallback() {
            @Override
            public void onEvent(final JSONArray argument, final Acknowledge acknowledge) {
                EventCallback(argument, acknowledge);
            }
        });  */

        client.setStringCallback(new StringCallback() {
            @Override
            public void onString(String message, Acknowledge acknowledge) {
                acknowledge.acknowledge(new JSONArray().put(message));
                StringCallback(message, acknowledge);
            }
        });

        client.setJSONCallback(new JSONCallback() {
            @Override
            public void onJSON(final JSONObject jsonObject, final Acknowledge acknowledge) {
                acknowledge.acknowledge(jsonObject.names());
                JSONCallback(jsonObject, acknowledge);
            }
        });

        client.setDisconnectCallback(new DisconnectCallback() {
            @Override
            public void onDisconnect(final Exception e) {
                DisconnectCallback(e);
            }
        });

        client.setErrorCallback(new ErrorCallback() {
            @Override
            public void onError(final String error) {
                ErrorCallback(error);
            }
        });

        client.setReconnectCallback(new ReconnectCallback() {
            @Override
            public void onReconnect() {
                ReconnectCallback();
            }
        });
    }

    public enum ConnectionState {
        CONNECTED, CONNECTING, DISCONNECTED;
    }
}
