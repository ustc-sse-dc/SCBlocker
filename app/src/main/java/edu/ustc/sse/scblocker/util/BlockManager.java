package edu.ustc.sse.scblocker.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ustc.sse.scblocker.BuildConfig;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.model.Rule;

/**
 * Created by dc on 000011/6/11.
 */
public class BlockManager {

    private static final String COUNTRY_CODES = ",376,971,93,355,374,599,244,672,54,43,61,297,994,387,880,32,226,359,973,257" +
            ",229,590,673,591,55,975,267,375,501,1,61,243,236,242,41,225,682,56,237,86" +
            ",57,506,53,238,61,357,420,49,253,45,213,593,372,20,291,34,251,358,679,500" +
            ",691,298,33,241,44,995,233,350,299,220,224,240,30,502,245,592,852,504,385,509" +
            ",36,62,353,972,44,91,964,98,39,962,81,254,996,855,686,269,850,82,965,7" +
            ",856,961,423,94,231,266,370,352,371,218,212,377,373,382,261,692,389,223,95,976" +
            ",853,222,356,230,960,265,52,60,258,264,687,227,234,505,31,47,977,674,683,64" +
            ",968,507,51,689,675,63,92,48,508,870,1,351,680,595,974,40,381,7,250,966" +
            ",677,248,249,46,65,290,386,421,232,378,221,252,597,239,503,963,268,235,228,66" +
            ",992,690,670,993,216,676,90,688,886,255,380,256,1,598,998,39,58,84,678,681" +
            ",685,967,262,27,260,263,";

    public static final int TYPE_ALL = 0;
    public static final int TYPE_SMS = 1;
    public static final int TYPE_CALL = 2;
    public static final int TYPE_EXCEPT = 3;

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.BlockProvider";

    private static final Uri URI_RULE_ALL = Uri.parse("content://" + AUTHORITY + "/rule");
    private static final Uri URI_BLOCKCONTENT_ALL = Uri.parse("content://" + AUTHORITY + "/blockcontent");


    private ContentResolver resolver;

    public BlockManager(Context context) {
        resolver = context.getContentResolver();
    }


    public List<Rule> getRules(int type) {
        List<Rule> list = new ArrayList<>();

        Cursor cursor = null;
        if (type == TYPE_ALL) {
            cursor = resolver.query(URI_RULE_ALL, new String[]{"_id", "content", "type", "sms", "call", "exception", "created", "remark"},
                    null, null, "created DESC");
        } else if (type == TYPE_SMS) {
            cursor = resolver.query(URI_RULE_ALL, new String[]{"_id", "content", "type", "sms", "call", "exception", "created", "remark"},
                    "sms = ?", new String[]{"1"}, "created DESC");
        } else if (type == TYPE_CALL) {
            cursor = resolver.query(URI_RULE_ALL, new String[]{"_id", "content", "type", "sms", "call", "exception", "created", "remark"},
                    "call = ?", new String[]{"1"}, "created DESC");
        } else if (type == TYPE_EXCEPT) {
            cursor = resolver.query(URI_RULE_ALL, new String[]{"_id", "content", "type", "sms", "call", "exception", "created", "remark"},
                    "exception = ?", new String[]{"1"}, "created DESC");
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
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
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    public long saveRule(Rule rule) {
        ContentValues values = new ContentValues();
        values.put("content", rule.getContent());
        values.put("type", rule.getType());
        values.put("sms", rule.getSms());
        values.put("call", rule.getCall());
        values.put("exception", rule.getException());
        values.put("remark", rule.getRemark());
        values.put("created", new Date().getTime());

        return ContentUris.parseId(resolver.insert(URI_RULE_ALL, values));
    }

    public long restoreRule(Rule rule) {
        return saveRule(rule);
    }

    public void updateRule(Rule rule) {
        ContentValues values = new ContentValues();
        values.put("content", rule.getContent());
        values.put("type", rule.getType());
        values.put("sms", rule.getSms());
        values.put("call", rule.getCall());
        values.put("exception", rule.getException());
        values.put("created", rule.getCreated());
        values.put("remark", rule.getRemark());

        resolver.update(ContentUris.withAppendedId(URI_RULE_ALL, rule.getId()), values, null, null);
    }

    public void deleteRule(Rule rule) {
        resolver.delete(ContentUris.withAppendedId(URI_RULE_ALL, rule.getId()), null, null);
    }

    /**
     * Query from database all the block contents where _id > id
     *
     * @param id
     * @return
     */
    public ArrayList<BlockContent> getContents(long id, int type) {
        ArrayList<BlockContent> result = new ArrayList<>();
        Cursor cursor = null;

        if (type == BlockContent.BLOCK_ALL) {
            cursor = resolver.query(
                    URI_BLOCKCONTENT_ALL, new String[]{"_id", "number", "type", "content", "created", "read"},
                    "_id > ?", new String[]{String.valueOf(id)}, "created DESC"
            );
        } else if (type == BlockContent.BLOCK_CALL) {
            cursor = resolver.query(
                    URI_BLOCKCONTENT_ALL, new String[]{"_id", "number", "type", "content", "created", "read"},
                    "_id > ? AND type = ?", new String[]{String.valueOf(id), String.valueOf(BlockContent.BLOCK_CALL)}, "created DESC"
            );
        } else if (type == BlockContent.BLOCK_SMS) {
            cursor = resolver.query(
                    URI_BLOCKCONTENT_ALL, new String[]{"_id", "number", "type", "content", "created", "read"},
                    "_id > ? AND type = ?", new String[]{String.valueOf(id), String.valueOf(BlockContent.BLOCK_SMS)}, "created DESC"
            );
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                BlockContent content = new BlockContent();
                content.setId(cursor.getLong(0));
                content.setNumber(cursor.getString(1));
                content.setType(cursor.getInt(2));
                content.setContent(cursor.getString(3));
                content.setCreated(cursor.getLong(4));
                content.setRead(cursor.getInt(5));

                result.add(content);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return result;
    }

    // content == null ??
    public long saveBlockContent(BlockContent content) {
        ContentValues values = new ContentValues();
        values.put("number", content.getNumber());
        values.put("type", content.getType()); // incoming call/sms
        values.put("content", content.getContent());
        values.put("created", content.getCreated());
        values.put("read", content.getRead());
        return ContentUris.parseId(resolver.insert(URI_BLOCKCONTENT_ALL, values));

    }

    public long restoreBlockContent(BlockContent content) {
        return saveBlockContent(content);
    }

    public void deleteBlockContent(BlockContent content) {
        resolver.delete(ContentUris.withAppendedId(URI_BLOCKCONTENT_ALL, content.getId()), null, null);
    }

    // 将数据库中的所有拦截数据设为已读
    public void readAllBlockContent() {
        ContentValues values = new ContentValues();
        values.put("read", BlockContent.READED);
        resolver.update(URI_BLOCKCONTENT_ALL, values, "read=?", new String[]{String.valueOf(BlockContent.UNREADED)});
    }

    public int getUnReadCount() {
        int count = 0;

        Cursor cursor = resolver.query(URI_BLOCKCONTENT_ALL, new String[]{"_id"},
                "read=?", new String[]{"0"}, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }


    public boolean blockSMS(String sender, String content) {
        Log.v(getClass().getSimpleName(), "From sender " + sender + " should block?");
        sender = trimCountryCode(sender);
        List<Rule> exceptions = getRules(TYPE_EXCEPT); //白名单
        if (exceptions != null && exceptions.size() > 0) {
            for (Rule exception : exceptions) {
                switch (exception.getType()) {
                    case Rule.TYPE_STRING:
                        if (sender.equals(exception.getContent())) {
                            return false;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        if (wildcardMatch(exception.getContent(), sender)) {
                            return false;
                        }
                        break;
                    case Rule.TYPE_KEYWORD:
                        if (content.contains(exception.getContent())) {
                            return false;
                        }
                        break;
                }
            }
        }

        List<Rule> rules = getRules(TYPE_SMS);
        if (rules != null && rules.size() > 0) {
            for (Rule rule : rules) {
                switch (rule.getType()) {
                    case Rule.TYPE_STRING:
                        if (sender.equals(rule.getContent())) {
                            return true;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        if (wildcardMatch(rule.getContent(), sender)) {
                            return true;
                        }
                        break;
                    case Rule.TYPE_KEYWORD:
                        if (content.contains(rule.getContent())) {
                            return true;
                        }
                        break;
                }
            }
        }
        return false;
    }

    public boolean blockCall(String caller) {
        caller = trimCountryCode(caller);
        List<Rule> exceptions = getRules(TYPE_EXCEPT);
        if (exceptions != null && exceptions.size() > 0) {
            for (Rule exception : exceptions) {
                switch (exception.getType()) {
                    case Rule.TYPE_STRING:
                        if (caller.equals(exception.getContent())) {
                            return false;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        if (wildcardMatch(exception.getContent(), caller)) {
                            return false;
                        }
                        break;
                }
            }
        }

        List<Rule> list = getRules(TYPE_CALL);
        if (list != null && list.size() > 0) {
            for (Rule rule : list) {
                switch (rule.getType()) {
                    case Rule.TYPE_STRING:
                        if (caller.equals(rule.getContent())) {
                            return true;
                        }
                        break;
                    case Rule.TYPE_WILDCARD:
                        if (wildcardMatch(rule.getContent(), caller)) {
                            return true;
                        }
                        break;
                }
            }
        }
        return false;
    }

    //TODO: figure it out
    private boolean wildcardMatch(String wildcard, String str) {
        if (wildcard == null || str == null)
            return false;

        boolean result = false;
        char c;
        boolean beforeStar = false;
        int back_i = 0;
        int back_j = 0;
        int i, j;
        for (i = 0, j = 0; i < str.length(); ) {
            if (wildcard.length() <= j) {
                if (back_i != 0) {
                    beforeStar = true;
                    i = back_i;
                    j = back_j;
                    back_i = 0;
                    back_j = 0;
                    continue;
                }
                break;
            }

            if ((c = wildcard.charAt(j)) == '*') {
                if (j == wildcard.length() - 1) {
                    result = true;
                    break;
                }
                beforeStar = true;
                j++;
                continue;
            }

            if (beforeStar) {
                if (str.charAt(i) == c) {
                    beforeStar = false;
                    back_i = i + 1;
                    back_j = j;
                    j++;
                }
            } else {
                if (c != '?' && c != str.charAt(i)) {
                    result = false;
                    if (back_i != 0) {
                        beforeStar = true;
                        i = back_i;
                        j = back_j;
                        back_i = 0;
                        back_j = 0;
                        continue;
                    }
                    break;
                }
                j++;
            }
            i++;
        }

        if (i == str.length() && j == wildcard.length())
            result = true;
        return result;
    }


    private String trimCountryCode(String phoneNumber) {
        if (phoneNumber.charAt(0) == '+') {
            if (COUNTRY_CODES.contains("," + phoneNumber.substring(1, 2) + ",")) {
                return phoneNumber.substring(2);
            } else if (COUNTRY_CODES.contains("," + phoneNumber.substring(1, 3) + ",")) {
                return phoneNumber.substring(3);
            } else if (COUNTRY_CODES.contains("," + phoneNumber.substring(1, 4) + ",")) {
                return phoneNumber.substring(4);
            }
        }
        return phoneNumber;
    }

}
