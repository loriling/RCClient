package com.elitecrm.rcclient.entity;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Client extends User {
    private String loginName;
    private int type;
    private String portraitUri;

    public String getPortraitUri() {
        return portraitUri;
    }

    public void setPortraitUri(String portraitUri) {
        this.portraitUri = portraitUri;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
}
