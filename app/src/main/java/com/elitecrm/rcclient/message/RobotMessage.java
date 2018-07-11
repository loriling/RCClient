package com.elitecrm.rcclient.message;

import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Loriling on 2018/7/11.
 */

@MessageTag(value = "E:RobMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class RobotMessage extends MessageContent {
    private String message;//消息属性，可随意定义
    protected String extra;

    public RobotMessage(){

    }

    public RobotMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {

        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if(jsonObj.has("message")) {
                this.setMessage(jsonObj.optString("message"));
            }

            if(jsonObj.has("extra")) {
                this.setExtra(jsonObj.optString("extra"));
            }

            if(jsonObj.has("user")) {
                this.setUserInfo(this.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }

        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            if(!TextUtils.isEmpty(this.getMessage())) {
                jsonObj.put("message", this.getMessage());
            }

            if(!TextUtils.isEmpty(this.getExtra())) {
                jsonObj.put("extra", this.getExtra());
            }

            if(this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    //给消息赋值。
    public RobotMessage(Parcel in) {
        this.setMessage(ParcelUtils.readFromParcel(in));
        this.setExtra(ParcelUtils.readFromParcel(in));
        this.setUserInfo((UserInfo)ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<RobotMessage> CREATOR = new Creator<RobotMessage>() {

        @Override
        public RobotMessage createFromParcel(Parcel source) {
            return new RobotMessage(source);
        }

        @Override
        public RobotMessage[] newArray(int size) {
            return new RobotMessage[size];
        }
    };

    /**
     * 描述了包含在 Parcelable 对象排列信息中的特殊对象的类型。
     *
     * @return 一个标志位，表明Parcelable对象特殊对象类型集合的排列。
     */
    public int describeContents() {
        return 0;
    }

    /**
     * 将类的数据写入外部提供的 Parcel 中。
     *
     * @param dest  对象被写入的 Parcel。
     * @param flags 对象如何被写入的附加标志。
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.getMessage());//该类为工具类，对消息中属性进行序列化
        ParcelUtils.writeToParcel(dest, this.getExtra());
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
    }

    public static RobotMessage obtain(String message) {
        RobotMessage model = new RobotMessage();
        model.setMessage(message);
        return model;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
