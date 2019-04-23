package com.elitecrm.rcclient.logic;

import android.net.Uri;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.entity.Session;
import com.elitecrm.rcclient.entity.User;

import io.rong.imkit.RongIM.UserInfoProvider;
import io.rong.imlib.model.UserInfo;

/**
 *
 * 用户信息提供者，没吃消息发出去时候会调用，根据id来获取对应的用户信息
 * Created by Loriling on 2017/2/21.
 */
public class EliteUserInfoProvider implements UserInfoProvider {
    @Override
    public UserInfo getUserInfo(String userId) {
        User client = Chat.getInstance().getClient();
        if (client != null && userId.equals(client.getId())) {
            UserInfo clientUserInfo = new UserInfo(client.getId(), client.getName(), Uri.parse(client.getIcon()));
            return clientUserInfo;
        }

        Session session = Chat.getInstance().getSession();
        if (session != null) {
            String agentIcon = session.getFirstAgent().getIcon();
            UserInfo agentUserInfo = new UserInfo(userId, session.getFirstAgent().getName(), Uri.parse(agentIcon));
            return agentUserInfo;
            //现在使用的单聊，坐席端统一用EliteCRM作为id，所以不区分不同坐席
//            Map<String, Agent> agents = session.getAgents();
//            Agent agent = agents.get(userId);
//            if(agent != null) {
//                UserInfo agentUserInfo = new UserInfo(userId, agent.getName(), Uri.parse(agent.getIcon()));
//                return agentUserInfo;
//            }
        }

        return null;
    }
}
