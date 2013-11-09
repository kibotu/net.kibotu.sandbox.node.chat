package net.kibotu.sandbox.chat.client.android.network;

import android.util.Log;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public enum SocketClient {

    instance;
    private static final String TAG = SocketClient.class.getSimpleName();
    private static SocketIOClient client;
    private static SocketHandler socketHandler;

    public static void init(@NotNull final SocketHandler socketHandler) {
        SocketClient.socketHandler = socketHandler;
    }

    public static void connect(@NotNull final String url) {
        if (client != null && client.isConnected())
            return;
        if (socketHandler == null)
            throw new IllegalStateException("No SocketHandler defined. Please use init() before.");

        SocketIORequest req = new SocketIORequest(url);
        req.setLogging("Socket.IO", Log.VERBOSE);

        try {
            client = SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), req, new ConnectCallback() {

                @Override
                public void onConnectCompleted(Exception ex, SocketIOClient client) {

                    client.addListener("message", new EventCallback() {
                        @Override
                        public void onEvent(final JSONArray argument, final Acknowledge acknowledge) {
                            socketHandler.EventCallback(argument, acknowledge);
                        }
                    });

                    client.setStringCallback(new StringCallback() {
                        @Override
                        public void onString(String message, Acknowledge acknowledge) {
                            acknowledge.acknowledge(new JSONArray().put(message));
                            socketHandler.StringCallback(message, acknowledge);
                        }
                    });

                    client.setJSONCallback(new JSONCallback() {
                        @Override
                        public void onJSON(final JSONObject jsonObject, final Acknowledge acknowledge) {
                            acknowledge.acknowledge(jsonObject.names());
                            socketHandler.JSONCallback(jsonObject, acknowledge);
                        }
                    });

                    client.setDisconnectCallback(new DisconnectCallback() {
                        @Override
                        public void onDisconnect(final Exception e) {
                            socketHandler.DisconnectCallback(e);
                        }
                    });

                    client.setErrorCallback(new ErrorCallback() {
                        @Override
                        public void onError(final String error) {
                            socketHandler.ErrorCallback(error);
                        }
                    });

                    client.setReconnectCallback(new ReconnectCallback() {
                        @Override
                        public void onReconnect() {
                            socketHandler.ReconnectCallback();
                        }
                    });
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void Emit(@NotNull final String name, @NotNull final JSONArray args) {
        if (!client.isConnected())
            throw new IllegalStateException("Not connected to the server.");
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
}
