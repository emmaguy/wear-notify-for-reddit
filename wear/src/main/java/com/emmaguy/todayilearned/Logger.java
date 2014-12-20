package com.emmaguy.todayilearned;

import android.util.Log;

public class Logger {
    public static void Log(String message) {
        if (com.emmaguy.todayilearned.sharedlib.BuildConfig.DEBUG) {
            Log.d("RedditWear", message);
        }
    }
}
