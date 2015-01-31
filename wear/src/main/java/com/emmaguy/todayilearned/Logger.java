package com.emmaguy.todayilearned;

import android.util.Log;

public class Logger {
    private static boolean sIsDebug = BuildConfig.DEBUG;

    public static void Log(String message) {
        if (sIsDebug) {
            Log.d("RedditWear", message);
        }
    }

    public static void Log(String message, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e("RedditWear", message, t);
        }
    }
}
