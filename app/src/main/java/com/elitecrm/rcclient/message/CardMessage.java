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

@MessageTag(value = "E:CardMsg", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED)
public class CardMessage extends MessageContent {
    private String title;
    private String imageUri;
    private String url;
    private String price;
    private String from;

    protected String extra;

    public CardMessage(){

    }

    public CardMessage(byte[] data) {
        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {

        }

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            if(jsonObj.has("title")) {
                this.setTitle(jsonObj.optString("title"));
            }
            if(jsonObj.has("imageUri")) {
                this.setImageUri(jsonObj.optString("imageUri"));
            }
            if(jsonObj.has("url")) {
                this.setUrl(jsonObj.optString("url"));
            }
            if(jsonObj.has("price")) {
                this.setPrice(jsonObj.optString("price"));
            }
            if(jsonObj.has("from")) {
                this.setFrom(jsonObj.optString("from"));
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            if(!TextUtils.isEmpty(this.getTitle())) {
                jsonObj.put("title", this.getTitle());
            }
            if(!TextUtils.isEmpty(this.getImageUri())) {
                jsonObj.put("imageUri", this.getImageUri());
            }
            if(!TextUtils.isEmpty(this.getUrl())) {
                jsonObj.put("url", this.getUrl());
            }
            if(!TextUtils.isEmpty(this.getPrice())) {
                jsonObj.put("price", this.getPrice());
            }
            if(!TextUtils.isEmpty(this.getFrom())) {
                jsonObj.put("from", this.getFrom());
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
    public CardMessage(Parcel in) {
        this.setTitle(ParcelUtils.readFromParcel(in));
        this.setImageUri(ParcelUtils.readFromParcel(in));
        this.setUrl(ParcelUtils.readFromParcel(in));
        this.setPrice(ParcelUtils.readFromParcel(in));
        this.setFrom(ParcelUtils.readFromParcel(in));
        this.setExtra(ParcelUtils.readFromParcel(in));
        // this.setUserInfo(ParcelUtils.readFromParcel(in, UserInfo.class));
    }

    /**
     * 读取接口，目的是要从Parcel中构造一个实现了Parcelable的类的实例处理。
     */
    public static final Creator<CardMessage> CREATOR = new Creator<CardMessage>() {

        @Override
        public CardMessage createFromParcel(Parcel source) {
            return new CardMessage(source);
        }

        @Override
        public CardMessage[] newArray(int size) {
            return new CardMessage[size];
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
        ParcelUtils.writeToParcel(dest, this.getTitle());//该类为工具类，对消息中属性进行序列化
        ParcelUtils.writeToParcel(dest, this.getImageUri());
        ParcelUtils.writeToParcel(dest, this.getUrl());
        ParcelUtils.writeToParcel(dest, this.getPrice());
        ParcelUtils.writeToParcel(dest, this.getFrom());
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
    }

    /**
     * 获取价格的“元”的部分
     * @return
     */
    public String getPriceMain() {
        String price = this.getPrice();
        if (price != null && price.contains(".")) {
            return price.split("\\.")[0];
        }
        return price;
    }

    /**
     * 获取价格的“角分”的部分
     * @return
     */
    public String getPriceSub() {
        String price = this.getPrice();
        if (price != null && price.contains(".")) {
            return "." + price.split("\\.")[1];
        }
        return "";
    }
}
