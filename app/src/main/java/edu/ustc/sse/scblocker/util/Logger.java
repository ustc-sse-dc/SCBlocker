package edu.ustc.sse.scblocker.util;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;
import edu.ustc.sse.scblocker.BuildConfig;

public class Logger {
    private static boolean DEBUG = BuildConfig.DEBUG;

    public static void log(String msg) {
        if (DEBUG) {
            try {
                XposedBridge.log("[--SCBlocker--] " + msg);
            } catch (Throwable t) {
                Log.i("[--SCBlocker--]", msg);
            }
        }
    }

    public static void log(Throwable t) {
        if (DEBUG) {
            try {
                XposedBridge.log(t);
            } catch (Throwable t1) {
                Log.i("[--SCBlocker--]", "", t);
            }
        }
    }
}
