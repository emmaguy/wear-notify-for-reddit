package com.emmaguy.todayilearned;

import android.util.Log;

public class Logger {
    private static boolean sIsDebug = true;//BuildConfig.DEBUG;

    public static void log(String message) {
        if (sIsDebug) {
            Log.d("RedditWear", message);
        }
    }

    public static void log(String message, Throwable t) {
        if (sIsDebug) {
            Log.e("RedditWear", message, t);
        }
    }
}
