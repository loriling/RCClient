package com.elitecrm.rcclient.logic;

import com.elitecrm.rcclient.baidumap.BaiduLocationPlugin;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imlib.model.Conversation;
import io.rong.sight.SightPlugin;
import io.rong.sight.message.SightMessage;
import io.rong.sight.message.SightMessageItemProvider;

/**
 * Created by Loriling on 2017/2/9.
 * 自定义聊天界面加号按钮点开后，看到的相关插件
 */

public class EliteExtensionModule extends DefaultExtensionModule {
    List<IPluginModule> pluginModuleList;
    private boolean enableSight;

    public EliteExtensionModule(boolean enableSight) {
        this.enableSight = enableSight;
        if (enableSight) {
            RongIM.registerMessageType(SightMessage.class);
            RongIM.registerMessageTemplate(new SightMessageItemProvider());
        }
    }

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        pluginModuleList = new ArrayList<>();
        pluginModuleList.add(new ImagePlugin());
        //高德地图
//        try {
//            String amapClassName = "com.amap.api.netlocation.AMapNetworkLocationClient";
//            Class cls = Class.forName(amapClassName);
//            if(cls != null) {
//                DefaultLocationPlugin locationPlugin = new DefaultLocationPlugin();
//                pluginModuleList.add(locationPlugin);
//            }
//        } catch (Exception exception) {
//            Log.i(Constants.LOG_TAG, "Not include AMap");
//        }

        //百度地图插件
        pluginModuleList.add(new BaiduLocationPlugin());
        pluginModuleList.add(new FilePlugin());

        if (this.enableSight) {
            pluginModuleList.add(new SightPlugin());
        }
        return pluginModuleList;
    }
}
