package com.skyland.zht;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Dao {
    private static final String TAG = "Dao";
    private Context context;
    private DBHelper ordersDBHelper;

    public Dao(Context context) {
        this.context = context;
        ordersDBHelper = new DBHelper(context);
    }

    public boolean initTable(){
        int count = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        db = ordersDBHelper.getReadableDatabase();
        db.execSQL("");
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        if (count > 0) return true;
        if (cursor != null) {
            cursor.close();
        }
        if (db != null) {
            db.close();
        }
        return false;
    }
}
