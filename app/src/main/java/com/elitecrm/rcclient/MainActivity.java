package com.elitecrm.rcclient;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化聊天，并启动聊天界面
        EliteChat.initAndStart("http://192.168.2.80:8980/webchat/rcs", "000001", "test1", "", MainActivity.this,  1);
    }
}
