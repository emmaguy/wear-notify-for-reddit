package com.emmaguy.todayilearned.sharedlib;

import android.util.Log;

public class Logger {

    public static void Log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d("RedditWear", message);
        }
    }

    public static void Log(String message, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e("RedditWear", message, t);
        }
    }
}
