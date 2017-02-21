package com.elitecrm.rcclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.elitecrm.rcclient.util.ActivityUtils;
import com.elitecrm.rcclient.util.Constants;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button ratingBtn = (Button) this.findViewById(R.id.ratingBtn);
        ratingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ActivityUtils.showRatingDialog((Activity) v.getContext());
            }
        });

        Button startChatBtn = (Button) this.findViewById(R.id.startChat);
        startChatBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                RongIM.getInstance().startConversation(v.getContext(), Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, "在线客服");
            }
        });

        String icon = "https://avatars2.githubusercontent.com/u/445475?v=3&s=460";
        //初始化聊天，并启动聊天界面
        EliteChat.initAndStart("http://192.168.2.80:8980/webchat", "000003", "000003", icon, MainActivity.this,  1);

    }
}
