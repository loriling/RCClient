package com.elitecrm.rcclient;

import android.content.Context;
import android.util.Log;

import com.elitecrm.rcclient.util.Constants;

import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * Created by Loriling on 2017/8/23.
 */

public class EliteNotificationReceiver extends PushMessageReceiver {
    @Override
    public boolean onNotificationMessageArrived(Context context, PushNotificationMessage message) {
        Log.d(Constants.LOG_TAG, "onNotificationMessageArrived");
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushNotificationMessage message) {
        Log.d(Constants.LOG_TAG, "onNotificationMessageClicked: " + message.getTargetId());
        //EliteChat.switchToChat(context, message.getTargetId());
        //这里需要用EventBus来发出事件，保证context是相同的
        return false;
    }
}
