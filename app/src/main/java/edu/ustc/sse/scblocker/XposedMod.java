package edu.ustc.sse.scblocker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XModuleResources;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Date;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import edu.ustc.sse.scblocker.activity.MainActivity;
import edu.ustc.sse.scblocker.hook.CallHook;
import edu.ustc.sse.scblocker.hook.SMSHook;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.util.BlockManager;
import edu.ustc.sse.scblocker.util.SettingsHelper;

/**
 * Created by dc on 000011/6/11.
 */
public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources{

    public static final String MODULE_NAME = BuildConfig.APPLICATION_ID;
    public static final String FILTER_NOTIFY_BLOCKED = BuildConfig.APPLICATION_ID + "_NOTIFY_BLOCKED";

    private String MODULE_PATH;

    private NotificationManagerCompat notifManager;
    private NotificationCompat.Builder notiBuilder;

    private int smallNotificationIcon = -1;
    private String notificationContentText;

    private SettingsHelper mSettingsHelper;
    private BlockManager mBlockerManager;


    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mSettingsHelper = new SettingsHelper();
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
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resParam) throws Throwable {
        if (resParam.equals("android")){
            if (smallNotificationIcon != -1){
                return;
            }

            getNotificationIcon(resParam);
        }
    }
    private void getNotificationIcon(XC_InitPackageResources.InitPackageResourcesParam resParam) {
        XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resParam.res);
        smallNotificationIcon = resParam.res.addResource(modRes, R.drawable.ic_perm_phone_msg_white_24dp);
        notificationContentText = resParam.res.getString(resParam.res.addResource(modRes, R.string.notification_content_text));
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
                        notifManager = NotificationManagerCompat.from(mContext);
                        notiBuilder = new NotificationCompat.Builder(mContext).setContentTitle("SCBlocker").setTicker("SCBlocker").setAutoCancel(true);

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
            BlockContent content = new BlockContent();
            int type = intent.getIntExtra("type", -1);
            switch (type){ // incoming sms or incoming call
                case BlockContent.BLOCK_CALL:
                    content.setType(BlockContent.BLOCK_CALL);
                    content.setNumber(intent.getStringExtra("number"));
                    content.setContent("");
                    content.setCreated(new Date().getTime());
                    content.setRead(BlockContent.UNREADED);
                    break;
                case BlockContent.BLOCK_SMS:
                    content.setType(BlockContent.BLOCK_SMS);
                    content.setNumber(intent.getStringExtra("number"));
                    content.setContent(intent.getStringExtra("content"));
                    content.setCreated(new Date().getTime());
                    content.setRead(BlockContent.UNREADED);
                    break;
            }

            //Logger.log(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(content.getCreated())));
            mBlockerManager.saveBlockContent(content);

            if (mSettingsHelper.isShowBlockNotification()) {
                showNotification(context, type);
            }
        }


    }

    private void showNotification(Context context, int type) {
        if (smallNotificationIcon == -1 || notificationContentText == null) {
            return;
        }

        if (type != BlockContent.BLOCK_SMS && type != BlockContent.BLOCK_CALL) {
            return;
        }

        int unreadCount = mBlockerManager.getUnReadCount();

        if (unreadCount == 0) {
            return;
        }

        ComponentName componentName = new ComponentName(MODULE_NAME, MainActivity.class.getName());

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(componentName);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra("position", type == BlockManager.TYPE_SMS ? 2 : 3);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(componentName);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        notiBuilder.setSmallIcon(smallNotificationIcon)
                .setContentText(String.format(notificationContentText, unreadCount))
                .setContentIntent(pendingIntent);

        notifManager.notify(0, notiBuilder.build());
    }


}
