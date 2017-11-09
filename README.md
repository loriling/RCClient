# RCClient
EliteWebChat与融云Android SDK app集成

此项目是一个完整的Android APP示例，clone下来后，按融云的文档，下载IMLib和IMKit（目前版本是2.8.14）
1. 在IMLib的AndroidManifest.xml中配置融云APP_KEY
2. 如果需要地图功能，在自己App的AndroidManifest.xml中配置相关地图的API_KEY
3. 在自己的Activity中，调用EliteChat.initAndStart方法，来启动聊天界面，具体方法说明到EliteChat.java中查看。