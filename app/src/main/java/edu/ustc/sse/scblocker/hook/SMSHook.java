package edu.ustc.sse.scblocker.hook;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import edu.ustc.sse.scblocker.XposedMod;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.util.BlockManager;
import edu.ustc.sse.scblocker.util.Logger;
import edu.ustc.sse.scblocker.util.SettingsHelper;


/**
 * Created by dc on 000005/6/5.
 */
public class SMSHook {
    private static final String TAG = "SMSHook";

    private BlockManager mBlockerManager;
    private Context mContext;
    private SettingsHelper mSettingsHelper;

    private SparseArray<String[]> smsArrays = new SparseArray<>();

    public SMSHook() {
        mSettingsHelper = new SettingsHelper();
    }


    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        Logger.log("Hook com.android.internal.telephony.RIL...");
        Class<?> clazz = XposedHelpers.findClass("com.android.internal.telephony.RIL", null);
        XposedBridge.hookAllConstructors(clazz, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                mContext = (Context) param.args[0];
                mBlockerManager = new BlockManager(mContext);
            }
        });

        XposedBridge.hookAllMethods(clazz, "processUnsolicited", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!mSettingsHelper.isEnable() || !mSettingsHelper.isEnableSMS()){
                    return ;
                }
                Parcel p = (Parcel) param.args[0];

                int position = p.dataPosition();
                int response = p.readInt();

                switch (response) {
                    case 1003:    // RILConstants.RIL_UNSOL_RESPONSE_NEW_SMS
                        try {
                            boolean received = true;

                            String a[] = new String[2];
                            // 这个param.thisObject就是挂钩的对象：com.android.internal.telephony.RIL
                            a[1] = (String) XposedHelpers.callMethod(param.thisObject, "responseString", p);

                            final SmsMessage sms = (SmsMessage) XposedHelpers.callStaticMethod(SmsMessage.class,
                                    "newFromCMT", (Object) a);
                            final String sender = sms.getOriginatingAddress();
                            String content = sms.getMessageBody();

                            Object smsHeader = XposedHelpers.callMethod(XposedHelpers.getObjectField(sms,
                                    "mWrappedSmsMessage"), "getUserDataHeader");
                            if (smsHeader != null) {
                                Object concatRef = XposedHelpers.getObjectField(smsHeader, "concatRef");
                                if (concatRef == null) {
                                    return;
                                }

                                int refNumber = XposedHelpers.getIntField(concatRef, "refNumber");
                                int seqNumber = XposedHelpers.getIntField(concatRef, "seqNumber");
                                int msgCount = XposedHelpers.getIntField(concatRef, "msgCount");

                                String[] smsArray = smsArrays.get(refNumber);
                                if (smsArray == null) {
                                    smsArray = new String[msgCount];
                                    smsArrays.put(refNumber, smsArray);
                                }
                                smsArray[seqNumber - 1] = content;

                                if (isFullFilled(smsArray)) {
                                    content = TextUtils.join("", smsArray);
                                    smsArrays.remove(refNumber);
                                } else {
                                    received = false;
                                }
                            }
                            if (received) {
                                Logger.log("New SMS: " + sender );
                                //判断是否需要拦截该短信
                                if (mBlockerManager.blockSMS(sender,content)) {
                                    try {
                                        XposedHelpers.callMethod(param.thisObject, "acknowledgeLastIncomingGsmSms",
                                                true, 0, null);
                                    } catch (Throwable t) {
                                        XposedHelpers.callMethod(param.thisObject, "acknowledgeLastIncomingCdmaSms",
                                                true, 0, null);
                                    }
                                    param.setResult(null);

                                    //  后续处理
                                    Intent intent = new Intent(XposedMod.FILTER_NOTIFY_BLOCKED);
                                    intent.putExtra("type", BlockContent.BLOCK_SMS);
                                    intent.putExtra("number", sender);
                                    intent.putExtra("content", content);

                                    mContext.sendBroadcast(intent);
                                }
                            }
                        } catch (Throwable t) {
                            Log.e(TAG, "Error in blocking SMS", t);
                        }
                        break;
                }
                p.setDataPosition(position);
            }
        });

    }



    private boolean isFullFilled(String[] smss) {
        for (String sms : smss) {
            if (sms == null) {
                return false;
            }
        }
        return true;
    }


}
