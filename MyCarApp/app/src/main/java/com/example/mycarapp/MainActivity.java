package com.example.mycarapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button Begin_Normal,Begin_VR;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Begin_Normal = (Button)findViewById(R.id.button_begin);
        Begin_VR = (Button)findViewById(R.id.button_vr);

        Begin_Normal.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                //生成一个Intent对象
                Intent intent = new Intent();
                //设置Intent对象要启动的Activity
                intent.setClass(MainActivity.this, MyVideo.class);
                //通过Intent对象启动另外一个Activity
                startActivity(intent);
                //finish();
                //System.exit(0);
            }
        });;

        Begin_VR.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MyVideoVR.class);
                startActivity(intent);
                //finish();
                //System.exit(0);
            }
        });;
    }
}

