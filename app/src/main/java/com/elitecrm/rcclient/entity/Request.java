package com.elitecrm.rcclient.entity;

/**
 * Created by Loriling on 2017/2/15.
 */

public class Request {
    private long id;
    private int queueId;
    private String fromUserId;
    private int status;

    public Request(){

    }
    public Request(int queueId){
        this.queueId = queueId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
