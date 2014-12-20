package com.emmaguy.todayilearned;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

public class Logger {
    private static Tracker mTracker;

    public static void Log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d("RedditWear", message);
        }
    }

    public static void Log(Context c, String message, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e("RedditWear", message, t);
        } else {
            getTracker(c)
                    .send(new HitBuilders.ExceptionBuilder()
                            .setDescription(new StandardExceptionParser(c, null).getDescription(Thread.currentThread().getName(), t))
                            .setFatal(false)
                            .build());
        }
    }

    private static synchronized Tracker getTracker(Context c) {
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(c).newTracker(R.xml.google_analytics);
            if(BuildConfig.DEBUG) {
                GoogleAnalytics.getInstance(c).getLogger().setLogLevel(com.google.android.gms.analytics.Logger.LogLevel.VERBOSE);
            }
        }
        return mTracker;
    }

    public static void sendEvent(Context c, String action, String label) {
        Log("Sending event: " + action + " " + label);

        getTracker(c)
                .send(new HitBuilders.EventBuilder()
                        .setCategory("RedditWear")
                        .setAction(action)
                        .setLabel(label)
                        .build());
    }
}
