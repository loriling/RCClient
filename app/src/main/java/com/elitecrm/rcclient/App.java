package com.elitecrm.rcclient;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.logic.EliteExtensionModule;
import com.elitecrm.rcclient.logic.EliteReceiveMessageListener;
import com.elitecrm.rcclient.logic.EliteUserInfoProvider;
import com.elitecrm.rcclient.message.CardMessage;
import com.elitecrm.rcclient.message.CardMessageItemProvider;
import com.elitecrm.rcclient.message.EliteMessage;
import com.elitecrm.rcclient.message.RobotMessage;
import com.elitecrm.rcclient.message.RobotMessageItemProvider;

import java.util.List;

import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;

/**
 * Created by Loriling on 2017/2/3.
 */

public class App extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * OnCreate 会被多个进程重入，这段保护代码，确保只有您需要使用 RongIMClient 的进程和 Push 进程执行了 init。
         * io.rong.push 为融云 push 进程名称，不可修改。
         */
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext())) ||
                "io.rong.push".equals(getCurProcessName(getApplicationContext()))) {


            //初始化chat
            Chat.init(this);
            //初始化融云
            RongIM.init(this);
            //注册接收消息监听器
            RongIM.setOnReceiveMessageListener(new EliteReceiveMessageListener());
            //注册自定义消息
            RongIM.registerMessageType(EliteMessage.class);
            //注册机器人消息
            RongIM.registerMessageType(RobotMessage.class);
            RongIM.registerMessageTemplate(new RobotMessageItemProvider());
            //注册卡片消息
            RongIM.registerMessageType(CardMessage.class);
            RongIM.registerMessageTemplate(new CardMessageItemProvider());
            //注册自定义用户信息提供者
            RongIM.setUserInfoProvider(new EliteUserInfoProvider(), true);

            //注册自定义扩展模块，先去除默认扩展，再注册自定义扩展
            List<IExtensionModule> extensionModules = RongExtensionManager.getInstance().getExtensionModules();
            if (extensionModules != null) {
                for (IExtensionModule extensionModule : extensionModules) {
                    RongExtensionManager.getInstance().unregisterExtensionModule(extensionModule);
                }
            }

            EliteExtensionModule extensionModule = new EliteExtensionModule();
            extensionModule.setEnableSight(true);// 加号按钮中，开启小视频功能
            extensionModule.setEnableCloseSession(true);// 加号按钮中，开启客户主动结束聊天功能
            RongExtensionManager.getInstance().registerExtensionModule(extensionModule);
        }

        //初始化百度map
        SDKInitializer.initialize(getApplicationContext());
    }

    public static String getCurProcessName(Context context) {

        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {

            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }
}
