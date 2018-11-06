# RCClient
WebChat与融云Android SDK app集成
详细文档：[https://loriling.github.io/EliteCRM/webchat-sdk-guide.html](https://loriling.github.io/EliteCRM/webchat-sdk-guide.html "https://loriling.github.io/EliteCRM/webchat-sdk-guide.html")

融云版本最新升级到了2.9.1

此项目是一个完整的Android APP示例，clone下来后，按融云的文档，下载IMLib和IMKit
1. 在IMLib的AndroidManifest.xml中配置融云APP_KEY
2. 如果需要地图功能，在自己App的AndroidManifest.xml中配置相关地图的API_KEY
3. 是否开启地图，是否开启小视频等功能，都早App.java中配置
4. 在自己的Activity中，调用EliteChat.initAndStart方法，来启动聊天界面，具体方法说明到EliteChat.java中查看。