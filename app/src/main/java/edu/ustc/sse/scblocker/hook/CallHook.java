package edu.ustc.sse.scblocker.hook;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import edu.ustc.sse.scblocker.XposedMod;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.util.BlockManager;


/**
 * Created by dc on 000005/6/5.
 */
public class CallHook {

    private static final String TAG = "CallHook";

    private final boolean isLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    private final String className = isLollipop ? "com.android.services.telephony.PstnIncomingCallNotifier"
            : "com.android.phone.CallNotifier";
    private final String methodName = isLollipop ? "handleNewRingingConnection" : "onNewRingingConnection";

    private Context mContext;
    private BlockManager mBlockerManager;



    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam){

        final Class<?> clazz = XposedHelpers.findClass(className, loadPackageParam.classLoader);

        XposedBridge.hookAllConstructors(clazz, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mContext = (Context)XposedHelpers.callMethod(
                        isLollipop ? XposedHelpers.getObjectField(param.thisObject, "mPhoneBase") : param.args[1],
                        "getContext"
                );
                mBlockerManager = new BlockManager(mContext);
            }
        });


        XposedBridge.hookAllMethods(clazz, methodName, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                try{
                    Object connection = XposedHelpers.getObjectField(param.args[0], "result");
                    final Object call = XposedHelpers.callMethod(connection, "getCall");
                    final String caller = (String)XposedHelpers.callMethod(connection, "getAddress");

                    // 判断是否需要拦截
                    if (mBlockerManager.blockCall(caller)) {
                        XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.phone.PhoneUtils",
                                loadPackageParam.classLoader), "hangupRingingCall", call);

                        param.setResult(null);

                        // 发送给本应用一个信息广播
                        Intent intent = new Intent(XposedMod.FILTER_NOTIFY_BLOCKED);
                        intent.putExtra("type", BlockContent.BLOCK_CALL);
                        intent.putExtra("number", caller);
                        mContext.sendBroadcast(intent);
                    }
                }catch (Throwable t){
                    Log.e(TAG, "Error in blocking incoming call ", t);
                }

            }
        });


    }
}
