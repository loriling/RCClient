package com.elitecrm.rcclient.util;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.EliteMessage;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.entity.User;
import com.elitecrm.rcclient.logic.EliteSendMessageCallback;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.message.InformationNotificationMessage;

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
     * @param queueId 队列号
     * @param from 来源
     */
    public static void sendChatRequest(int queueId, String from){
        JSONObject requestJSON = new JSONObject();
        try{
            requestJSON.put("messageId", MessageUtils.getNextMessageId());
            requestJSON.put("type", Constants.RequestType.SEND_CHAT_REQUEST);
            requestJSON.put("queueId", queueId);
            requestJSON.put("from", from);
            requestJSON.put("token", Chat.getInstance().getToken());
        } catch (Exception e) {}

        InformationNotificationMessage inm = InformationNotificationMessage.obtain(requestJSON.toString());
        Message myMessage = Message.obtain(Constants.CHAT_TARGET_ID, Conversation.ConversationType.SYSTEM, inm);
        RongIM.getInstance().sendMessage(myMessage, null, null, new EliteSendMessageCallback());
    }

    public static void sendRating(int rating, String comments) {
        try{
            EliteMessage eliteMessage = EliteMessage.obtain("");
            JSONObject extraJSON = new JSONObject();
            extraJSON.put("token", Chat.getInstance().getToken());
            extraJSON.put("sessionId", Chat.getInstance().getSession().getId());
            extraJSON.put("type", Constants.RequestType.RATE_SESSION);
            extraJSON.put("rating", rating);
            extraJSON.put("ratingComments", comments);
            eliteMessage.setExtra(extraJSON.toString());
            Message lastMessage = Message.obtain(Constants.CHAT_TARGET_ID, Conversation.ConversationType.SYSTEM, eliteMessage);
            RongIM.getInstance().sendMessage(lastMessage, null, null, new EliteSendMessageCallback());
        } catch (Exception e) {}
    }

    /**
     * 根据发送人id，来获取对应坐席的名字
     * @param agentId
     * @return
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
}
