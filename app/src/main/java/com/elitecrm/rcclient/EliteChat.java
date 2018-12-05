package com.elitecrm.rcclient;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.logic.EliteSendMessageListener;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.DigestUtils;
import com.elitecrm.rcclient.util.HttpUtil;

import org.json.JSONObject;

import java.net.HttpURLConnection;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

import static com.elitecrm.rcclient.util.Constants.CHAT_TITLE;

/**
 * Created by Loriling on 2017/2/13.
 */

public class EliteChat {
    private static Context context;
    private static boolean startChatOnReady = false;
    private static String serverAddr;
    private static String ngsAddr;

    /**
     * 初始化EliteChat， 获取rongcloud的token，并且启动聊天。
     * 如果发现token已经存在并且融云连接状态还是连接中的，则直接进入聊天
     * @param serverAddr EliteWebChat服务地址
     * @param userId 用户登录id
     * @param name 用户名
     * @param portraitUri 用户头像uri
     * @param context 当前上下文
     * @param queueId 排队队列号
     * @param ngsAddr ngs服务地址
     * @param from 请求来源
     */
    public static void initAndStart(String serverAddr, String userId, String name, String portraitUri, String targetId, Context context, int queueId, String ngsAddr, String from, String tracks) {
        EliteChat.context = context;
        if (Chat.getInstance().isTokenValid() && RongIMClient.getInstance().getCurrentConnectionStatus() == RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED) {
            Chat.getInstance().initClient(userId, name, portraitUri, targetId);
            Chat.getInstance().initRequest(queueId, from, tracks);
            //发出聊天排队请求
            Chat.getInstance().sendChatRequest();
            //启动聊天会话界面
            RongIM.getInstance().startConversation(EliteChat.context, Conversation.ConversationType.PRIVATE, targetId, Constants.CHAT_TITLE);
        } else {
            EliteChat.initServerAddr(serverAddr);
            if (ngsAddr != null) {
                EliteChat.setNgsAddr(ngsAddr);
            }
            Chat.getInstance().initRequest(queueId, from, tracks);
            startChatOnReady = true;
            new FetchTokenTask().execute(serverAddr, userId, name, portraitUri, targetId);
        }
    }

    public static void initAndStart(String serverAddr, String userId, String name, String portraitUri, String targetId, Context context, int queueId, String ngsAddr) {
        initAndStart(serverAddr, userId, name, portraitUri, targetId, context, queueId, ngsAddr, "APP", "");
    }

    public static void initAndStart(String serverAddr, String userId, String name, String portraitUri, String targetId, Context context, int queueId) {
        initAndStart(serverAddr, userId, name, portraitUri, targetId, context, queueId, null, "APP", "");
    }

    /**
     * 初始化EliteChat，获取融云token
     * @param serverAddr EliteWebChat服务地址
     * @param userId 用户登录id
     * @param name 用户名
     * @param portraitUri 用户头像uri
     */
    public static void init(String serverAddr, String userId, String name, String portraitUri, String targetId) {
        EliteChat.initServerAddr(serverAddr);
        new FetchTokenTask().execute(serverAddr, userId, name, portraitUri, targetId);
    }

    /**
     * 当收到提醒时候，onNotificationMessageClicked 方法触发，然后可以通过调用此方法来打开chat
     * @param context
     */
    public static void switchToChat(Context context, String target) {
        if (context != null) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Uri.Builder builder = Uri.parse("rong://" + context.getPackageName()).buildUpon();
            builder.appendPath("conversation").appendPath(Conversation.ConversationType.PRIVATE.getName())
                    .appendQueryParameter("targetId", target)
                    .appendQueryParameter("title", Constants.CHAT_TITLE);
            Uri uri = builder.build();
            Log.d(Constants.LOG_TAG, "Switch to chat: " + uri.toString());
            intent.setData(uri);
            context.startActivity(intent);
        }
    }

    public static void checkToken(String serverAddr, String token,  String userId, String name, String portraitUri, String targetId, Context context, int queueId, String ngsAddr, String from, String tracks) {
        EliteChat.context = context;
        new CheckStateTask().execute(serverAddr, token, userId, name, portraitUri, targetId, queueId + "", ngsAddr, from, tracks);
    }

    /**
     * 聊天的入口
     * @param serverAddr
     * @param token
     * @param userId
     * @param name
     * @param portraitUri
     * @param targetId
     * @param context
     * @param queueId
     * @param ngsAddr
     * @param from
     * @param tracks
     */
    public static void startChat(String serverAddr, String token,  String userId, String name, String portraitUri, String targetId, Context context, int queueId, String ngsAddr, String from, String tracks) {
        //检查token是否可用，如果可用则继续之前的使用，不可用则从新init
        if (Chat.getInstance().isSessionAvailable()) {
            if (Chat.getInstance().isTokenValid()) {
                if (queueId != Chat.getInstance().getRequest().getQueueId()) {//如果队列改变了，则直接结束之前会话，并开启新的会话
                    EliteChat.closeSession(serverAddr, Chat.getInstance().getSession().getId(), userId, name, portraitUri, targetId, context, queueId, ngsAddr, from, tracks);
                } else {
                    // 如果队列没变化，则去检查token是否还合法，如果合法则可以直接继续聊天。如果不合法则需要重新登录
                    EliteChat.checkToken(serverAddr, token, userId, name, portraitUri, targetId, context, queueId, ngsAddr, from, tracks);
                }
                return;
            }
        } else {
            EliteChat.initAndStart(serverAddr, userId, name, portraitUri, targetId, context, queueId, ngsAddr, from, tracks);
        }
    }

    /**
     * 通过调用webchat服务，获取融云token
     */
    public static class FetchTokenTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String token = null;
            try {
                String serverAddr = params[0];
                String userId = params[1];
                String name = params[2];
                String portraitUri = params[3];
                String targetId = params[4];
                HttpURLConnection conn = HttpUtil.createPostHttpConnection(serverAddr + "/rcs", "application/json");
                JSONObject requestJSON = new JSONObject();
                requestJSON.put("action", "login");

                Chat.getInstance().initClient(userId, name, portraitUri, targetId);

                requestJSON.put("userId", userId);
                requestJSON.put("name", name);
                requestJSON.put("portraitUri", portraitUri);
                requestJSON.put("targetId", targetId);
                String body = requestJSON.toString();
                conn.setRequestProperty("sign", DigestUtils.md5Hex(body + Constants.SecurityKey.PUBLIC_KEY));
                HttpUtil.setBodyParameter(body, conn);
                String resultStr = HttpUtil.returnResult(conn);
                JSONObject resultJSON = new JSONObject(resultStr);
                if (1 == resultJSON.getInt("result")) {
                    token = resultJSON.getString("token");
                    Chat.getInstance().setToken(token);
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "FetchToken: " + e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(final String token) {
            RongIM.connect(token, new RongIMClient.ConnectCallback() {

                /**
                 * Token 错误。可以从下面两点检查
                 * 1.  Token 是否过期，如果过期您需要向 App Server 重新请求一个新的 Token
                 * 2.  token 对应的 appKey 和工程里设置的 appKey 是否一致
                 */
                @Override
                public void onTokenIncorrect() {
                    Chat.getInstance().setToken(null);
                    Log.e(Constants.LOG_TAG, "onTokenIncorrect");
                }

                /**
                 * 连接融云成功
                 * @param userId 当前 token 对应的用户 id
                 */
                @Override
                public void onSuccess(String userId) {
                    Log.d(Constants.LOG_TAG, "onSuccess: " + userId);
                    //连接成功，记录用户id
                    Chat.getInstance().setupClient(userId);
                    //注册发送消息监听器
                    RongIM.getInstance().setSendMessageListener(new EliteSendMessageListener());
                    if (startChatOnReady) {
                        //发出聊天排队请求
                        Chat.getInstance().sendChatRequest();
                        //启动聊天会话界面
                        Log.d(Constants.LOG_TAG, "Start Conversation: " + Chat.getInstance().getClient().getTargetId());
                        RongIM.getInstance().startConversation(EliteChat.context, Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), CHAT_TITLE);
                    }
                }

                /**
                 * 连接融云失败
                 * @param errorCode 错误码，可到官网 查看错误码对应的注释
                 */
                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                    Chat.getInstance().setToken(null);
                    Log.e(Constants.LOG_TAG, "RongIM.connect onError: " + errorCode);
                }
            });
        }
    }

    /**
     * 检查当前token是否合法，如果合法继续使用，如果不合法则从新登录
     */
    public static class CheckStateTask extends AsyncTask<String, Void, String> {
        String serverAddr;
        String userId;
        String name;
        String portraitUri;
        String targetId;
        int queueId;
        String ngsAddr;
        String from;
        String tracks;
        @Override
        protected String doInBackground(String... params) {
            try {
                serverAddr = params[0];
                String token = params[1];
                userId = params[2];
                name = params[3];
                portraitUri = params[4];
                targetId = params[5];
                queueId = Integer.parseInt(params[6]);
                ngsAddr = params[7];
                from = params[8];
                tracks = params[9];
                HttpURLConnection conn = HttpUtil.createPostHttpConnection(serverAddr + "/rcs", "application/json");
                JSONObject requestJSON = new JSONObject();
                requestJSON.put("action", "check");
                requestJSON.put("token", token);
                String body = requestJSON.toString();
                conn.setRequestProperty("sign", DigestUtils.md5Hex(body + Constants.SecurityKey.PUBLIC_KEY));
                HttpUtil.setBodyParameter(body, conn);
                String resultStr = HttpUtil.returnResult(conn);
                JSONObject resultJSON = new JSONObject(resultStr);
                if (1 == resultJSON.getInt("result")) {
                    return "success";
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "CheckState: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if ("success".equals(result)) {
                RongIM.getInstance().startConversation(EliteChat.context, Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), "在线客服");
            } else {
                Chat.getInstance().clear();
                initAndStart(serverAddr, userId, name, portraitUri, targetId, EliteChat.context, queueId, ngsAddr, from, tracks);
            }
        }
    }

    public static void initServerAddr(String serverAddr) {
        setServerAddr(serverAddr);
        String ngsAddr = serverAddr.substring(0, serverAddr.lastIndexOf("/")) + "/ngs";
        setNgsAddr(ngsAddr);
    }

    public static void closeSession(String serverAddr, long sessionId, String userId, String name, String portraitUri, String targetId, Context context, int queueId, String ngsAddr, String from, String tracks) {
        EliteChat.context = context;
        new CloseSessionTask().execute(serverAddr, sessionId + "", userId, name, portraitUri, targetId, queueId + "", ngsAddr, from, tracks);
    }

    public static class CloseSessionTask extends AsyncTask<String, Void, String> {
        String serverAddr;
        String userId;
        String name;
        String portraitUri;
        String targetId;
        int queueId;
        String ngsAddr;
        String from;
        String tracks;
        long sessionId;

        @Override
        protected String doInBackground(String... params) {
            try {
                serverAddr = params[0];
                sessionId = Long.parseLong(params[1]);
                userId = params[2];
                name = params[3];
                portraitUri = params[4];
                targetId = params[5];
                queueId = Integer.parseInt(params[6]);
                ngsAddr = params[7];
                from = params[8];
                tracks = params[9];

                String resultStr = doCloseSession(serverAddr, sessionId);
                JSONObject resultJSON = new JSONObject(resultStr);
                if (1 == resultJSON.getInt("result")) {
                    return "success";
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, "CloseSession: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if ("success".equals(result)) {
                Log.e(Constants.LOG_TAG, "CloseSession successful");
                initAndStart(serverAddr, userId, name, portraitUri, targetId, EliteChat.context, queueId, ngsAddr, from, tracks);
            }
        }
    }

    public static String doCloseSession(String serverAddr, long sessionId) throws Exception {
        HttpURLConnection conn = HttpUtil.createPostHttpConnection(serverAddr + "/rcs", "application/json");
        JSONObject requestJSON = new JSONObject();
        requestJSON.put("action", "closeSession");
        requestJSON.put("sessionId", sessionId);
        String body = requestJSON.toString();
        conn.setRequestProperty("sign", DigestUtils.md5Hex(body + Constants.SecurityKey.PUBLIC_KEY));
        HttpUtil.setBodyParameter(body, conn);
        String resultStr = HttpUtil.returnResult(conn);
        return resultStr;
    }

    public static String getNgsAddr() {
        return ngsAddr;
    }

    public static void setNgsAddr(String ngsAddr) {
        EliteChat.ngsAddr = ngsAddr;
    }

    public static String getServerAddr() {
        return serverAddr;
    }

    public static void setServerAddr(String serverAddr) {
        EliteChat.serverAddr = serverAddr;
    }
}
