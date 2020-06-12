package com.elitecrm.rcclient;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.util.MessageUtils;
import com.elitecrm.rcclient.util.PermissionUtils;

import java.io.File;

public class MainActivity extends Activity {
    private EditText serverAddrEditText;
    private EditText userIdEditText;
    private EditText nameEditText;
    private EditText portraitUriEditText;
    private EditText queueIdEditText;
    private EditText targetIdEditText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView versionView = (TextView)findViewById(R.id.versionView);
        PackageManager pm = this.getPackageManager();
        String versionCode = "unknown";
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(this.getPackageName(), 0);
            versionCode = "v" + pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        versionView.setText(versionCode);

        serverAddrEditText = findViewById(R.id.serverAddrEdittext);
        userIdEditText = findViewById(R.id.userIdEdittext);
        nameEditText = findViewById(R.id.nameEdittext);
        portraitUriEditText = findViewById(R.id.portraitUriEdittext);
        queueIdEditText = findViewById(R.id.queueIdEdittext);
        queueIdEditText.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[] { '1', '2', '3', '4', '5', '6', '7', '8','9', '0'};
            }
            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_PHONE;
            }
        });
        targetIdEditText = findViewById(R.id.targetIdEdittext);

        sharedPreferences = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String serverAddr = sharedPreferences.getString("serverAddr", "http://dev.elitecrm.com/webchat");
        serverAddrEditText.setText(serverAddr);
        String userId = sharedPreferences.getString("userId", "");
        userIdEditText.setText(userId);
        String name = sharedPreferences.getString("name", "");
        nameEditText.setText(name);
        String portraitUri = sharedPreferences.getString("portraitUri", "http://www.elitecrm.com/images/favicon.ico");
        portraitUriEditText.setText(portraitUri);
        int queueId = sharedPreferences.getInt("queueId", 1);
        queueIdEditText.setText(queueId + "");
        String targetId = sharedPreferences.getString("targetId", "Elite");
        targetIdEditText.setText(targetId);

        Button startChatBtn = this.findViewById(R.id.startChat);
        startChatBtn.getBackground().setAlpha(150);
        startChatBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String serverAddr = serverAddrEditText.getText().toString();
                editor.putString("serverAddr", serverAddr);
                String userId = userIdEditText.getText().toString();
                editor.putString("userId", userId);
                String name = nameEditText.getText().toString();
                editor.putString("name", name);
                String portraitUri = portraitUriEditText.getText().toString();
                editor.putString("portraitUri", portraitUri);
                editor.commit();

                int queueId = Integer.parseInt(queueIdEditText.getText().toString());
                editor.putInt("queueId", queueId);

                String target = targetIdEditText.getText().toString();
                // 发自定义消息
//                MessageUtils.sendCustomMessage("{\"name\":\"xxx\"}", target);
                // 发文字
//                MessageUtils.sendTextMessage("firstMsg", target);
                // 发图片
                String imgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/chat.png";
                MessageUtils.sendImgMessage(Uri.fromFile(new File(imgPath)), Uri.fromFile(new File(imgPath)), target);

                EliteChat.startChat(serverAddr, userId, name, portraitUri, target, v.getContext(), queueId, null, "APP", "", "");
            }
        });

        PermissionUtils.verifyLocationPermissions(this);
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
}
