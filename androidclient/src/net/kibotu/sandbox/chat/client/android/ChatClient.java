package net.kibotu.sandbox.chat.client.android;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.koushikdutta.async.AsyncServer;
import net.kibotu.sandbox.chat.client.android.network.AndroidSocketHandler;
import net.kibotu.sandbox.chat.client.android.network.SocketHandler;

public class ChatClient extends Activity {

    public static final String TAG = ChatClient.class.getSimpleName();
    private String ip;
    private static Activity context;
    private static TextView view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.main);

        //String ip = "http://localhost:3000";
        ip = "http://192.168.2.101:3000";

        view = (TextView) findViewById(R.id.output);
        view.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                connect();
            }
        });

        findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AndroidSocketHandler.Emit("send", AndroidSocketHandler.getJsonObject());
            }
        });
    }

    private void connect() {

        if(AndroidSocketHandler.connectionState != SocketHandler.ConnectionState.DISCONNECTED) return;

        Log.v(TAG, "Starting client");
        try {
            AndroidSocketHandler socket = new AndroidSocketHandler();
            AndroidSocketHandler.connect(ip);
            // socket.testMessageToChat();

        } catch (Exception e) {
            Log.v(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        AsyncServer.getDefault().stop();
        super.onDestroy();
    }

    public static void appendText(final String text) {

        final TextView _view = view;

        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                _view.append(text);
            }
        });
    }
}
