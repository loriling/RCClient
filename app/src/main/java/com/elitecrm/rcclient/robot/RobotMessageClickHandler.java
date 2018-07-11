package com.elitecrm.rcclient.robot;

import android.view.View;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.util.MessageUtils;

/**
 * Created by Loriling on 2018/7/3.
 */

public class RobotMessageClickHandler implements RobotMessageClickURLSpan.OnClickListener{
    @Override
    public void onClick(View view, String url) {
        System.out.println("onclick: " + url);
        if ("【转人工】".equals(url)) {
            MessageUtils.sendTransferMessage();
        } else {
            url = url.replace("【", "").replace("】", "");
            MessageUtils.sendTextMessage(url, Chat.getInstance().getClient().getTargetId());
        }
    }
}
