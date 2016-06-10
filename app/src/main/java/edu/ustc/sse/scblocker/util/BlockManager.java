package edu.ustc.sse.scblocker.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.model.Rule;

/**
 * Created by dc on 000011/6/11.
 */
public class BlockManager {

    public static final int TYPE_ALL = 0;
    public static final int TYPE_SMS = 1;
    public static final int TYPE_CALL = 2;
    public static final int TYPE_EXCEPT = 3;

    private DbHelper mDbHelper;
    private ContentResolver mResolver;

    public BlockManager(Context context){
        mDbHelper = new DbHelper(context);
        mResolver = context.getContentResolver();
    }

    public List<Rule> getRules(int type){
        List<Rule> list = new ArrayList<>();

        Cursor cursor = null;
        if (type == TYPE_ALL){
            cursor = mDbHelper.getReadableDatabase().query(DbHelper.TABLE_RULE, new String[]{"_id","content","type","sms","call","exception","created","remark"},
                    null, null, null, null, "created DESC");
        }else if (type == TYPE_SMS){
            cursor = mDbHelper.getReadableDatabase().query(DbHelper.TABLE_RULE, new String[]{"_id","content","type","sms", "call","exception","created","remark"},
                    "sms = ?", new String[]{"1"}, null,null, "created DESC");
        }else if (type == TYPE_CALL){
            cursor = mDbHelper.getReadableDatabase().query(DbHelper.TABLE_RULE, new String[]{"_id","content","type","sms", "call","exception","created","remark"},
                    "call = ?", new String[]{"1"}, null, null, "created DESC");
        }else if (type == TYPE_EXCEPT) {
            cursor = mDbHelper.getReadableDatabase().query(DbHelper.TABLE_RULE, new String[]{"_id","content","type","sms", "call","exception","created","remark"},
                    "exception = ?", new String[]{"1"}, null, null, "created DESC");
        }

        if (cursor != null && cursor.moveToFirst()){
            do{
                Rule rule = new Rule();
                rule.setId(cursor.getLong(0));
                rule.setContent(cursor.getString(1));
                rule.setType(cursor.getInt(2));
                rule.setSms(cursor.getInt(3));
                rule.setCall(cursor.getInt(4));
                rule.setException(cursor.getInt(5));
                rule.setCreated(cursor.getLong(6));
                rule.setRemark(cursor.getString(7));

                list.add(rule);
            }while (cursor.moveToNext());
        }

        if (cursor != null){
            cursor.close();
        }
        return list;
    }

    public void saveRule(Rule rule){
        ContentValues values = new ContentValues();
        values.put("content", rule.getContent());
        values.put("type", rule.getType());
        values.put("sms", rule.getSms());
        values.put("call", rule.getCall());
        values.put("exception", rule.getException());
        values.put("remark", rule.getRemark());

        mDbHelper.getWritableDatabase().insert(DbHelper.TABLE_RULE, null, values);
    }

    public void updateRule(Rule rule){
        ContentValues values = new ContentValues();
        values.put("content", rule.getContent());
        values.put("type", rule.getType());
        values.put("sms", rule.getSms());
        values.put("call", rule.getCall());
        values.put("exception", rule.getException());
        values.put("created", rule.getCreated());
        values.put("remark", rule.getRemark());

        mDbHelper.getWritableDatabase().update(DbHelper.TABLE_RULE, values, "_id = ?", new String[]{String.valueOf(rule.getId())});
    }

    public void deleteRule(Rule rule){
        mDbHelper.getWritableDatabase().delete(DbHelper.TABLE_RULE, "_id = ?", new String[]{String.valueOf(rule.getId())});
    }

    /**
     * Query from database all the block contents where _id > id
     * @param id
     * @return
     */
    public List<BlockContent> getContents(long id){
        List<BlockContent> result =new ArrayList<>();
        Cursor cursor = mDbHelper.getReadableDatabase().query(
                DbHelper.TABLE_BLOCKCONTENT, new String[]{"_id","number","type","content","created","read"},
                "_id > ?", new String[]{String.valueOf(id)}, null, null, "created DESC"
        );
        if (cursor != null && cursor.moveToFirst()){
            do {
                BlockContent content = new BlockContent();
                content.setId(cursor.getLong(0));
                content.setNumber(cursor.getString(1));
                content.setType(cursor.getInt(2));
                content.setContent(cursor.getString(3));
                content.setCreated(cursor.getInt(4));
                content.setRead(cursor.getInt(5));

                result.add(content);
            }while(cursor.moveToNext());
        }

        if (cursor != null){
            cursor.close();
        }

        return result;
    }

    public long saveBlockContent(BlockContent content){
        ContentValues values = new ContentValues();
        values.put("number", content.getNumber());
        values.put("type", content.getType());
        values.put("content", content.getContent());
        values.put("created", content.getCreated());
        values.put("read", content.getRead());

        return mDbHelper.getWritableDatabase().insert(DbHelper.TABLE_BLOCKCONTENT, null, values);

    }

    public long restoreBlockContent(BlockContent content){
        return saveBlockContent(content);
    }

    public int deleteBlockContent(BlockContent content){
        return mDbHelper.getWritableDatabase().delete(DbHelper.TABLE_BLOCKCONTENT, "created=?",
                new String[]{String.valueOf(content.getCreated())});
    }

    // 将数据库中的所有拦截数据设为已读
    public void readAllBlockContent(){
        ContentValues values = new ContentValues();
        values.put("read", BlockContent.READED);
        mDbHelper.getWritableDatabase().update(DbHelper.TABLE_BLOCKCONTENT, values, "read=?",
                new String[]{"0"});
    }

    public boolean blockSMS(String sender, String content){
        List<Rule> exceptions = getRules(TYPE_EXCEPT);
        if (exceptions != null && exceptions.size() > 0){
            for (Rule exception : exceptions){
                switch (exception.getType()){
                    case Rule.TYPE_STRING:
                        if (sender.equals(exception.getContent())){
                            return false;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        //TODO: SMS excepted wildcard manipulating
                        return false;
                    case Rule.TYPE_KEYWORD:
                        //TODO: SMS excepted keyword manipulating
                        return false;
                }
            }
        }

        List<Rule> rules = getRules(TYPE_SMS);
        if (rules != null && rules.size() > 0){
            for (Rule rule : rules){
                switch (rule.getType()){
                    case Rule.TYPE_STRING:
                        if (sender.equals(rule.getContent())){
                            return true;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        //TODO: SMS wildcard manipulating
                        return false;
                    case Rule.TYPE_KEYWORD:
                        //TODO: SMS keyword manipulating
                        return false;
                }
            }
        }

        return false;
    }

    public boolean blockCall(String caller){
        List<Rule> exceptions = getRules(TYPE_EXCEPT);
        if (exceptions != null && exceptions.size() > 0){
            for (Rule exception : exceptions){
                switch (exception.getType()){
                    case Rule.TYPE_STRING:
                        if (caller.equals(exception.getContent())){
                            return false;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        //TODO: Excepted Wildcard manipulating
                        return false;
                }
            }
        }

        List<Rule> list = getRules(TYPE_CALL);
        if (list != null && list.size() > 0){
            for (Rule rule : list){
                switch (rule.getType()){
                    case Rule.TYPE_STRING:
                        if (caller.equals(rule.getContent())){
                            return true;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        //TODO: Wildcard manipulating
                        return false;
                }
            }
        }


        return false;
    }

}
