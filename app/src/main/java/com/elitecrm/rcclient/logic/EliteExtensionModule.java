package com.elitecrm.rcclient.logic;

import android.util.Log;

import com.elitecrm.rcclient.baidumap.BaiduLocationPlugin;
import com.elitecrm.rcclient.util.Constants;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.DefaultLocationPlugin;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imkit.widget.provider.SightMessageItemProvider;
import io.rong.imlib.model.Conversation;
import io.rong.message.SightMessage;
import io.rong.sight.SightPlugin;

/**
 * Created by Loriling on 2017/2/9.
 * 自定义聊天界面加号按钮点开后，看到的相关插件
 */

public class EliteExtensionModule extends DefaultExtensionModule {
    List<IPluginModule> pluginModuleList = new ArrayList<>();

    public EliteExtensionModule enableImage(boolean enable) {
        if (enable) {
            pluginModuleList.add(new ImagePlugin());
        }
        return this;
    }
    public EliteExtensionModule enableMap(String type) {
        if ("baidu".equalsIgnoreCase(type)) {
            //百度地图插件
            pluginModuleList.add(new BaiduLocationPlugin());
        } else if ("amap".equalsIgnoreCase(type)) {
            //高德地图
            try {
                String amapClassName = "com.amap.api.netlocation.AMapNetworkLocationClient";
                Class cls = Class.forName(amapClassName);
                if(cls != null) {
                    DefaultLocationPlugin locationPlugin = new DefaultLocationPlugin();
                    pluginModuleList.add(locationPlugin);
                }
            } catch (Exception exception) {
                Log.i(Constants.LOG_TAG, "Not include AMap");
            }
        }
        return this;
    }

    public EliteExtensionModule enableFile(boolean enable) {
        if (enable) {
            pluginModuleList.add(new FilePlugin());
        }
        return this;
    }

    public EliteExtensionModule enableSight(boolean enable) {
        if (enable) {
            RongIM.registerMessageType(SightMessage.class);
            RongIM.registerMessageTemplate(new SightMessageItemProvider());
            pluginModuleList.add(new SightPlugin());
        }
        return this;
    }

    public EliteExtensionModule enableCloseSession(boolean enable) {
        if (enable) {
            pluginModuleList.add(new EliteCloseSessionPlugin());
        }
        return this;
    }

    private EliteExtensionModule() {

    }

    public static EliteExtensionModule getInstance() {
        return new EliteExtensionModule();
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        return pluginModuleList;
    }
}
