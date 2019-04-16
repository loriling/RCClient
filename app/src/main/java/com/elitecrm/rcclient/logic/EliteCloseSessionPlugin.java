package com.elitecrm.rcclient.logic;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.elitecrm.rcclient.EliteChat;
import com.elitecrm.rcclient.R;
import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.util.ActivityUtils;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.MessageUtils;

import org.json.JSONObject;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imlib.model.Conversation;
import io.rong.message.InformationNotificationMessage;

import static com.elitecrm.rcclient.EliteChat.doCloseSession;

/**
 * Created by Loriling on 2018/11/29.
 */

public class EliteCloseSessionPlugin implements IPluginModule {
    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.elite_ext_plugin_close_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return "结束会话";
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {
        Chat chat = Chat.getInstance();
        Session session = chat.getSession();
        if (session != null) {
            if (!chat.isPushRating()) {
                chat.setPushRating(true);
                ActivityUtils.showRatingDialog(chat.getSession().getId());
            }
            new CloseSessionTask().execute(EliteChat.getServerAddr(), session.getId() + "");
        } else {
            InformationNotificationMessage inm = InformationNotificationMessage.obtain("会话已结束");
            MessageUtils.insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm);
        }
        rongExtension.collapseExtension();
    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }

    public static class CloseSessionTask extends AsyncTask<String, Void, String> {
        String serverAddr;
        long sessionId;

        @Override
        protected String doInBackground(String... params) {
            try {
                serverAddr = params[0];
                sessionId = Long.parseLong(params[1]);

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
                Chat.getInstance().clearRequestAndSession();
                InformationNotificationMessage inm = InformationNotificationMessage.obtain("会话以结束");
                MessageUtils.insertMessage(Conversation.ConversationType.PRIVATE, Chat.getInstance().getClient().getTargetId(), null, inm);
            }
        }
    }
}
