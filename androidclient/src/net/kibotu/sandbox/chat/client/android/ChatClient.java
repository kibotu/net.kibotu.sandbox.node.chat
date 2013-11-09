package net.kibotu.sandbox.chat.client.android;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.koushikdutta.async.AsyncServer;
import net.kibotu.sandbox.chat.client.android.network.AndroidSocketHandler;
import net.kibotu.sandbox.chat.client.android.network.SocketClient;
import org.json.JSONObject;

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
        SocketClient.init(new AndroidSocketHandler());

        view = (TextView) findViewById(R.id.output);
        view.setMovementMethod(new ScrollingMovementMethod());

        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                SocketClient.connect(ip);
            }
        });

        findViewById(R.id.reconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                SocketClient.reconnect();
            }
        });

        findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                SocketClient.disconnect();
            }
        });

        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                JSONObject getJsonObject = AndroidSocketHandler.getJsonObject("android", ((EditText)findViewById(R.id.editText)).getText().toString());
                SocketClient.Emit("send", getJsonObject);
            }
         });
    }

    @Override
    public void onDestroy() {
        AsyncServer.getDefault().stop();
        super.onDestroy();
    }

    public static void appendText(final String text) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.append(text);
                view.append("\n");
            }
        });
    }
}
