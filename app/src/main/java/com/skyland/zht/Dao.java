package com.skyland.zht;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

public class Dao {
    private static final String TAG = "Dao";
    private Context context;
    private DBHelper ordersDBHelper;

    public Dao(Context context) {
        this.context = context;
        ordersDBHelper = new DBHelper(context);
    }

    public boolean initSysTable() {
        SQLiteDatabase db = null;
        db = ordersDBHelper.getReadableDatabase();
        boolean isExist = ordersDBHelper.tableExist(db, "LocalStorage");
        if (!isExist) {
            //数据表，用于存放密码，工作流待办箱等信息
            String sql = "Create Table LocalStorage ([key] text,[value] text)";
            db.execSQL(sql);
        }
        if (db != null) {
            db.close();
        }
        return false;
    }

    public boolean SaveToLacal(String key, String value) {
        SQLiteDatabase db = null;
        try {
            db = ordersDBHelper.getWritableDatabase();
            db.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("value", value);
            db.insertOrThrow("LocalStorage", null, contentValues);

            db.setTransactionSuccessful();
            return true;
        }catch (SQLiteConstraintException e){
            Toast.makeText(context, "主键重复", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Log.e(TAG, "", e);
        }finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }

    public boolean executeSql(String Sql) {
        SQLiteDatabase db = null;
        db = ordersDBHelper.getReadableDatabase();
        db.beginTransaction();
        db.execSQL(Sql);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        return true;
    }
}
