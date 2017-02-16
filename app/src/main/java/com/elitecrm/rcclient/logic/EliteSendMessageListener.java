package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.MessageUtils;

import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

/**
 * Created by Loriling on 2017/2/9.
 */

public class EliteSendMessageListener implements RongIM.OnSendMessageListener {
    @Override
    public Message onSend(Message message) {
        if(message != null) {
            MessageContent messageContent = message.getContent();
            try {
                JSONObject messageExtraJSON = new JSONObject();
                messageExtraJSON.put("token", Chat.getInstance().getToken());
                if(Chat.getInstance().getSession() != null){
                    messageExtraJSON.put("sessionId", Chat.getInstance().getSession().getId());
                } else if (Chat.getInstance().getRequest() != null){
                    messageExtraJSON.put("requestId", Chat.getInstance().getRequest().getId());
                } else {
                    MessageUtils.sendChatRequest(Chat.getInstance().getRequest().getQueueId(), "APP");
                    Chat.addUnsendMessage(message);
                    return null;
                }

                if(messageContent instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) messageContent;
                    textMessage.setExtra(messageExtraJSON.toString());
                } else if (messageContent instanceof VoiceMessage) {
                    VoiceMessage voiceMessage = (VoiceMessage)messageContent;
                    messageExtraJSON.put("length", voiceMessage.getDuration());
                    voiceMessage.setExtra(messageExtraJSON.toString());
                } else if (messageContent instanceof LocationMessage) {
                    LocationMessage locationMessage = (LocationMessage)messageContent;
                    locationMessage.setExtra(messageExtraJSON.toString());
                } else if (messageContent instanceof ImageMessage) {
                    ImageMessage imageMessage = (ImageMessage)messageContent;
                    imageMessage.setExtra(messageExtraJSON.toString());
                } else if (messageContent instanceof FileMessage) {
                    FileMessage fileMessage = (FileMessage)messageContent;
                    fileMessage.setExtra(messageExtraJSON.toString());
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage());
            }
        }
        return message;
    }

    @Override
    public boolean onSent(Message message, RongIM.SentMessageErrorCode sentMessageErrorCode) {
        return false;
    }
}
