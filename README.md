# WebChat与融云Android SDK app集成

此项目是提供了webchat集成rongcloud的完整的Android APP示例，使用时候同步代码，其中App.java为相关功能配置，需要自己关注与修改，MainActivity是demo的主界面，使用时候不需要，换成自己的activity即可。其他文件都需要导入。
1. 在IMLib的AndroidManifest.xml中配置融云APP_KEY
2. 如果需要地图功能，在自己App的AndroidManifest.xml中配置相关地图的API_KEY
3. 是否开启地图，是否开启小视频等功能，都早App.java中配置
4. 在自己的Activity中，调用EliteChat.initAndStart方法，来启动聊天界面，具体方法说明到EliteChat.java中查看。

详细文档：<a href="https://loriling.github.io/EliteCRM/webchat-sdk-guide.html" target="_blank">点击这里</a>

融云版本最新升级到了2.9.1

### 2019.04.02
1. 未发送消息从内存中改存到了sqlite中，app关闭后重新打开时候依旧可以获取到未发消息了。
注意启动时候新增了初始化调用： Chat.init(context);

### 2018.12.05
1. 修复不同targetId入参时候，可能造成的消息错位问题。

### 2018.11.30
1. 增加了客户端主动接受聊天按钮，如果开启此功能，就可以从加号里看到结束聊天按钮。

### 2018.11.27
1. 修改了重新进入聊天界面逻辑，如果修改了队列号进入，则会先结束之前会话，重新排队新的队列。