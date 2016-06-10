package edu.ustc.sse.scblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import java.util.Date;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import edu.ustc.sse.scblocker.hook.CallHook;
import edu.ustc.sse.scblocker.hook.SMSHook;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.util.BlockManager;

/**
 * Created by dc on 000011/6/11.
 */
public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources{

    public static final String MOUDLE_NAME = BuildConfig.APPLICATION_ID;
    public static final String FILTER_NOTIFY_BLOCKED = BuildConfig.APPLICATION_ID + "_NOTIFY_BLOCKED";


    private BlockManager mBlockerManager;


    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        new SMSHook().initZygote(startupParam);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.android.phone")){
            new CallHook().handleLoadPackage(loadPackageParam);
        }else if (loadPackageParam.packageName.equals("android")){
            initNotificationBroadcastReceiver(loadPackageParam);
        }
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam initPackageResourcesParam) throws Throwable {

    }

    private void initNotificationBroadcastReceiver(XC_LoadPackage.LoadPackageParam loadPackageParam){
        //TODO: What's the meaning of this block of code??
        XposedHelpers.findAndHookMethod("com.android.server.am.ActivityManagerService", loadPackageParam.classLoader, "systemReady", Runnable.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                final  Runnable origCallback = (Runnable)param.args[0];
                param.args[0] = new Runnable() {
                    @Override
                    public void run() {
                        if (origCallback != null) origCallback.run();

                        final Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
                        mBlockerManager = new BlockManager(mContext);

                        HandlerThread thread = new HandlerThread("SCBlocker");
                        thread.start();
                        final Handler mHandler = new Handler(thread.getLooper());
                        mContext.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(final Context context, final Intent intent) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        XposedMod.this.saveHistoryAndNotify(mContext, intent);
                                    }
                                });
                            }
                        }, new IntentFilter(FILTER_NOTIFY_BLOCKED));

                    }
                };

            }
        });
    }

    private void saveHistoryAndNotify(Context context, Intent intent){
        if (intent != null && intent.getExtras() != null){
            BlockContent content = null;
            Bundle bundle = intent.getExtras();
            int type = bundle.getInt("type");
            switch (type){
                case BlockContent.BLOCK_CALL:
                    content = new BlockContent();
                    content.setNumber(bundle.getString("number"));
                    content.setContent("");
                    content.setCreated(new Date().getTime());
                    content.setType(BlockContent.BLOCK_CALL);
                    content.setRead(BlockContent.UNREADED);
                    break;
                case BlockContent.BLOCK_SMS:
                    content = new BlockContent();
                    content.setType(BlockContent.BLOCK_SMS);
                    content.setNumber(bundle.getString("number"));
                    content.setContent(bundle.getString("content"));
                    content.setCreated(bundle.getLong("created"));
                    content.setRead(BlockContent.UNREADED);
                    break;
            }

            mBlockerManager.saveBlockContent(content);
        }
    }


}
