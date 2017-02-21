package com.elitecrm.rcclient.entity;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Agent extends User{
    private String comments;

    public Agent(String id, String name, String icon) {
        this.setId(id);
        this.setName(name);
        this.setIcon(icon);
    }
    public Agent(String id, String name, String icon, String comments) {
        this.setId(id);
        this.setName(name);
        this.setIcon(icon);
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
