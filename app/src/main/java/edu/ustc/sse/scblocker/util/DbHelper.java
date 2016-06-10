package edu.ustc.sse.scblocker.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dc on 000011/6/11.
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "blocker.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_BLOCKCONTENT = "content";
    public static final String TABLE_RULE         = "rule";

    public DbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BLOCKCONTENT +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, number TEXT, type INTEGER, content TEXT, created INTEGER, read INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RULE +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, content TEXT, type INTEGER, sms INTEGER, call INTEGER, exception  INTEGER, created INTEGER, remark TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion){
            case 1:

            case 2:
        }

    }
}
