package com.elitecrm.rcclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.elitecrm.rcclient.entity.Chat;

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

        serverAddrEditText = (EditText)findViewById(R.id.serverAddrEdittext);
        userIdEditText = (EditText)findViewById(R.id.userIdEdittext);
        nameEditText = (EditText)findViewById(R.id.nameEdittext);
        portraitUriEditText = (EditText)findViewById(R.id.portraitUriEdittext);
        queueIdEditText = (EditText)findViewById(R.id.queueIdEdittext);
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
        targetIdEditText = (EditText)findViewById(R.id.targetIdEdittext);

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

        Button startChatBtn = (Button) this.findViewById(R.id.startChat);
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
                //MessageUtils.sendCustomMessage("{\"name\":\"xxx\"}", target);
                //MessageUtils.sendTextMessage("firstMsg", target);

                EliteChat.startChat(serverAddr, Chat.getInstance().getToken(), userId, name, portraitUri, target, v.getContext(), queueId, null, "APP", "");
            }
        });

    }
}
