package com.elitecrm.rcclient.util;

import android.util.Base64;
import android.util.Log;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.MessageSO;
import com.elitecrm.rcclient.entity.Request;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.entity.User;
import com.elitecrm.rcclient.logic.EliteSendMessageCallback;
import com.elitecrm.rcclient.message.EliteMessage;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
import io.rong.sight.message.SightMessage;

/**
 * Created by ThinkPad on 2017/2/8.
 */

public class MessageUtils {
    private static long messageId = 0l;
    public static long getNextMessageId(){
        return messageId++;
    }

    /**
     * 发出排队请求
     * @param request 请求对象
     */
    public static void sendChatRequest(Request request){
        try {
            String targetId = Chat.getInstance().getClient().getTargetId();
            JSONObject requestJSON = new JSONObject();
            requestJSON.put("messageId", MessageUtils.getNextMessageId());
            requestJSON.put("queueId", request.getQueueId());
            requestJSON.put("brand", request.getBrand());
            requestJSON.put("from", request.getFrom());
            requestJSON.put("tracks", request.getTracks());

            JSONObject extraJSON = new JSONObject();
            extraJSON.put("token", Chat.getInstance().getToken());
            extraJSON.put("type", Constants.RequestType.SEND_CHAT_REQUEST);
            extraJSON.put("targetId", targetId);

            EliteMessage eliteMessage = EliteMessage.obtain(requestJSON.toString());
            eliteMessage.setExtra(extraJSON.toString());
            Message myMessage = Message.obtain(targetId, Conversation.ConversationType.SYSTEM, eliteMessage);
            RongIM.getInstance().sendMessage(myMessage, null, null, new EliteSendMessageCallback());
        } catch (Exception e) {}
    }

    /**
     * 发送满意度消息
     * @param sessionId 会话id
     * @param ratingId 满意度id
     * @param comments 满意度comments
     */
    public static void sendRating(long sessionId, int ratingId, String comments) {
        try{
            JSONObject extraJSON = new JSONObject();
            extraJSON.put("token", Chat.getInstance().getToken());
            extraJSON.put("sessionId", sessionId);
            extraJSON.put("type", Constants.RequestType.RATE_SESSION);
            JSONObject requestJSON = new JSONObject();
            requestJSON.put("ratingId", ratingId);
            requestJSON.put("ratingComments", comments);
            EliteMessage eliteMessage = EliteMessage.obtain(requestJSON.toString());
            eliteMessage.setExtra(extraJSON.toString());
            Message lastMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.SYSTEM, eliteMessage);
            RongIM.getInstance().sendMessage(lastMessage, null, null, new EliteSendMessageCallback());
        } catch (Exception e) {}
    }

    /**
     * 发送自定义消息，参数message可以是任何类型字符串，坐席收到该消息后，会在前台通过消息方式发出
     * 当会话已经建立时候，直接发送；当会话还没有建立，则通过addUnsendMessage
     * @param message
     * @return 发送(添加)成功还是失败
     */
    public static boolean sendCustomMessage(String message, String target) {
        if(Chat.getInstance().isSessionAvailable()){
            long sessionId = Chat.getInstance().getSession().getId();
            Message custMessage = generateEliteMessage(Chat.getInstance().getToken(), sessionId, message, Constants.RequestType.SEND_CUSTOM_MESSAGE, target);
            if(custMessage != null){
                RongIM.getInstance().sendMessage(custMessage, null, null, new EliteSendMessageCallback());
                return true;
            }
        } else {
            Message custMessage = generateEliteMessage(null, 0, message, Constants.RequestType.SEND_CUSTOM_MESSAGE, target);
            if (custMessage != null) {
                Chat.getInstance().addUnsendMessage(custMessage);
                return true;
            }
        }
        return false;
    }

    /**
     * 发送文字消息
     * 当会话已经建立时候，直接发送；当会话还没有建立，则通过addUnsendMessage
     * @param text 文字消息内容
     * @param target 发送的目标
     * @return
     */
    public static boolean sendTextMessage(String text, String target) {
        try {
            TextMessage textMessage = TextMessage.obtain(text);
            if (Chat.getInstance().isSessionAvailable()) {
                JSONObject extraJSON = new JSONObject();
                extraJSON.put("token", Chat.getInstance().getToken());
                extraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                textMessage.setExtra(extraJSON.toString());
                Message message = Message.obtain(target, Conversation.ConversationType.PRIVATE, textMessage);
                RongIM.getInstance().sendMessage(message, null, null, new EliteSendMessageCallback());
                return true;
            } else {
                Message message = Message.obtain(target, Conversation.ConversationType.PRIVATE, textMessage);
                message.setObjectName(Constants.ObjectName.TXT_MSG);
                Chat.getInstance().addUnsendMessage(message);
                return true;
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "MessageUtils.sendTextMessage: " + e.getMessage());
        }
        return false;
    }

    /**
     * 构造一个自定义消息对象
     * @param message
     * @return
     */
    public static Message generateEliteMessage(String token, long sessionId, String message, int type, String target) {
        try{
            JSONObject extraJSON = new JSONObject();
            extraJSON.put("token", token);
            extraJSON.put("sessionId", sessionId);
            extraJSON.put("type", type);
            EliteMessage eliteMessage = EliteMessage.obtain(message);
            eliteMessage.setExtra(extraJSON.toString());

            Message custMessage = Message.obtain(target, Conversation.ConversationType.SYSTEM, eliteMessage);
            custMessage.setObjectName(Constants.ObjectName.ELITE_MSG);
            return custMessage;
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "MessageUtils.generateEliteMessage: " + e.getMessage());
        }
        return null;
    }

    /**
     * 根据发送人id，来获取对应坐席的名字
     * @param agentId
     * @return {string}
     */
    public static String getAgentNameById(String agentId) {
        Session session = Chat.getInstance().getSession();
        if(session != null){
            User agent = session.getAgents().get(agentId);
            if(agent != null) {
                return agent.getName();
            }
        }
        return "EliteCRM";
    }

    /**
     * 发送转人工请求
     * @return
     */
    public static boolean sendTransferMessage() {
        try {
            if(Chat.getInstance().isSessionAvailable()){
                Chat chat = Chat.getInstance();
                Session session = chat.getSession();
                if (session.isRobotMode()) {//只有在机器人模式下，才可以发出转接
                    Message message = generateEliteMessage(chat.getToken(), chat.getSession().getId(), "转接", Constants.RequestType.ROBOT_TRANSFER_MESSAGE, chat.getClient().getTargetId());
                    RongIM.getInstance().sendMessage(message, null, null, new EliteSendMessageCallback());
                    return true;
                } else {
                    InformationNotificationMessage inm = InformationNotificationMessage.obtain("已经在人工聊天中");
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm);
                }
            }
        } catch (Exception e) {
            Log.e(Constants.LOG_TAG, "MessageUtils.sendTransferMessage: " + e.getMessage());
        }
        return false;
    }

    public static void insertMessage(Conversation.ConversationType type, String targetId, String senderUserId, MessageContent messageContent, long time) {
        Message.ReceivedStatus rs = new Message.ReceivedStatus(1);
        RongIM.getInstance().insertIncomingMessage(type, targetId, senderUserId, rs, messageContent, time, null);
//        RongIM.getInstance().insertMessage(type, targetId, senderUserId, messageContent, null);
    }

    public static void insertMessage(Conversation.ConversationType type, String targetId, String senderUserId, MessageContent messageContent) {
        insertMessage(type, targetId, senderUserId, messageContent, System.currentTimeMillis());
    }

    public static MessageSO marshal(Message message) {
        MessageSO messageSO = new MessageSO();
        messageSO.setTargetId(message.getTargetId());
        messageSO.setConversationType(message.getConversationType().getValue());
        messageSO.setObjectName(message.getObjectName());
        try {
            messageSO.setContent(new String(message.getContent().encode(), "utf-8"));
        } catch (UnsupportedEncodingException e) {}
        return messageSO;
    }

    public static Message unmarshal(String targetId, int conversationType, String objectName, String content) {
        try {
            MessageContent messageContent = generateMessageContent(objectName, content);
            return Message.obtain(targetId, Conversation.ConversationType.setValue(conversationType), messageContent);
        } catch (UnsupportedEncodingException e) {}
        return null;
    }
    public static Message unmarshal(MessageSO messageSO) {
        return unmarshal(messageSO.getTargetId(), messageSO.getConversationType(), messageSO.getObjectName(), messageSO.getContent());
    }

    public static MessageContent generateMessageContent(String objectName, String content) throws UnsupportedEncodingException {
        MessageContent messageContent = null;
        if (objectName.equals(Constants.ObjectName.TXT_MSG)) {
            messageContent = new TextMessage(content.getBytes("utf-8"));
        } else if (objectName.equals(Constants.ObjectName.IMG_MSG)) {
            messageContent = new ImageMessage(content.getBytes("utf-8"));
        } else if (objectName.equals(Constants.ObjectName.FILE_MSG)) {
            messageContent = new FileMessage(content.getBytes("utf-8"));
        } else if (objectName.equals(Constants.ObjectName.LBS_MSG)) {
            messageContent = new LocationMessage(content.getBytes("utf-8"));
        } else if (objectName.equals(Constants.ObjectName.VC_MSG)) {
            messageContent = new VoiceMessage(content.getBytes("utf-8"));
        } else if (objectName.equals(Constants.ObjectName.ELITE_MSG)) {
            messageContent = new EliteMessage(content.getBytes("utf-8"));
        } else if (objectName.equals(Constants.ObjectName.SIGHT_MSG)) {
            messageContent = new SightMessage(content.getBytes("utf-8"));
        }
        return messageContent;
    }
}
