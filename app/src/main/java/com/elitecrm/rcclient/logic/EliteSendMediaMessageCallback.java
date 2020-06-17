package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.util.Constants;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

public class EliteSendMediaMessageCallback implements IRongCallback.ISendMediaMessageCallback {


    @Override
    public void onProgress(Message message, int i) {

    }

    @Override
    public void onCanceled(Message message) {

    }

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
