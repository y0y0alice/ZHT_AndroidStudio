package com.skyland.zht;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "skyland.db";

    public DBHelper(Context context) {
        super(context, getMyDatabaseName(context), null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //数据表，用于存放密码，工作流待办箱等信息
//        String sql = "Create Table LocalStorage ([key] nvarchar(8000),[value] text)";
//        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "DROP TABLE IF EXISTS LocalStorage";
        sqLiteDatabase.execSQL(sql);
        onCreate(sqLiteDatabase);
    }

    private static String  getMyDatabaseName(Context context) {
        File outerPath = Environment.getExternalStorageDirectory();
        String databasename = DB_NAME;
        boolean isSdcardEnable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {//SDCard是否插入
            isSdcardEnable = true;
        }
        String dbPath = null;
        if (isSdcardEnable) {
            try {
                dbPath = outerPath.getCanonicalPath() + "/ZHT/database/";
                //  dbPath = outerPath.getCanonicalPath() + "/ZHT/download";
//            dbPath =Environment.getExternalStorageDirectory().getPath() +"/database/";
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {//未插入SDCard，建在内存中
            dbPath = context.getFilesDir().getPath() + "/database/";
        }
        File dbp = new File(dbPath);
        boolean iscreate =true;
        if (!dbp.exists()) {
            iscreate =  dbp.mkdirs();
        }
        databasename = dbPath + DB_NAME;
        return databasename;
    }
}
