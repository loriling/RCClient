package com.elitecrm.rcclient.util;

/**
 * Created by ThinkPad on 2017/2/4.
 */

public class Constants {
    public static final String LOG_TAG = "EliteTag";
    public static final String CHAT_TARGET_ID = "EliteCRM";
    public static final String CHAT_TITLE = "在线客服";

    public static final String SET_TEXT_TYPING_TITLE = "对方正在输入";
    public static final String SET_VOICE_TYPING_TITLE = "对方正在讲话";

    public interface RequestStatus {
        int WAITING = 0;
        int ACCEPTED = 1;
        int REFUSED = 2;
        int TIMEOUT = 3;
        int DROPPED = 4;
        int NO_AGENT_ONLINE = 5;
        int OFF_HOUR = 6;
        int CANCELED_BY_CLIENT = 7;
        int ENTERPRISE_WECHAT_ACCEPTED = 11;
    }
    public interface Result {
        int SUCCESS = 1;
        int REQUEST_ALREADY_IN_ROULTING = -1;
        int ALREADY_IN_CHATTING = -2;
        int NOT_IN_WORKTIME = -3;
        int INVAILD_SKILLGROUP = -4;
        int NO_AGENT_ONLINE = -5;
        int INVAILD_CLIENT_ID = -6;
        int INVAILD_QUEUE_ID = -7;
        int REQUEST_ERROR = -8;
        int INVAILD_TO_USER_ID = -9;
        int INVAILD_CHAT_REQUEST_ID = -10;
        int INVAILD_CHAT_SESSION_ID = -11;
        int INVAILD_MESSAGE_TYPE = -12;
        int UPLOAD_FILE_FAILED = -13;
        int INVAILD_PARAMETER = -14;
        int INVAILD_TOKEN = -15;
        int INVAILD_FILE_EXTENSION = -16;
        int EMPTY_MESSAGE = -17;
        int INVAILD_SESSION_TYPE = -18;
        int INVAILD_LOGINNAME_OR_PASSWORD = -20;
        int INVAILD_SIGN = -30;
        int INTERNAL_ERROR = -100;
    }

    public interface RequestType {
        //发送和接收通用消息
        int LOGON = 1;
        int LOGOUT = 2;
        int REGISTER = 3;

        //客户发送 并且有接受回执
        int SEND_CHAT_REQUEST = 101;//发出聊天请求
        int CANCEL_CHAT_REQUEST = 102;//取消聊天请求
        int CLOSE_SESSION = 103;//结束聊天
        int RATE_SESSION = 104;//满意度评价
        int SEND_CHAT_MESSAGE = 110;//发送聊天消息
        int SEND_PRE_CHAT_MESSAGE = 111;//发送预消息（还没排完队时候的消息）

        //客户接受
        int CHAT_REQUEST_STATUS_UPDATE = 201;//聊天排队状态更新
        int CHAT_STARTED = 202;//通知客户端可以开始聊天
        int AGENT_PUSH_RATING = 203;//坐席推送了满意度
        int AGENT_UPDATED = 204;//坐席人员变更
        int AGENT_CLOSE_SESSION = 205;//坐席关闭
        int AGENT_SEND_MESSAGE = 210;//收到聊天消息
    }

    //1:文本, 2:图片, 3:文件, 4:位置, 5:语音 目前有这五种类型
    public interface MessageType {
        int TEXT = 1;
        int IMG = 2;
        int FILE = 3;
        int LOCATION = 4;
        int VOICE = 5;
        //TODO
        int VIDEO = 6;
        int SYSTEM_NOTICE = 99;
    }

    public interface NoticeMessageType {
        int NORMAL = 0;
        int TRACK_CHANGE = 1;
        int PUSH_RATING = 2;
        int AFK_ELAPSED_NOTIFY = 3;
        int AFK_ELAPSED_CLOSE_SESSION = 4;
        int TYPING = 5;
        int INVITE_NOTICE = 10;
        int TRANSFER_NOTICE = 11;
    }
    public interface ObjectName {
        String TXT_MSG = "RC:TxtMsg";
        String INFO_NTF = "RC:InfoNtf";
        String PROFILE_NTF = "RC:ProfileNtf";
        String CS_HS = "RC:CsHs";
        String VC_MSG = "RC:VcMsg";
        String LBS_MSG = "RC:LBSMsg";
        String IMG_MSG = "RC:ImgMsg";
        String FILE_MSG = "RC:FileMsg";
        String ELITE_MSG = "E:Msg";
    }

}
