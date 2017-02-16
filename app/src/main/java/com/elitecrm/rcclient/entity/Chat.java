package com.elitecrm.rcclient.entity;

import android.util.Log;

import com.elitecrm.rcclient.logic.EliteSendMessageCallback;
import com.elitecrm.rcclient.util.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Chat {
    // 最大为发送消息存储量，缓存过多消息怕出问题
    private static final int MAX_UNSEND_MESSAGES = 50;
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
        request.setStatus(Constants.RequestStatus.WAITING);
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

    public static void setRequestStatus(int status) {
        Request request = Chat.getInstance().getRequest();
        request.setStatus(status);
    }

    public static void addUnsendMessage(Message message) {
        List<Message> messages = Chat.getInstance().unsendMessages;
        if(messages.size() >= MAX_UNSEND_MESSAGES){
            messages.remove(0);
        }
        Chat.getInstance().unsendMessages.add(message);
    }

    public static List<Message> getUnsendMessages(){
        return Chat.getInstance().unsendMessages;
    }

    /**
     * 发送之前未送达的消,当排队之前发出的消息,会先缓存起来，如果排上队了，就会补发这些消息
     */
    public static void sendUnsendMessages() {
        List<Message> messages = Chat.getInstance().unsendMessages;
        if(messages.size() > 0) {
            for(Message message : messages) {
                MessageContent messageContent = message.getContent();
                if(messageContent instanceof TextMessage) {
                    try {
                        TextMessage textMessage = (TextMessage)messageContent;
                        EliteMessage eliteMessage = new EliteMessage();
                        eliteMessage.setMessage(textMessage.getContent());
                        JSONObject extraJSON = new JSONObject();
                        extraJSON.put("token", Chat.getInstance().getToken());
                        extraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                        extraJSON.put("type", Constants.RequestType.SEND_CHAT_MESSAGE);
                        extraJSON.put("messageType", Constants.MessageType.TEXT);
                        eliteMessage.setExtra(extraJSON.toString());
                        Message lastMessage = Message.obtain(Constants.CHAT_TARGET_ID, Conversation.ConversationType.SYSTEM, eliteMessage);
                        RongIM.getInstance().sendMessage(lastMessage, null, null, new EliteSendMessageCallback());
                    } catch (Exception e) {
                        Log.e(Constants.LOG_TAG, e.getMessage());
                    }
                }
            }
            messages.clear();
        }
    }

    public static boolean isSessionAvailable(){
        Session session = Chat.getInstance().getSession();
        if(session != null && session.getId() != 0){
            return true;
        }
        return false;
    }

    public static boolean isRequestInWaiting(){
        Request request = Chat.getInstance().getRequest();
        if(request != null && request.getId() != 0 && request.getStatus() == Constants.RequestStatus.WAITING) {
            return true;
        }
        return false;
    }

    public static void clearRequestAndSession(){
        Chat.getInstance().getRequest().setId(0);
        Chat.getInstance().setSession(null);
    }
}
