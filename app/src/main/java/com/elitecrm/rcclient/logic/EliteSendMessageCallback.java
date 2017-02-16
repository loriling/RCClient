package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.util.Constants;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by ThinkPad on 2017/2/9.
 */

public class EliteSendMessageCallback implements IRongCallback.ISendMessageCallback {
    @Override
    public void onAttached(Message message) {

    }

    @Override
    public void onSuccess(Message message) {

    }

    @Override
    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
        Log.e(Constants.LOG_TAG, "[" + errorCode + "] " + message.toString());
    }
}
