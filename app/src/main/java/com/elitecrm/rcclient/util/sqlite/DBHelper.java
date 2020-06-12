package com.elitecrm.rcclient.util.sqlite;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    public static final String TAG = "DBHelper";
    public static final String DB_NAME = "database.db";

    public static final int DB_VERSION = 2;

    private static final String MESSAGE_CREATE_TABLE_SQL = "create table message ("
            + "id char(36) primary key,"
            + "target_id varchar(128) not null,"
            + "conversation_type integer not null,"
            + "object_name varchar(32) not null,"
            + "content varchar(4000) not null,"
            + "send_time long not null,"
            + "unsend_type integer default 0"
            + ");";


    public DBHelper(Context context) {
        // 传递数据库名与版本号给父类
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(MESSAGE_CREATE_TABLE_SQL);
            Log.d(TAG, "create table message: " + MESSAGE_CREATE_TABLE_SQL);
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS message");
            db.execSQL(MESSAGE_CREATE_TABLE_SQL);
            Log.d(TAG, "create table message: " + MESSAGE_CREATE_TABLE_SQL);
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
