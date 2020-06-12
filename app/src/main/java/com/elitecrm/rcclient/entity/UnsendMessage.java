package com.elitecrm.rcclient.entity;

import io.rong.imlib.model.Message;

public class UnsendMessage {
    public static final int NORMAL = 0; // 通过页面发送
    public static final int API = 1; // 通过api发送
    private int type = NORMAL;
    private Message message;

    public UnsendMessage(int type, Message message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
