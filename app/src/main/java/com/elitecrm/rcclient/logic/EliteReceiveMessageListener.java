package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.message.EliteMessage;
import com.elitecrm.rcclient.message.RobotMessage;
import com.elitecrm.rcclient.robot.RobotMessageHandler;
import com.elitecrm.rcclient.util.ActivityUtils;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.MessageUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.TextMessage;

import static com.elitecrm.rcclient.util.MessageUtils.insertMessage;

/**
 * Created by Loriling on 2017/2/3.
 */

public class EliteReceiveMessageListener implements RongIMClient.OnReceiveMessageListener {
    @Override
    public boolean onReceived(Message message, int left) {
        String objName = message.getObjectName();
        MessageContent messageConetent = message.getContent();
        long receivedTime = message.getReceivedTime() + 3000; // 这里加3秒后消息顺序就正常了，先这样吧

        if (objName.equals(Constants.ObjectName.ELITE_MSG)) {//所有的状态通知
            EliteMessage eliteMessage = new EliteMessage(messageConetent.encode());
            String contentStr = eliteMessage.getMessage();
            try {
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
                    if (result == Constants.Result.SUCCESS) {
                        if (contentJSON.has("sessionId")) {// 如果会话已经有了
                            long sessionId = contentJSON.getLong("sessionId");
                            String agentId = contentJSON.optString("agentId");
                            String agentName = contentJSON.optString("agentName");
                            String icon = contentJSON.optString("icon");
                            String comments = contentJSON.optString("comments");
                            Chat.getInstance().initSession(sessionId, agentId, agentName, icon, comments);
                        } else {
                            int queueLength = contentJSON.getInt("queueLength");
                            if (queueLength == 0) {
                                //inm = InformationNotificationMessage.obtain(contentJSON.getString("message")); //继续之前会话;机器人会话开始  这些提示不是不是不需要显示出来了
                            } else {
                                inm = InformationNotificationMessage.obtain("当前排在第" + queueLength + "位");
                            }
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
                        insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm, receivedTime);
                } else if (type == Constants.RequestType.CANCEL_CHAT_REQUEST) {

                } else if (type == Constants.RequestType.CLOSE_SESSION) {

                } else if (type == Constants.RequestType.RATE_SESSION) {

                } else if (type == Constants.RequestType.SEND_CHAT_MESSAGE) {
                    int result = contentJSON.getInt("result");
                    // 当收到发送的消息返回session不合法时候，认为服务端会话已经关闭了，而客户端由于某些原因没能收到关闭信息
                    // 这时候也去清空会话，并且把原始消息缓存起来，同时发出聊天排队请求
                    if (result == Constants.Result.INVAILD_CHAT_SESSION_ID) {
                        Chat.getInstance().clearRequestAndSession();
                        JSONObject originalMessage = contentJSON.getJSONObject("originalMessage");
                        String objectName = originalMessage.getString("objectName");
                        MessageContent messageContent = MessageUtils.generateMessageContent(objectName, originalMessage.getString("content"));
                        if (messageConetent != null) {
                            Message unsendMessage = Message.obtain(Chat.getInstance().getClient().getTargetId(), Conversation.ConversationType.PRIVATE, messageContent);
                            unsendMessage.setObjectName(objectName);
                            Chat.getInstance().addUnsendMessage(unsendMessage);
                        }
                        Chat.getInstance().sendChatRequest();
                    } else {
                        if (contentJSON.has("sessionId")) {
                            long sessionId = contentJSON.getLong("sessionId");
                            JSONArray agents = contentJSON.getJSONArray("agents");
                            JSONObject agentJSON = agents.getJSONObject(0);
                            String agentId = agentJSON.getString("id");
                            String name = agentJSON.getString("name");
                            String icon = agentJSON.optString("icon");
                            String comments = agentJSON.optString("comments");
                            Chat.getInstance().initSession(sessionId, agentId, name, icon, comments);
                        }
                    }

                } else if (type == Constants.RequestType.SEND_PRE_CHAT_MESSAGE) {

                }
                //
                else if (type == Constants.RequestType.CHAT_REQUEST_STATUS_UPDATE) {
                    JSONObject dataJSON = contentJSON.getJSONObject("data");
                    int requestStatus = dataJSON.getInt("requestStatus");
                    Chat.getInstance().setRequestStatus(requestStatus);
                    int queueLength = dataJSON.getInt("queueLength");
                    InformationNotificationMessage informationMessage = null;
                    if (requestStatus == Constants.RequestStatus.WAITING) {
                        informationMessage = InformationNotificationMessage.obtain("还有" + queueLength + "位，等待中..");
                    } else if (requestStatus == Constants.RequestStatus.DROPPED) {
                        informationMessage = InformationNotificationMessage.obtain("请求异常丢失");
                    } else if (requestStatus == Constants.RequestStatus.TIMEOUT) {
                        informationMessage = InformationNotificationMessage.obtain("请求超时");
                    } else {
                        informationMessage = InformationNotificationMessage.obtain("请求异常: " + requestStatus);
                    }
                    if (informationMessage != null) {
                        insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage, receivedTime);
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
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage, receivedTime);
                    Chat.getInstance().sendUnsendMessages();
                } else if (type == Constants.RequestType.AGENT_PUSH_RATING) {
                    // 坐席推送满意度处理
                    Chat.getInstance().setPushRating(true);
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
                    Chat chat = Chat.getInstance();
                    if (!chat.isPushRating()) {
                        ActivityUtils.showRatingDialog(chat.getSession().getId());
                    }
                    Chat.getInstance().clearRequestAndSession();
                    InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain("会话结束");
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage, receivedTime);
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
                            insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), agentId, textMessage, receivedTime);
                        } else if (noticeType == Constants.NoticeMessageType.INVITE_NOTICE ||
                                noticeType == Constants.NoticeMessageType.TRANSFER_NOTICE) {//转接或者邀请的消息作为通知类消息
                            String content = msgJSON.getString("content");
                            InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain(content);
                            insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, informationMessage);
                        }
                    }
                }
                // @Deprecated 服务端会发送真正的E:RobMsg过来，而不是再用这种发送E:Msg然后前台再自己insert的方式了
                else if (type == Constants.RequestType.ROBOT_MESSAGE) {//机器人消息
                    String agentId = Chat.getInstance().getSession().getAgent().getId();
                    String content = contentJSON.getString("content");
                    Log.d("robot", "onReceived: " + content);
                    int robotType = contentJSON.getInt("robotType");
                    content = RobotMessageHandler.handleMessage(content, robotType);
                    RobotMessage robotMessage = RobotMessage.obtain(content);
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), agentId, robotMessage, receivedTime);
                }
                else if (type == Constants.RequestType.ROBOT_TRANSFER_MESSAGE) {
                    int result = contentJSON.getInt("result");
                    InformationNotificationMessage inm = null;
                    if (result == Constants.Result.SUCCESS) {
                        int queueLength = contentJSON.optInt("queueLength");
                        if (queueLength == 0) {
                            inm = InformationNotificationMessage.obtain(contentJSON.getString("message"));
                        } else {
                            inm = InformationNotificationMessage.obtain("当前排在第" + queueLength + "位");
                        }
                    } else {
                        inm = InformationNotificationMessage.obtain(contentJSON.optString("message"));
                    }
                    insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm, receivedTime);
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "EliteReceiveMessageListener.onReceived: " + e.getMessage());
            }
        } else {

        }
        return false;//这里返回true就不会有消息声音提示
    }


}
