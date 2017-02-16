package com.elitecrm.rcclient.logic;

import com.elitecrm.rcclient.util.Constants;

import java.util.ArrayList;
import java.util.List;

import io.rong.common.RLog;
import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.manager.InternalModuleManager;
import io.rong.imkit.plugin.DefaultLocationPlugin;
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

        String e;
        Class cls;
        try {
            e = "com.amap.api.netlocation.AMapNetworkLocationClient";
            cls = Class.forName(e);
            if(cls != null) {
                DefaultLocationPlugin locationPlugin = new DefaultLocationPlugin();
                pluginModuleList.add(locationPlugin);
            }
        } catch (Exception var10) {
            RLog.i(Constants.LOG_TAG, "Not include AMap");
            var10.printStackTrace();
        }

        if(conversationType.equals(Conversation.ConversationType.GROUP) || conversationType.equals(Conversation.ConversationType.DISCUSSION) || conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            pluginModuleList.addAll(InternalModuleManager.getInstance().getExternalPlugins(conversationType));
        }

        pluginModuleList.add(file);

        return pluginModuleList;
    }
}
