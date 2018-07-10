package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.EliteMessage;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.robot.RobotMessageHandler;
import com.elitecrm.rcclient.util.ActivityUtils;
import com.elitecrm.rcclient.util.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.ImageMessage;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.LocationMessage;
import io.rong.message.TextMessage;
import io.rong.message.VoiceMessage;

import static com.elitecrm.rcclient.util.MessageUtils.insertMessage;

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
                    InformationNotificationMessage inm = null;
                    long requestId = contentJSON.optLong("requestId");
                    if (requestId > 0) {
                        Chat.getInstance().setRequestId(requestId);
                    }
                    //boolean continueLastSession = contentJSON.optBoolean("continueLastSession");
                    int result = contentJSON.getInt("result");
                    if(result == Constants.Result.SUCCESS) {
                        int queueLength = contentJSON.getInt("queueLength");
                        if(queueLength == 0) {
                            //inm = InformationNotificationMessage.obtain(contentJSON.getString("message")); //继续之前会话;机器人会话开始  这些提示不是不是不需要显示出来了
                        } else {
                            inm = InformationNotificationMessage.obtain("当前排在第" + queueLength + "位");
                        }
                    } else {
                        inm = InformationNotificationMessage.obtain(contentJSON.getString("message"));
                        if(result == Constants.Result.REQUEST_ALREADY_IN_ROULTING) {
                            Chat.getInstance().setRequestStatus(Constants.RequestStatus.WAITING);
                        } else if(result == Constants.Result.NO_AGENT_ONLINE) {
                            Chat.getInstance().setRequestStatus(Constants.RequestStatus.NO_AGENT_ONLINE);
                        } else if (result == Constants.Result.NOT_IN_WORKTIME) {
                            Chat.getInstance().setRequestStatus(Constants.RequestStatus.OFF_HOUR);
                        } else {
                            //其他情况都当做dropped
                            Chat.getInstance().setRequestStatus(Constants.RequestStatus.DROPPED);
                        }
                    }
                    if (inm != null)
                        insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm);
                } else if (type == Constants.RequestType.CANCEL_CHAT_REQUEST) {

                } else if (type == Constants.RequestType.CLOSE_SESSION) {

                } else if (type == Constants.RequestType.RATE_SESSION) {

                } else if (type == Constants.RequestType.SEND_CHAT_MESSAGE) {
                    int result = contentJSON.getInt("result");
                    // 当收到发送的消息返回session不合法时候，认为服务端会话已经关闭了，而客户端由于某些原因没能收到关闭信息
                    // 这时候也去清空会话，并且把原始消息缓存起来，同时发出聊天排队请求
                    if(result == Constants.Result.INVAILD_CHAT_SESSION_ID) {
                        Chat.getInstance().clearRequestAndSession();
                        JSONObject originalMessage = contentJSON.getJSONObject("originalMessage");
                        String objectName = originalMessage.getString("objectName");
                        MessageContent messageContent = null;
                        if (objectName.equals(Constants.ObjectName.TXT_MSG)) {
                            messageContent = new TextMessage(originalMessage.getString("content").getBytes("utf-8"));
                        } else if (objectName.equals(Constants.ObjectName.IMG_MSG)) {
                            messageContent = new ImageMessage(originalMessage.getString("content").getBytes("utf-8"));
                        } else if (objectName.equals(Constants.ObjectName.FILE_MSG)) {
                            messageContent = new FileMessage(originalMessage.getString("content").getBytes("utf-8"));
                        } else if (objectName.equals(Constants.ObjectName.LBS_MSG)) {
                            messageContent = new LocationMessage(originalMessage.getString("content").getBytes("utf-8"));
                        } else if (objectName.equals(Constants.ObjectName.VC_MSG)) {
                            messageContent = new VoiceMessage(originalMessage.getString("content").getBytes("utf-8"));
                        } else if (objectName.equals(Constants.ObjectName.ELITE_MSG)) {
                            messageContent = new EliteMessage(originalMessage.getString("content").getBytes("utf-8"));
                        }
                        if (messageConetent != null) {
                            Message unsendMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.PRIVATE, messageContent);
                            Chat.getInstance().addUnsendMessage(unsendMessage);
                        }
                        Chat.getInstance().sendChatRequest();
                    }

                } else if (type == Constants.RequestType.SEND_PRE_CHAT_MESSAGE) {

                }
                //
                else if (type == Constants.RequestType.CHAT_REQUEST_STATUS_UPDATE) {
                    int requestStatus = contentJSON.getInt("requestStatus");
                    Chat.getInstance().setRequestStatus(requestStatus);
                    int queueLength = contentJSON.getInt("queueLength");
                    InformationNotificationMessage informationMessage = null;
                    if (requestStatus == Constants.RequestStatus.WAITING) {
                        informationMessage = InformationNotificationMessage.obtain("还有" + queueLength + "位，等待中..");
                    } else if (requestStatus == Constants.RequestStatus.DROPPED) {
                        informationMessage = InformationNotificationMessage.obtain("请求异常丢失");
                    } else if (requestStatus == Constants.RequestStatus.TIMEOUT) {
                        informationMessage = InformationNotificationMessage.obtain("请求超时");
                    }
                    if (informationMessage != null) {
                        insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage);
                    }
                } else if (type == Constants.RequestType.CHAT_STARTED) {
                    long sessionId = contentJSON.getLong("sessionId");
                    JSONArray agents = contentJSON.getJSONArray("agents");
                    JSONObject agentJSON = agents.getJSONObject(0);
                    String agentId = agentJSON.getString("id");
                    String name = agentJSON.getString("name");
                    String icon = agentJSON.optString("icon");
                    String comments = agentJSON.optString("comments");
                    boolean robotMode = contentJSON.optBoolean("robotMode");

                    //记录会话相关信息
                    Session session = Chat.getInstance().initSession(sessionId, agentId, name, icon, comments);
                    session.setRobotMode(robotMode);

                    InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain("坐席[" + name + "]为您服务");
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage);
                    Chat.getInstance().sendUnsendMessages();
                } else if (type == Constants.RequestType.AGENT_PUSH_RATING) {
                    // 坐席推送满意度处理
                    ActivityUtils.showRatingDialog();
                } else if (type == Constants.RequestType.AGENT_UPDATED) {
                    // 坐席人员变更处理
                    long sessionId = contentJSON.getLong("sessionId");
                    JSONArray agentsJSON = contentJSON.getJSONArray("agents");
                    Chat.getInstance().clearSessionAgents();
                    for (int i = 0; i < agentsJSON.length(); i++) {
                        JSONObject agentJSON = agentsJSON.getJSONObject(i);
                        String agentId = agentJSON.getString("id");
                        String agentName = agentJSON.getString("name");
                        String icon = agentJSON.optString("icon");
                        String comments = agentJSON.optString("comments");
                        Chat.getInstance().setupAgent(agentId, agentName, icon, comments);
                    }
                    //如果收到了坐席人员变更消息，肯定就是指转人工了，也就是不再是机器人服务了（只存在机器人转人工，不存在人工转机器人的情况）
                    Chat.getInstance().getSession().setRobotMode(false);
                } else if (type == Constants.RequestType.AGENT_CLOSE_SESSION) {
                    Chat.getInstance().clearRequestAndSession();
                    InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain("会话结束");
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage);
                } else if (type == Constants.RequestType.AGENT_SEND_MESSAGE) {
                    String agentId = contentJSON.getString("agentId");
                    JSONObject msgJSON = contentJSON.getJSONObject("msg");
                    int msgType = msgJSON.getInt("type");
                    if (msgType == Constants.MessageType.SYSTEM_NOTICE) {
                        int noticeType = msgJSON.getInt("noticeType");
                        if (noticeType == Constants.NoticeMessageType.NORMAL ||
                                noticeType == Constants.NoticeMessageType.AFK_ELAPSED_CLOSE_SESSION ||
                                noticeType == Constants.NoticeMessageType.AFK_ELAPSED_NOTIFY) {
                            String content = msgJSON.getString("content");
                            TextMessage textMessage = TextMessage.obtain(content);
                            insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), agentId, textMessage);
                        } else if (noticeType == Constants.NoticeMessageType.INVITE_NOTICE ||
                                noticeType == Constants.NoticeMessageType.TRANSFER_NOTICE) {//转接或者邀请的消息作为通知类消息
                            //String content = msgJSON.getString("content");
                            //InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain(content);
                            //insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage);
                        }
                    }
                } else if (type == Constants.RequestType.ROBOT_MESSAGE) {//机器人消息
                    String agentId = Chat.getInstance().getSession().getAgent().getId();
                    String content = contentJSON.getString("content");
                    int robotType = contentJSON.getInt("robotType");
                    long time = contentJSON.getLong("time");
                    content = RobotMessageHandler.handleMessage(content, robotType);
                    TextMessage textMessage = TextMessage.obtain(content);
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), agentId, textMessage, time);
                } else if (type == Constants.RequestType.ROBOT_TRANSFER_MESSAGE) {
                    int result = contentJSON.getInt("result");
                    InformationNotificationMessage inm = null;
                    if(result == Constants.Result.SUCCESS){
                        int queueLength = contentJSON.optInt("queueLength");
                        if (queueLength == 0) {
                            inm = InformationNotificationMessage.obtain(contentJSON.getString("message"));
                        } else {
                            inm = InformationNotificationMessage.obtain("当前排在第" + queueLength + "位");
                        }
                    } else {
                        inm = InformationNotificationMessage.obtain(contentJSON.optString("message"));
                    }
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm);
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "EliteReceiveMessageListener.onReceived: " + e.getMessage());
            }
        } else if (messageConetent instanceof TextMessage) {//坐席发的文字聊天消息，都作为TextMessage发送过来
            //设置一个userInfo对象到messageContent，实现后台消息提示，其中userId必须是我们的targetId: EliteCRM
//            try {
//                TextMessage textMessage = (TextMessage)messageConetent;
//                String extraStr = textMessage.getExtra();
//                JSONObject extraJSON = new JSONObject(extraStr);
//                String agentId = extraJSON.getString("agentId");
//                String agentName = extraJSON.getString("agentName");
//                String icon = extraJSON.optString("icon");
//                if(icon != null && !icon.startsWith("http://") && !icon.startsWith("https://")){
//                    icon = EliteChat.getNgsAddr() + "/fs/get?file=" + icon;
//                }
//                Uri iconUri = Uri.parse(icon);
//                UserInfo userInfo = new UserInfo(Constants.CHAT_TARGET_ID, agentName, iconUri);
//                messageConetent.setUserInfo(userInfo);
//            } catch (Exception e) {
//                Log.e(Constants.LOG_TAG, e.getMessage());
//            }
//            TextMessage tm = (TextMessage)messageConetent;
//            String contentStr = tm.getContent();
//            Log.d(Constants.LOG_TAG, contentStr);
        } else if (messageConetent instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage)messageConetent;
            //fileMessage.setSize(0);
        }
        return false;//这里返回true就不会有消息声音提示
    }


}
