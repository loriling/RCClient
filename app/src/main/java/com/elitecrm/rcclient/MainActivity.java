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

import com.elitecrm.rcclient.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Message;

public class MainActivity extends Activity {
    private EditText userIdEditText;
    private EditText nameEditText;
    private EditText portraitUriEditText;
    private EditText queueIdEditText;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View main = findViewById(R.id.activity_main);
        main.getBackground().setAlpha(180);

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

        sharedPreferences = this.getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        userIdEditText.setText(userId);
        String name = sharedPreferences.getString("name", "");
        nameEditText.setText(name);
        String portraitUri = sharedPreferences.getString("portraitUri", "http://www.elitecrm.com/images/favicon.ico");
        portraitUriEditText.setText(portraitUri);
        int queueId = sharedPreferences.getInt("queueId", 1);
        queueIdEditText.setText(queueId + "");

        Button startChatBtn = (Button) this.findViewById(R.id.startChat);
        startChatBtn.getBackground().setAlpha(150);
        startChatBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                String userId = userIdEditText.getText().toString();
                editor.putString("userId", userId);
                String name = nameEditText.getText().toString();
                editor.putString("name", name);
                String portraitUri = portraitUriEditText.getText().toString();
                editor.putString("portraitUri", portraitUri);
                editor.commit();

                int queueId = Integer.parseInt(queueIdEditText.getText().toString());
                editor.putInt("queueId", queueId);

                Message custMessage = MessageUtils.generateCustomMessage("xxxxx");
                List<Message> messages = new ArrayList<Message>();
                messages.add(custMessage);
                EliteChat.initAndStart(getResources().getString(R.string.app_server_addr), userId, name, portraitUri, v.getContext(), queueId, messages);
                //RongIM.getInstance().startConversation(v.getContext(), Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, "在线客服");
            }
        });

    }
}
