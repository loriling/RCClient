package com.elitecrm.rcclient.logic;

import android.net.Uri;
import android.util.Log;

import com.elitecrm.rcclient.entity.Agent;
import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.EliteMessage;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.util.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.ProfileNotificationMessage;
import io.rong.message.TextMessage;

/**
 * Created by Loriling on 2017/2/3.
 */

public class EliteReceiveMessageListener implements RongIMClient.OnReceiveMessageListener {
    @Override
    public boolean onReceived(Message message, int left) {
        String objName = message.getObjectName();
        MessageContent messageConetent = message.getContent();

        if(objName.equals("E:Msg")) {//所有的状态通知
            EliteMessage eliteMessage = new EliteMessage(messageConetent.encode());
            String contentStr = eliteMessage.getMessage();
            try{
                JSONObject contentJSON = new JSONObject(contentStr);
                int type = contentJSON.getInt("type");
                Log.d(Constants.LOG_TAG, contentStr);
                if (type == Constants.RequestType.SEND_CHAT_REQUEST) {
                    //聊天请求消息的返回
                    String mContent = contentJSON.getString("message");
                    InformationNotificationMessage inm = InformationNotificationMessage.obtain(mContent);
                    long requestId = contentJSON.getLong("requestId");
                    Chat.getInstance().setRequestId(requestId);
                    int result = contentJSON.getInt("result");
                    if(result == Constants.Result.SUCCESS) {
                        int queueLength = contentJSON.getInt("queueLength");
                        if(queueLength == 0) {
                            inm = InformationNotificationMessage.obtain(contentJSON.getString("message"));
                        } else {
                            inm = InformationNotificationMessage.obtain("当前排在第" + queueLength + "位");
                        }
                    } else {
                        inm = InformationNotificationMessage.obtain(contentJSON.getString("message"));
                        if(result == Constants.Result.REQUEST_ALREADY_IN_ROULTING) {
                            Chat.setRequestStatus(Constants.RequestStatus.WAITING);
                        } else if(result == Constants.Result.NO_AGENT_ONLINE) {
                            Chat.setRequestStatus(Constants.RequestStatus.NO_AGENT_ONLINE);
                        } else if (result == Constants.Result.NOT_IN_WORKTIME) {
                            Chat.setRequestStatus(Constants.RequestStatus.OFF_HOUR);
                        } else {
                            //其他情况都当做dropped
                            Chat.setRequestStatus(Constants.RequestStatus.DROPPED);
                        }
                    }
                    RongIM.getInstance().insertMessage(Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, null, inm, null);
                } else if (type == Constants.RequestType.CANCEL_CHAT_REQUEST) {

                } else if (type == Constants.RequestType.CLOSE_SESSION) {

                } else if (type == Constants.RequestType.RATE_SESSION) {

                } else if (type == Constants.RequestType.SEND_CHAT_MESSAGE) {

                } else if (type == Constants.RequestType.SEND_PRE_CHAT_MESSAGE) {

                }
                //
                else if (type == Constants.RequestType.CHAT_REQUEST_STATUS_UPDATE) {
                    int requestStatus = contentJSON.getInt("requestStatus");
                    Chat.setRequestStatus(requestStatus);
                    int queueLength = contentJSON.getInt("queueLength");
                    InformationNotificationMessage informationMessage = null;
                    if(requestStatus == Constants.RequestStatus.WAITING){
                        informationMessage = InformationNotificationMessage.obtain("还有" + queueLength + "位，等待中..");
                    } else if (requestStatus == Constants.RequestStatus.DROPPED){
                        informationMessage = InformationNotificationMessage.obtain("请求异常丢失");
                    } else if (requestStatus == Constants.RequestStatus.TIMEOUT){
                        informationMessage = InformationNotificationMessage.obtain("请求超时");
                    }
                    if(informationMessage != null) {
                        RongIM.getInstance().insertMessage(Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, null, informationMessage, null);
                    }
                } else if (type == Constants.RequestType.CHAT_STARTED) {
                    long sessionId = contentJSON.getLong("sessionId");
                    JSONArray agents = contentJSON.getJSONArray("agents");
                    JSONObject agentJSON = agents.getJSONObject(0);
                    String agentId = agentJSON.getString("id");
                    String name = agentJSON.getString("name");

                    //记录会话相关信息
                    Session session = new Session();
                    session.setId(sessionId);
                    Agent agent = new Agent();
                    agent.setId(agentId);
                    agent.setName(name);
                    session.addAgent(agent);
                    Chat.getInstance().setSession(session);
                    InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain("坐席[" + name + "]为您服务");
                    RongIM.getInstance().insertMessage(Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, null, informationMessage, null);
                    Chat.sendUnsendMessages();
                } else if (type == Constants.RequestType.AGENT_PUSH_RATING) {
                    // TODO: 2017/2/15  坐席推送满意度处理
                } else if (type == Constants.RequestType.AGENT_UPDATED) {
                    // TODO: 2017/2/15 坐席人员变更处理

                } else if (type == Constants.RequestType.AGENT_CLOSE_SESSION) {
                    Chat.clearRequestAndSession();
                    InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain("会话结束");
                    RongIM.getInstance().insertMessage(Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, null, informationMessage, null);
                } else if (type == Constants.RequestType.AGENT_SEND_MESSAGE) {
                    String agentId = contentJSON.getString("agentId");
                    JSONObject msgJSON = contentJSON.getJSONObject("msg");
                    int msgType = msgJSON.getInt("type");
                    if(msgType == Constants.MessageType.SYSTEM_NOTICE) {
                        int noticeType = msgJSON.getInt("noticeType");
                        if(noticeType == Constants.NoticeMessageType.NORMAL) {
                            String content = msgJSON.getString("content");
                            TextMessage textMessage = TextMessage.obtain(content);
                            RongIM.getInstance().insertMessage(Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, agentId, textMessage, null);
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage());
            }
        } else if (messageConetent instanceof ProfileNotificationMessage) {
            ProfileNotificationMessage profileNotificationMessage = (ProfileNotificationMessage)messageConetent;
            String data = profileNotificationMessage.getData();
            Log.d(Constants.LOG_TAG, data);

        } else if (messageConetent instanceof TextMessage) {//坐席发的文字聊天消息，都作为TextMessage发送过来
            //设置一个userInfo对象到messageContent，实现后台消息提示，其中userId必须是我们的targetId: EliteCRM
            try {
                String extraStr = message.getExtra();
                JSONObject extraJSON = new JSONObject(extraStr);
                String agentName = extraJSON.getString("agentName");
                String icon = extraJSON.optString("icon");
                Uri iconUri = Uri.parse(icon);
                UserInfo userInfo = new UserInfo(Constants.CHAT_TARGET_ID, agentName, iconUri);
                messageConetent.setUserInfo(userInfo);
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage());
            }
            TextMessage tm = (TextMessage)messageConetent;
            String contentStr = tm.getContent();
            Log.d(Constants.LOG_TAG, contentStr);
        }
        return false;//这里返回true就不会有消息声音提示
    }
}
