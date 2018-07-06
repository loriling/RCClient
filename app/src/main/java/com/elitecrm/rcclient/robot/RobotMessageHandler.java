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
    public static String handleMessage(String content, int robotType) {
        if (robotType == Constants.RobotEngine.ROBOT_TYPE_XIAODUO) {
            try {
                JSONObject contentJSON = new JSONObject(content);
                int errorCode = contentJSON.getInt("error_code");
                if (errorCode == 0) {//success
//                    {
//                        "error_code": 0,
//                        "info": "",
//                        "lp": "ec_0",
//                        "data": {
//                            "msg_id": 32483988,
//                            "user_id": "16e81433-b4ba-4cd2-945b-3aee250147dc",
//                            "question": "无法识别",
//                            "answers": [],
//                            "options": [],
//                            "recommend": [],
//                            "related_questions": [],
//                            "hot_questions": [],
//                            "inspects": [],
//                            "state": 3
//                        }
//                    }
                    JSONObject dataJSON = contentJSON.getJSONObject("data");
                    int state = dataJSON.getInt("state");
                    if (state == 1) {//state=1时表示问题成功识别。answers中为所识别问题的答案列表，recommend为空，hot_questions为空，related_questions可能非空（若改问题配置了关联问题）
                        JSONArray answersJSON = dataJSON.getJSONArray("answers");
                        if (answersJSON != null && answersJSON.length() > 0) {
                            content = answersJSON.get(0).toString();
                        }
                    } else if (state == 2) {//state=2时表示问题无法进准识别。可推荐相识问题。answers为空，hot_questions为空，recommend可能非空
                        JSONArray recommendJSON = dataJSON.getJSONArray("recommend");
                        if (recommendJSON != null && recommendJSON.length() > 0) {
                            content = recommendJSON.get(0).toString();
                        }
                    } else if (state == 3) {//state=3时表示问题无法识别。answers为空，recommend可能非空，hot_questions可能非空（如果配置了渠道热门问题则非空）
                        content = "问题无法识别\n【转人工】";
                    }

                } else if (errorCode == 1) {
                    content = "其他错误";
                } else if (errorCode == 100025) {
                    content = "系统繁忙";
                } else if (errorCode == 100026) {
                    content = "salt不合法";
                } else if (errorCode == 100027) {
                    content = "签名错误";
                } else if (errorCode == 100028) {
                    content = "参数错误";
                } else if (errorCode == 100029) {
                    content = "channel_id或者unit_id错误";
                } else if (errorCode == 100030) {
                    content = "question长度不能超过1000个字符";
                }
            } catch (Exception e) {
                content = "内部错误: " + e.getMessage();
                e.printStackTrace();
            }
        }

        return content;
    }
}
