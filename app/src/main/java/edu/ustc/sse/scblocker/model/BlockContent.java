package edu.ustc.sse.scblocker.model;

/**
 * 拦截的短信/电话模型
 * Created by dc on 000011/6/11.
 */
public class BlockContent {

    //用来区分拦截的具体内容
    public static final int BLOCK_CALL = 0;
    public static final int BLOCK_SMS  = 1;

    public static final int UNREADED = 0;
    public static final int READED   = 1;


    private long id;
    private String number;      // 拦截的号码
    private int type;           // 拦截的类型
    private String content;     // 拦截的内容
    private long created;       // 拦截的时间
    private int read;           //

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(long id) {
        this.id = id;

    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }
}
