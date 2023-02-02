package cn.zazastudio.tcp;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class TCPHandler extends Handler {

    public static final int READ_DATA = 1;//读取到消息
    public static final int WRITE_DATA = 99;//发送消息
    public static final int CONNECT_SUCCESS = 100;//连接成功
    public static final int CONNECT_BREAK = 300;//断开连接
    public static final int CONNECT_FAILED = 400;//连接失败
    public static boolean CONNECT_STATUS = false;//当前与服务端连接状态

    private Context context;

    public TCPHandler(Context context) {
        this.context = context;
    }

    public TCPHandler(TextView tv_show, Button bt_connect, Button bt_send, Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case CONNECT_SUCCESS:
                Toast.makeText(context, "连接成功！", Toast.LENGTH_LONG).show();
                CONNECT_STATUS = true;
                break;
            case CONNECT_BREAK:
                Toast.makeText(context, "连接断开！", Toast.LENGTH_LONG).show();
                CONNECT_STATUS = false;
                break;
            case CONNECT_FAILED:
                Toast.makeText(context, "连接失败！", Toast.LENGTH_LONG).show();
                CONNECT_STATUS = false;
                break;
            case READ_DATA:
                break;
            case WRITE_DATA:
                break;
        }
    }

}
