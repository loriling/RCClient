package com.elitecrm.rcclient.logic;

import com.elitecrm.rcclient.baidumap.BaiduLocationPlugin;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.manager.InternalModuleManager;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.plugin.ImagePlugin;
import io.rong.imkit.widget.provider.FilePlugin;
import io.rong.imlib.model.Conversation;

/**
 * Created by Loriling on 2017/2/9.
 * 自定义聊天界面加号按钮点开后，看到的相关插件
 */

public class EliteExtensionModule extends DefaultExtensionModule {

    @Override
    public List<IPluginModule> getPluginModules(Conversation.ConversationType conversationType) {
        ArrayList pluginModuleList = new ArrayList();
        ImagePlugin image = new ImagePlugin();
        FilePlugin file = new FilePlugin();
        pluginModuleList.add(image);

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

        if(conversationType.equals(Conversation.ConversationType.GROUP) || conversationType.equals(Conversation.ConversationType.DISCUSSION) || conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            pluginModuleList.addAll(InternalModuleManager.getInstance().getExternalPlugins(conversationType));
        }

        pluginModuleList.add(file);

        return pluginModuleList;
    }
}
