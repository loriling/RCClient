package com.elitecrm.rcclient.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.elitecrm.rcclient.baidumap.BaiduLocationListActivity;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.LocationMessage;

/**
 * Created by Loriling on 2017/2/15.
 */

public class EliteConversationFragment extends ConversationFragment implements RongIM.LocationProvider,RongIM.ConversationBehaviorListener{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置地理位置监听事件
        RongIM.setLocationProvider(this);
        RongIM.setConversationBehaviorListener(this);//设置会话界面操作的监听器。
    }

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo) {
        return false;
    }

    @Override
    public boolean onMessageClick(Context context, View view, Message message) {
        // 消息点击事件，判断如果是位置消息就取出Content()跳转到地图activity
        if (message.getContent() instanceof LocationMessage) {
            Intent intent = new Intent(getActivity(), BaiduLocationListActivity.class);
            intent.putExtra("location", message.getContent());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {

    }
}
