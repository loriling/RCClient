package com.elitecrm.rcclient.entity;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Message;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Chat {
    private String token;
    private Request request;
    private Client client;
    private Session session;
    private List<Message> unsendMessages = new ArrayList<>();

    private static Chat chat;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    private Chat() {

    }

    public static Chat getInstance() {
        if(chat == null) {
            chat = new Chat();
        }
        return chat;
    }

    public static void initClient(String userId, String name, String portraitUri) {
        Client client = new Client();
        client.setLoginName(userId);
        client.setName(name);
        client.setPortraitUri(portraitUri);
        Chat.getInstance().setClient(client);
    }

    public static void initRequest(int queueId) {
        Request request = new Request(queueId);
        Chat.getInstance().setRequest(request);
    }

    public static void setRequestId(long requestId) {
        Request request = Chat.getInstance().getRequest();
        if(request == null) {
            request = new Request();
            Chat.getInstance().setRequest(request);
        }
        request.setId(requestId);
    }

    public static void addUnsendMessage(Message message) {
        Chat.getInstance().unsendMessages.add(message);
    }

    public static void clearRequestAndSession(){
        Chat.getInstance().setRequest(null);
        Chat.getInstance().setSession(null);
    }
}
