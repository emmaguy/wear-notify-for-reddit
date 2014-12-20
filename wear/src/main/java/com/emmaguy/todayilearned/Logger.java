package com.emmaguy.todayilearned;

import android.util.Log;

public class Logger {
    public static void Log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d("RedditWear", message);
        }
    }
}
