package com.elitecrm.rcclient.robot;

import com.elitecrm.rcclient.util.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Loriling on 2018/7/3.
 *
 * 100025	系统繁忙
 100026	salt不合法
 100027	签名错误
 100028	参数错误
 100029	channel_id或者unit_id错误
 100030	question长度不能超过1000个字符
 */

public class RobotMessageHandler {
    private static final String AUTO_ZRG = "AUTO_ZRG";
    private static final String ZRG = "CLIENT_ZRG";

    public static String handleMessage(String content, int robotType) {
        String robotContent = "";
        if (robotType == Constants.RobotEngine.ROBOT_TYPE_XIAODUO) {
            try {
                JSONObject contentJSON = new JSONObject(content);
                String type = contentJSON.getString("type");
                if ("error".equals(type)) {
                    if (contentJSON.has("message")) {
                        robotContent = contentJSON.getString("message");
                    }
                }
                else if ("text".equals(type)) {
                    if (contentJSON.has("content")) {
                        robotContent = contentJSON.getString("content");
                    }
                    if (contentJSON.has("relatedQuestions")) {
                        JSONArray relatedQuestions = contentJSON.getJSONArray("relatedQuestions");
                        if (relatedQuestions.length() > 0) {
                            for (int i = 0; i < relatedQuestions.length(); i++) {
                                JSONObject relatedQuestion = relatedQuestions.getJSONObject(i);
                                if (relatedQuestion.has("relates")) {
                                    JSONArray relates = relatedQuestion.getJSONArray("relates");
                                    if (relates.length() > 0) {
                                        if (relatedQuestion.has("title")) {
                                            robotContent += relatedQuestion.getString("title");
                                        }
                                        for (int j=0; j < relates.length(); j++) {
                                            JSONObject relate = relates.getJSONObject(j);
                                            robotContent += "\n" + (j + 1) + "【" + relate.getString("name") + "】";
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else if ("image".equals(type)) { //TODO 解析图片
                    JSONObject imageJSON = contentJSON.getJSONObject("content");

                }
                else if ("image-text".equals(type)) { //TODO 解析图片和文字混合的形式
                    JSONObject imageTextJSON = contentJSON.getJSONObject("content");

                }
                else if ("news".equals(type)) { //TODO 解析图文
                    JSONObject newsJSON = contentJSON.getJSONObject("content");

                }
                else if ("command".equals(type)) {  //TODO 解析命令
                    JSONObject commandJSON = contentJSON.getJSONObject("content");
                    if (contentJSON.has("extra")) {
                        robotContent = contentJSON.getString("extra");
                    }

                    if (ZRG.equals(commandJSON.getString("code"))) {
                        if (null != content && !"".equals(content)) {
                            robotContent += "\n【转人工】";
                        } else {
                            robotContent += "【转人工】";
                        }
                    }
                } else {
                    content = "未定义的消息";
                }
            } catch (Exception e) {
                robotContent = content;
            }
        } else {
            robotContent = content;
        }

        return robotContent;
    }
}
