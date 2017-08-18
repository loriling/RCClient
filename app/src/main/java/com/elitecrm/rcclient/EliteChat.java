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
    private static boolean initialized = false;
    private static boolean startChatReady = false;
    private static String serverAddr;
    private static String ngsAddr;

    /**
     * 初始化EliteChat， 并且启动聊天
     * @param serverAddr EliteWebChat服务地址
     * @param userId 用户登录id
     * @param name 用户名
     * @param portraitUri 用户头像uri
     * @param context 当前上下文
     * @param queueId 排队队列号
     * @param ngsAddr ngs服务地址
     */
    public static void initAndStart(String serverAddr, String userId, String name, String portraitUri, String targetId, Context context, int queueId, String ngsAddr) {
        EliteChat.context = context;
        EliteChat.initServerAddr(serverAddr);
        if(ngsAddr != null) {
            EliteChat.setNgsAddr(ngsAddr);
        }
        Chat.getInstance().initRequest(queueId);
        startChatReady = true;
        new FetchTokenTask().execute(serverAddr + "/rcs", userId, name, portraitUri, targetId);
    }

    /**
     * 初始化EliteChat， 并且启动聊天
     * @param serverAddr
     * @param userId
     * @param name
     * @param portraitUri
     * @param context
     * @param queueId
     */
    public static void initAndStart(String serverAddr, String userId, String name, String portraitUri, String targetId, Context context, int queueId) {
        initAndStart(serverAddr, userId, name, portraitUri, targetId, context, queueId, null);
    }

    /**
     * 初始化EliteChat
     * @param serverAddr EliteWebChat服务地址
     * @param userId 用户登录id
     * @param name 用户名
     * @param portraitUri 用户头像uri
     */
    public static void init(String serverAddr, String userId, String name, String portraitUri, String targetId) {
        EliteChat.initServerAddr(serverAddr);
        new FetchTokenTask().execute(serverAddr + "/rcs", userId, name, portraitUri, targetId);
    }

    /**
     * 启动聊天
     * @param context 当前上下文
     * @param queueId 排队队列号
     */
    public static void startChat(Context context, int queueId) {
        EliteChat.context = context;
        Chat.getInstance().initRequest(queueId);
        if(initialized) {
            //发出聊天排队请求
            Chat.getInstance().sendChatRequest();
            //启动聊天会话界面
            RongIM.getInstance().startConversation(EliteChat.context, Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), Constants.CHAT_TITLE);
        }
    }

    /**
     * 当收到提醒时候，onNotificationMessageClicked 方法触发，然后可以通过调用此方法来打开chat
     * @param context
     */
    public static void switchToChat(Context context) {
        if(context != null) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri.Builder builder = Uri.parse("rong://" + context.getPackageName()).buildUpon();
            builder.appendPath("conversation").appendPath(Conversation.ConversationType.PRIVATE.getName())
                    .appendQueryParameter("targetId", Chat.getInstance().getClient().getTargetId())
                    .appendQueryParameter("title", Constants.CHAT_TITLE);
            Uri uri = builder.build();
            intent.setData(uri);
            context.startActivity(intent);
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
                HttpURLConnection conn = HttpUtil.createPostHttpConnection(serverAddr, "application/json");
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
                if(1 == resultJSON.getInt("result")){
                    token = resultJSON.getString("token");
                    Chat.getInstance().setToken(token);
                }
            } catch (Exception e) {
                Log.e(Constants.LOG_TAG, e.getMessage());
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
                    initialized = true;
                    if (startChatReady) {
                        //发出聊天排队请求
                        Chat.getInstance().sendChatRequest();
                        //启动聊天会话界面
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
                    Log.e(Constants.LOG_TAG, "onError: " + errorCode);
                }
            });
        }
    }

    public static void initServerAddr(String serverAddr) {
        setServerAddr(serverAddr);
        String ngsAddr = serverAddr.substring(0, serverAddr.lastIndexOf("/")) + "/ngs";
        setNgsAddr(ngsAddr);
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
