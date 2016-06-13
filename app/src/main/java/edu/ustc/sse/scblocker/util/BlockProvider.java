package edu.ustc.sse.scblocker.util;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

import edu.ustc.sse.scblocker.BuildConfig;

/**
 * Created by dc on 000013/6/13.
 */
public class BlockProvider extends ContentProvider {

    private static final String TABLE_RULE = "rule";
    private static final String TABLE_BLOCKCONTENT = "content";

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.BlockProvider";

    private static final int RULE_ALL = 0;
    private static final int RULE_ITEM = 1;

    private static final int BLOCKCONTENT_ALL = 2;
    private static final int BLOCKCONTENT_ITEM = 3;

    private static final String RULE_TYPE = "vnd.android.cursor.dir/vnd.hblocker.rule";
    private static final String RULE_ITEM_TYPE = "vnd.android.cursor.item/vnd.hblocker.rule";
    private static final String BLOCKCONTENT_TYPE = "vnd.android.cursor.dir/vnd.hblocker.blockcontent";
    private static final String BLOCKCONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.hblocker.blockcontent";

    private static final UriMatcher matcher;

    private ContentResolver resolver = null;
    private DbHelper mDbHelper;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY, "rule", RULE_ALL);
        matcher.addURI(AUTHORITY, "rule/#", RULE_ITEM);
        matcher.addURI(AUTHORITY, "blockcontent", BLOCKCONTENT_ALL);
        matcher.addURI(AUTHORITY, "blockcontent/#", BLOCKCONTENT_ITEM);

    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        if (context != null){
            resolver = context.getContentResolver();
        }
        mDbHelper = new DbHelper(context);

        return true;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (matcher.match(uri)){
            case RULE_ALL:
                return RULE_TYPE;
            case RULE_ITEM:
                return RULE_ITEM_TYPE;
            case BLOCKCONTENT_ALL:
                return BLOCKCONTENT_TYPE;
            case BLOCKCONTENT_ITEM:
                return BLOCKCONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Error URI: " + uri);
        }
    }


    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (matcher.match(uri)) {
            case RULE_ALL:
            case RULE_ITEM:
                return db.query(TABLE_RULE, projection, selection, selectionArgs, null, null, sortOrder);
            case BLOCKCONTENT_ALL:
            case BLOCKCONTENT_ITEM:
                return db.query(TABLE_BLOCKCONTENT, projection, selection, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Error URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long rowId;
        switch (matcher.match(uri)){
            case RULE_ALL:
                rowId = db.insert(TABLE_RULE, null, contentValues);
                break;
            case BLOCKCONTENT_ALL:
                rowId = db.insert(TABLE_BLOCKCONTENT, null, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Error URI:" + uri);
        }
        if (rowId < 0){
            throw new SQLiteException("Unable to insert " + contentValues + " for " + uri);
        }

        Uri newUri = ContentUris.withAppendedId(uri, rowId);
        resolver.notifyChange(newUri, null);

        return newUri;
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int count;
        switch (matcher.match(uri)){
            case RULE_ITEM:
                count = db.update(TABLE_RULE, contentValues, "_id=?",new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            case BLOCKCONTENT_ITEM:
                count = db.update(TABLE_BLOCKCONTENT, contentValues, "_id=?", new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            case RULE_ALL:
                count = db.update(TABLE_RULE, contentValues, null,null);
                break;
            case BLOCKCONTENT_ALL:
                count = db.update(TABLE_BLOCKCONTENT, contentValues, null, null);
                break;
            default:
                throw new IllegalArgumentException("Error URI: " + uri);
        }

        resolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int count;
        switch (matcher.match(uri)){
            case RULE_ITEM:
                count = db.delete(TABLE_RULE, "_id=?", new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            case BLOCKCONTENT_ITEM:
                count = db.delete(TABLE_BLOCKCONTENT, "_id=?", new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new IllegalArgumentException("Error URI: " + uri);
        }
        resolver.notifyChange(uri, null);

        return count;
    }


    class DbHelper extends SQLiteOpenHelper {

        public static final String DATABASE_NAME = "blocker.db";
        public static final int DATABASE_VERSION = 1;

        public DbHelper(Context context) {
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
            switch (oldVersion) {
                case 1:

                case 2:
            }

        }
    }
}
