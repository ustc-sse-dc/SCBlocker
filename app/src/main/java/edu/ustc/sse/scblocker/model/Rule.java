package edu.ustc.sse.scblocker.model;

import java.io.Serializable;

/** 拦截规则模型
 * Created by dc on 000011/6/11.
 */
public class Rule implements Serializable{

    public static final int TYPE_STRING   = 0;
    public static final int TYPE_KEYWORD  = 1;


    public static final int BLOCK_BOTH = 0;
    public static final int BLOCK_SMS  = 1;
    public static final int BLOCK_CALL = 2;

    private long id;
    private String content;         // 要拦截的号码、关键字、
    private int type;               // phone number? keyword for sms?
                                    // block sms? call? or both?
                                    // sms call block?
    private int sms;                //  1   1    0(both)
    private int call;               //  1   0    1(sms)
                                    //  0   1    2(call)
    private int exception;          // 白名单
    private long created;           // 规则建立时间
    private String remark;          // 规则的描述


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public int getSms() {
        return sms;
    }

    public void setSms(int sms) {
        this.sms = sms;
    }

    public int getCall() {
        return call;
    }

    public void setCall(int call) {
        this.call = call;
    }

    public int getException() {
        return exception;
    }

    public void setException(int exception) {
        this.exception = exception;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
