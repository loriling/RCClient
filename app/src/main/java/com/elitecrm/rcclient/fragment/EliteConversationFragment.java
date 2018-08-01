package com.elitecrm.rcclient.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.elitecrm.rcclient.R;
import com.elitecrm.rcclient.baidumap.BaiduLocationListActivity;
import com.elitecrm.rcclient.message.RobotMessage;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;

/**
 * Created by Loriling on 2017/2/15.
 */

public class EliteConversationFragment extends ConversationFragment implements RongIM.LocationProvider,RongIM.ConversationClickListener{

    private ListView listView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置地理位置监听事件
        RongIM.setLocationProvider(this);
        RongIM.setConversationClickListener(this);//设置会话界面操作的监听器。
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.rc_list);
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
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }

    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {

    }

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onMessageLinkClick(Context context, String s, Message message) {
        return false;
    }

    public void onEventMainThread(io.rong.imlib.model.Message msg) {
        super.onEventMainThread(msg);
        MessageContent messageContent = msg.getContent();
        //如果是InformationNotificationMessage或者RobotMessage消息，则触发滚动到底部
        if (messageContent instanceof InformationNotificationMessage || messageContent instanceof RobotMessage) {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null && listView != null) {
                        listView.setSelection(listView.getAdapter().getCount());
                    }
                }
            });
        }
    }
}
