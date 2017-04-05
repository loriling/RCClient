package com.elitecrm.rcclient.entity;

import android.net.Uri;
import android.util.Log;

import com.elitecrm.rcclient.EliteChat;
import com.elitecrm.rcclient.logic.EliteSendMessageCallback;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.MessageUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Chat {
    // 最大为发送消息存储量，缓存过多消息怕出问题
    private static final int MAX_UNSEND_MESSAGES = 20;
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

    public void initClient(String userId, String name, String portraitUri, String targetId) {
        Client client = new Client();
        client.setLoginId(userId);
        client.setName(name);
        client.setIcon(portraitUri);
        client.setTargetId(targetId);
        this.setClient(client);
    }

    public void initRequest(int queueId) {
        Request request = new Request(queueId);
        request.setStatus(Constants.RequestStatus.WAITING);
        this.setRequest(request);
    }

    public void setRequestId(long requestId) {
        if(request == null) {
            request = new Request();
            Chat.getInstance().setRequest(request);
        }
        request.setId(requestId);
    }

    public void setRequestStatus(int status) {
        request.setStatus(status);
    }

    public void addUnsendMessage(Message message) {
        if(unsendMessages.size() >= MAX_UNSEND_MESSAGES){
            unsendMessages.remove(0);
        }
        unsendMessages.add(message);
    }

    public void sendChatRequest(){
        MessageUtils.sendChatRequest(request.getQueueId(), "APP");
    }

    public List<Message> getUnsendMessages(){
        return unsendMessages;
    }

    /**
     * 发送之前未送达的消,当排队之前发出的消息,会先缓存起来，如果排上队了，就会补发这些消息
     */
    public void sendUnsendMessages() {
        if(unsendMessages.size() > 0) {
            for(Message message : unsendMessages) {
                MessageContent messageContent = message.getContent();
                try {
                    if(messageContent instanceof TextMessage ||
                            messageContent instanceof VoiceMessage ||
                            messageContent instanceof ImageMessage ||
                            messageContent instanceof FileMessage ||
                            messageContent instanceof LocationMessage){
                        EliteMessage eliteMessage = new EliteMessage();
                        JSONObject extraJSON = new JSONObject();
                        extraJSON.put("token", Chat.getInstance().getToken());
                        extraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                        extraJSON.put("type", Constants.RequestType.SEND_CHAT_MESSAGE);
                        if(messageContent instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage)messageContent;
                            eliteMessage.setMessage(textMessage.getContent());
                            extraJSON.put("messageType", Constants.MessageType.TEXT);
                        } else if (messageContent instanceof VoiceMessage) {
                            VoiceMessage voiceMessage = (VoiceMessage)messageContent;
                            eliteMessage.setMessage(voiceMessage.getBase64());
                            extraJSON.put("length", voiceMessage.getDuration());
                            extraJSON.put("messageType", Constants.MessageType.VOICE);
                        } else if (messageContent instanceof LocationMessage) {
                            LocationMessage locationMessage = (LocationMessage)messageContent;
                            eliteMessage.setMessage(locationMessage.getBase64());
                            extraJSON.put("latitude", locationMessage.getLat());
                            extraJSON.put("longitude", locationMessage.getLng());
                            extraJSON.put("poi", locationMessage.getPoi());
                            extraJSON.put("imgUri", locationMessage.getImgUri());
                            extraJSON.put("messageType", Constants.MessageType.LOCATION);
                        } else if (messageContent instanceof ImageMessage) {
                            ImageMessage imageMessage = (ImageMessage)messageContent;
                            eliteMessage.setMessage(imageMessage.getBase64());
                            extraJSON.put("imageUri", imageMessage.getRemoteUri().toString());
                            extraJSON.put("messageType", Constants.MessageType.IMG);
                        } else if (messageContent instanceof FileMessage) {
                            FileMessage fileMessage = (FileMessage)messageContent;
                            extraJSON.put("fileName", fileMessage.getName());
                            extraJSON.put("fileSize", fileMessage.getSize());
                            extraJSON.put("fileType", fileMessage.getType());
                            extraJSON.put("fileUrl", fileMessage.getFileUrl().toString());
                            extraJSON.put("messageType", Constants.MessageType.FILE);
                        }
                        eliteMessage.setExtra(extraJSON.toString());
                        Message lastMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.SYSTEM, eliteMessage);
                        RongIM.getInstance().sendMessage(lastMessage, null, null, new EliteSendMessageCallback());
                    } else if (messageContent instanceof EliteMessage) { //自定义消息的预发送
                        EliteMessage eliteMessage = (EliteMessage)messageContent;
                        JSONObject extraJSON = null;
                        try {
                            extraJSON = new JSONObject(eliteMessage.getExtra());
                        } catch (Exception e) {
                            extraJSON = new JSONObject();
                        }
                        extraJSON.put("token", Chat.getInstance().getToken());
                        extraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                        eliteMessage.setExtra(extraJSON.toString());
                        Message lastMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.SYSTEM, eliteMessage);
                        RongIM.getInstance().sendMessage(lastMessage, null, null, new EliteSendMessageCallback());
                    }

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG, e.getMessage());
                }
            }
            unsendMessages.clear();
        }
    }

    public boolean isTokenValid() {
        return getToken() != null;
    }

    public boolean isSessionAvailable(){
        if(session != null && session.getId() != 0){
            return true;
        }
        return false;
    }

    public boolean isRequestInWaiting(){
        if(request != null && request.getId() != 0 && request.getStatus() == Constants.RequestStatus.WAITING) {
            return true;
        }
        return false;
    }

    /**
     * 设置请求id为0，同时清空会话对象
     * 请求对象不能直接清空，因为请求对象中存储着排队的队列号，要供下次排队时候使用
     */
    public void clearRequestAndSession(){
        request.setId(0);
        session = null;
    }

    public void clearSessionAgents(){
        session.getAgents().clear();
    }

    /**
     * 初始化Session对象
     * @param sessionId
     * @param agentId
     * @param agentName
     * @param icon
     * @param comments
     */
    public void initSession(long sessionId, String agentId, String agentName, String icon, String comments) {
        session = new Session();
        session.setId(sessionId);
        setupAgent(agentId, agentName, icon, comments);
    }

    /**
     * 装载Agent，当是第一个agent的时候，刷新UserInfo缓存（现在是单聊模式，所以坐席对应的就是全局TargetId： EliteCRM）
     * @param agentId
     * @param agentName
     * @param icon
     * @param comments
     */
    public void setupAgent(String agentId, String agentName, String icon, String comments){
        if(icon != null && !icon.equals("") && !icon.startsWith("http://") && !icon.startsWith("https://")){
            icon = EliteChat.getNgsAddr() + "/fs/get?file=" + icon;
        }
        Agent agent = new Agent(agentId, agentName, icon, comments);
        if(session.getAgents().size() == 0){
            session.setFirstAgent(agent);
            RongIM.getInstance().refreshUserInfoCache(new UserInfo(Chat.getInstance().getClient().getTargetId(), agent.getName(), Uri.parse(agent.getIcon())));
        }
        session.addAgent(agent);
    }

    /**
     * 装载client，同时刷新UserInfo缓存
     * @param userId
     */
    public void setupClient(String userId) {
        client.setId(userId);
        RongIM.getInstance().refreshUserInfoCache(new UserInfo(userId, client.getName(), Uri.parse(client.getIcon())));
    }
}
