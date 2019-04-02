package com.elitecrm.rcclient.util.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.elitecrm.rcclient.entity.MessageSO;
import com.elitecrm.rcclient.util.MessageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.rong.imlib.model.Message;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public void addMessage(Message message) {
        MessageSO messageSO = MessageUtils.marshal(message);
        db.execSQL("INSERT INTO message VALUES(?,?,?,?,?,?)", new Object[]{UUID.randomUUID(), messageSO.getTargetId(), messageSO.getConversationType(), messageSO.getObjectName(), messageSO.getContent(), System.currentTimeMillis()});
    }

    public List<Message> queryMessages() {
        List<Message> messageList = new ArrayList<>();
        Cursor cursor = db.rawQuery("select target_id, conversation_type, object_name, content  from message order by send_time", null);
        while (cursor.moveToNext()) {
            String targetId = cursor.getString(0);
            int conversationType = cursor.getInt(1);
            String objectName = cursor.getString(2);
            String content = cursor.getString(3);
            Message message = MessageUtils.unmarshal(targetId, conversationType, objectName, content);
            if (message != null) {
                messageList.add(message);
            }
        }
        cursor.close();
        db.execSQL("delete from message");
        return messageList;
    }
}
