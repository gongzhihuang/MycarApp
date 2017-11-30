package com.example.mycarapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MyVideo extends Activity {
    MySurfaceView r;
    private Socket socket;
    OutputStream socketWriter ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//隐去标题（应用的名字必须要写在setContentView之前，否则会有异常）
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.myvideo);
        r = (MySurfaceView) findViewById(R.id.mySurfaceView);

        new Thread() {
            public void run() {
                try {
                    socket = new Socket(InetAddress.getByName(ConnectINFO.IP),Integer.parseInt(ConnectINFO.PORT));
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socketWriter = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void onDestroy()
    {
        super.onDestroy();

    }
}




