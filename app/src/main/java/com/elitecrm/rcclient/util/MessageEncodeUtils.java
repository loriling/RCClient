package com.elitecrm.rcclient.util;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.common.RLog;
import io.rong.message.ImageMessage;

public class MessageEncodeUtils {
    public static byte[] encodeImageMessage(ImageMessage imageMessage) {
        JSONObject jsonObj = new JSONObject();

        try {
            if (!TextUtils.isEmpty(imageMessage.getBase64())) {
                jsonObj.put("content", imageMessage.getBase64());
            } else {
                RLog.d("ImageMessage", "缩略图为空，请检查构造图片消息的地址");
            }

            if (imageMessage.getMediaUrl() != null) {
                jsonObj.put("imageUri", imageMessage.getMediaUrl().toString());
            }
            if (imageMessage.getThumUri() != null) {
                jsonObj.put("thumUri", imageMessage.getThumUri().toString());
            }

            if (imageMessage.getLocalUri() != null) {
                jsonObj.put("localPath", imageMessage.getLocalUri().toString());
            }

            if (imageMessage.isUpLoadExp()) {
                jsonObj.put("exp", true);
            }

            jsonObj.put("isFull", imageMessage.isFull());
            if (!TextUtils.isEmpty(imageMessage.getExtra())) {
                jsonObj.put("extra", imageMessage.getExtra());
            }

            if (imageMessage.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", imageMessage.getJSONUserInfo());
            }

            jsonObj.put("isBurnAfterRead", imageMessage.isDestruct());
            jsonObj.put("burnDuration", imageMessage.getDestructTime());
        } catch (JSONException var3) {
            RLog.e("JSONException", var3.getMessage());
        }

        imageMessage.setBase64(null);
        return jsonObj.toString().getBytes();
    }

    public static ImageMessage decodeImageMessage(byte[] data) {
        ImageMessage imageMessage = new ImageMessage();
        String jsonStr = new String(data);

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("imageUri")) {
                String uri = jsonObj.optString("imageUri");
                if (!TextUtils.isEmpty(uri)) {
                    imageMessage.setRemoteUri(Uri.parse(uri));
                }
            }

            if (jsonObj.has("thumUri")) {
                imageMessage.setThumUri(Uri.parse(jsonObj.optString("thumUri")));
            }

            if (jsonObj.has("localPath")) {
                imageMessage.setLocalPath(Uri.parse(jsonObj.optString("localPath")));
            }

            if (jsonObj.has("content")) {
                imageMessage.setBase64(jsonObj.optString("content"));
            }

            if (jsonObj.has("extra")) {
                imageMessage.setExtra(jsonObj.optString("extra"));
            }

            if (jsonObj.has("exp")) {
                imageMessage.setUpLoadExp(true);
            }

            if (jsonObj.has("isFull")) {
                imageMessage.setIsFull(jsonObj.optBoolean("isFull"));
            } else if (jsonObj.has("full")) {
                imageMessage.setIsFull(jsonObj.optBoolean("full"));
            }

            if (jsonObj.has("user")) {
                imageMessage.setUserInfo(imageMessage.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }

            if (jsonObj.has("isBurnAfterRead")) {
                imageMessage.setDestruct(jsonObj.getBoolean("isBurnAfterRead"));
            }

            if (jsonObj.has("burnDuration")) {
                imageMessage.setDestructTime(jsonObj.getLong("burnDuration"));
            }
        } catch (JSONException var5) {
            Log.e("JSONException", var5.getMessage());
        }
        return imageMessage;
    }
}
