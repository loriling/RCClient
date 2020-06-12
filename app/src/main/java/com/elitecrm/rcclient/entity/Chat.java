package com.elitecrm.rcclient.entity;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.elitecrm.rcclient.EliteChat;
import com.elitecrm.rcclient.logic.EliteSendMessageCallback;
import com.elitecrm.rcclient.message.EliteMessage;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.MessageUtils;
import com.elitecrm.rcclient.util.sqlite.DBManager;

import org.json.JSONObject;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Chat {

    private Context context;
    private String token;
    private Request request;
    private Client client;
    private Session session;
    private DBManager db;

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

    public DBManager getDb() {
        return db;
    }

    public void setDb(DBManager db) {
        this.db = db;
    }

    private Chat(Context context) {
        this.context = context;
        this.db = new DBManager(context);
    }

    public static void init(Context context) {
        chat = new Chat(context);
    }

    public static Chat getInstance() {
        return chat;
    }

    public void initClient(String userId, String name, String portraitUri, String targetId, String title) {
        Client client = new Client();
        client.setLoginId(userId);
        client.setName(name);
        client.setIcon(portraitUri);
        client.setTargetId(targetId);
        client.setTitle(title);
        this.setClient(client);
    }

    public Request initRequest(int queueId, String from, String tracks) {
        Request request = new Request(queueId);
        request.setFrom(from);
        request.setStatus(Constants.RequestStatus.WAITING);
        request.setTracks(tracks);
        this.setRequest(request);
        return request;
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

    public void addUnsendMessage(Message message, int type) {
        Log.d(Constants.LOG_TAG, "Add unsend message: " + message + " type: " + type);
        db.addMessage(new UnsendMessage(type, message));
    }

    public void sendChatRequest(){
        //清空老的请求id和会话对象
        clearRequestAndSession();
        //发出聊天请求
        MessageUtils.sendChatRequest(request);
    }


    /**
     * 发送之前未送达的消,当排队之前发出的消息,会先缓存起来，如果排上队了，就会补发这些消息
     */
    public void sendUnsendMessages() {
        List<UnsendMessage> unsendMessages = db.queryMessages();
        if (unsendMessages.size() > 0) {
            Log.d(Constants.LOG_TAG, "Send " + unsendMessages.size() + " unsend messages.");
            for (UnsendMessage unsendMessage : unsendMessages) {
                Message message = unsendMessage.getMessage();
                MessageContent messageContent = message.getContent();
                try {
                    if(messageContent instanceof TextMessage ||
                            messageContent instanceof VoiceMessage ||
                            messageContent instanceof ImageMessage ||
                            messageContent instanceof FileMessage ||
                            messageContent instanceof LocationMessage ||
                            messageContent instanceof SightMessage){
                        EliteMessage eliteMessage = new EliteMessage();
                        JSONObject extraJSON = new JSONObject();
                        extraJSON.put("token", Chat.getInstance().getToken());
                        extraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                        extraJSON.put("type", Constants.RequestType.SEND_CHAT_MESSAGE);
                        if(messageContent instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage)messageContent;
                            if (unsendMessage.getType() == Constants.UnsendMessageType.API) {
                                MessageUtils.doSendTextMessage(textMessage, Chat.getInstance().getClient().getTargetId());
                                continue;
                            } else {
                                eliteMessage.setMessage(textMessage.getContent());
                                extraJSON.put("messageType", Constants.MessageType.TEXT);
                            }
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
                            if (unsendMessage.getType() == Constants.UnsendMessageType.API) {
                                MessageUtils.doSendImgMessage(imageMessage, Chat.getInstance().getClient().getTargetId());
                                continue;
                            } else {
                                eliteMessage.setMessage(imageMessage.getBase64());
                                extraJSON.put("imageUri", imageMessage.getRemoteUri().toString());
                                extraJSON.put("messageType", Constants.MessageType.IMG);
                            }
                        } else if (messageContent instanceof FileMessage) {
                            FileMessage fileMessage = (FileMessage)messageContent;
                            extraJSON.put("fileName", fileMessage.getName());
                            extraJSON.put("fileSize", fileMessage.getSize());
                            extraJSON.put("fileType", fileMessage.getType());
                            extraJSON.put("fileUrl", fileMessage.getFileUrl().toString());
                            extraJSON.put("messageType", Constants.MessageType.FILE);
                        } else if (messageContent instanceof SightMessage) {
                            SightMessage sightMessage = (SightMessage)messageContent;
                            eliteMessage.setMessage(sightMessage.getBase64());
                            extraJSON.put("name", sightMessage.getName());
                            extraJSON.put("size", sightMessage.getSize());
                            extraJSON.put("sightUrl", sightMessage.getMediaUrl().toString());
                            extraJSON.put("duration", sightMessage.getDuration());
                            extraJSON.put("messageType", Constants.MessageType.SIGHT);
                        }
                        eliteMessage.setExtra(extraJSON.toString());
                        Message lastMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.PRIVATE, eliteMessage);
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
                        Message lastMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.PRIVATE, eliteMessage);
                        RongIM.getInstance().sendMessage(lastMessage, null, null, new EliteSendMessageCallback());
                    }

                } catch (Exception e) {
                    Log.e(Constants.LOG_TAG, "Chat.sendUnsendMessages" + e.getMessage());
                }
            }
            unsendMessages.clear();
        }
    }

    public boolean isTokenValid() {
        return getToken() != null;
    }

    public boolean isSessionAvailable(){
        if (session != null && session.getId() != 0){
            return true;
        }
        return false;
    }

    public boolean isRequestInWaiting(){
        if (request != null && request.getId() != 0 && request.getStatus() == Constants.RequestStatus.WAITING) {
            return true;
        }
        return false;
    }

    /**
     * 设置当前会话已经推送了满意度
     */
    public void setPushRating(boolean pushRating) {
        if (session != null) {
            session.setPushRating(pushRating);
        }
    }

    /**
     * 查看当前会话是否已经被推送过满意度
     * @return
     */
    public boolean isPushRating() {
        if (session != null) {
            return session.isPushRating();
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
    public Session initSession(long sessionId, String agentId, String agentName, String icon, String comments) {
        session = new Session();
        session.setId(sessionId);
        setupAgent(agentId, agentName, icon, comments);
        return session;
    }

    /**
     * 装载Agent，当是第一个agent的时候，刷新UserInfo缓存（现在是单聊模式，所以坐席对应的就是全局TargetId： EliteCRM）
     * @param agentId
     * @param agentName
     * @param icon
     * @param comments
     */
    public void setupAgent(String agentId, String agentName, String icon, String comments) {
        if (icon != null && !icon.equals("") && !icon.startsWith("http://") && !icon.startsWith("https://")) {
            icon = EliteChat.getServerAddr() + "/ngsIcon.do?path=" + icon + "&queue=" + Chat.getInstance().getRequest().getQueueId();
        }
        Agent agent = new Agent(agentId, agentName, icon, comments);
        if (session.getAgents().size() == 0) {
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

    /**
     * 清空所有当前缓存
     */
    public void clear() {
        this.token = null;
        clearRequestAndSession();
    }
}
