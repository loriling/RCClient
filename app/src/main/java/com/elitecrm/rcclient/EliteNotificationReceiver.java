package com.elitecrm.rcclient;

import android.content.Context;
import android.util.Log;

import com.elitecrm.rcclient.util.Constants;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * Created by Loriling on 2020/6/11.
 */

public class EliteNotificationReceiver extends PushMessageReceiver {

    @Override
    public boolean onNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        Log.d(Constants.LOG_TAG, "onNotificationMessageArrived");
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        Log.d(Constants.LOG_TAG, "onNotificationMessageClicked: " + pushNotificationMessage.getTargetId());
        return false;
    }
}
