package net.kibotu.sandbox.chat.client.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.koushikdutta.async.AsyncServer;

public class ChatClient extends Activity {

    public static final String TAG = ChatClient.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v(TAG, "Starting client");
        try {
            SocketClient socket = new SocketClient("http://localhost:3000");
            socket.testMessageToChat();
        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        AsyncServer.getDefault().stop();
    }
}
