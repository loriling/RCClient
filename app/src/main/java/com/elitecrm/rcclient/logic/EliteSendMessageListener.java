package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.util.Constants;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;
import io.rong.sight.message.SightMessage;

/**
 * Created by Loriling on 2017/2/9.
 */

public class EliteSendMessageListener implements RongIM.OnSendMessageListener {
    @Override
    public Message onSend(Message message) {
        if(message != null) {
            MessageContent messageContent = message.getContent();
            try {
                if(messageContent instanceof TextMessage ||
                        messageContent instanceof VoiceMessage ||
                        messageContent instanceof LocationMessage ||
                        messageContent instanceof ImageMessage ||
                        messageContent instanceof FileMessage ||
                        messageContent instanceof SightMessage) {
                    JSONObject messageExtraJSON = new JSONObject();
                    messageExtraJSON.put("token", Chat.getInstance().getToken());
                    if(Chat.getInstance().isSessionAvailable()){ // 有session时候
                        messageExtraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                    } else if (Chat.getInstance().isRequestInWaiting()){ // 请求id合法切请求状态是等待中时候
                        messageExtraJSON.put("requestId", Chat.getInstance().getRequest().getId());
                    } else {
                        //这里不主动发出聊天请求，转为收到发送消息的返回后，判断了返回结果再做处理
                        //Chat.sendChatRequest();
                        //Chat.addUnsendMessage(message);
                        //return message;
                    }
                    if (messageContent instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) messageContent;
                        textMessage.setExtra(messageExtraJSON.toString());
                    } else if (messageContent instanceof VoiceMessage) {
                        VoiceMessage voiceMessage = (VoiceMessage)messageContent;
                        messageExtraJSON.put("length", voiceMessage.getDuration());
                        voiceMessage.setExtra(messageExtraJSON.toString());
                    } else if (messageContent instanceof LocationMessage) {
                        LocationMessage locationMessage = (LocationMessage)messageContent;
                        String extraStr = locationMessage.getExtra();
                        if(extraStr != null) {
                            JSONObject oExtraJSON = new JSONObject(extraStr);
                            String map = oExtraJSON.optString("map");
                            messageExtraJSON.put("map", map);
                        }
                        locationMessage.setExtra(messageExtraJSON.toString());
                    } else if (messageContent instanceof ImageMessage) {
                        ImageMessage imageMessage = (ImageMessage)messageContent;
                        imageMessage.setExtra(messageExtraJSON.toString());
                    } else if (messageContent instanceof FileMessage) {
                        FileMessage fileMessage = (FileMessage)messageContent;
                        fileMessage.setExtra(messageExtraJSON.toString());
                    } else if (messageContent instanceof SightMessage) {
                        SightMessage sightMessage = (SightMessage) messageContent;
                        sightMessage.setExtra(messageExtraJSON.toString());
                    }
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "EliteSendMessageListener.onSend: " + e.getMessage());
            }
        }
        return message;
    }

    @Override
    public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
        return false;
    }
}
