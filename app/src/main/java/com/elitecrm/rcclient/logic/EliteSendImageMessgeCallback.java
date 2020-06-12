package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.util.Constants;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

public class EliteSendImageMessgeCallback extends RongIMClient.SendImageMessageCallback {
    @Override
    public void onAttached(Message message) {

    }

    @Override
    public void onError(Message message, RongIMClient.ErrorCode errorCode) {
        Log.e(Constants.LOG_TAG, "[" + errorCode + "] " + message.toString());
    }

    @Override
    public void onSuccess(Message message) {

    }

    @Override
    public void onProgress(Message message, int i) {

    }
}
