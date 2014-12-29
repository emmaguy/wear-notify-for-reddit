package com.emmaguy.todayilearned;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class Logger {
    public static final String LOG_EVENT_SYNC_SUBREDDITS = "SyncSubreddits";
    public static final String LOG_EVENT_UPDATE_INTERVAL = "UpdateInterval";
    public static final String LOG_EVENT_SAVE_TO_POCKET = "SaveToPocket";
    public static final String LOG_EVENT_REPLY_TO_POST = "ReplyToPost";
    public static final String LOG_EVENT_SEND_DM = "SendDM";
    public static final String LOG_EVENT_LOGIN = "Login";

    public static final String LOG_EVENT_SUCCESS = "Success";
    public static final String LOG_EVENT_FAILURE = "Failure";

    private static Tracker mTracker;

    public static void Log(String message) {
        if (BuildConfig.DEBUG) {
            Log.d("RedditWear", message);
        }
    }

    private static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void sendThrowable(Context c, String message, Throwable t) {
        if (BuildConfig.DEBUG) {
            Log.e("RedditWear", message, t);
        } else {
            String description = "Exception: " + new StandardExceptionParser(c, null).getDescription(Thread.currentThread().getName(), t);

            if (t instanceof RetrofitError) {
                RetrofitError retrofitError = (RetrofitError) t;
                description += " " + retrofitError.getKind();
            }

            getTracker(c)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(description)
                            .setAction(message + " msg: " + t.getMessage())
                            .setLabel("Connected to network: " + String.valueOf(isConnectedToNetwork(c)))
                            .build());
        }
    }

    public static void sendEvent(Context c, String action, String label) {
        if (BuildConfig.DEBUG) {
            Log("Sending event: " + action + " " + label);
        } else {
            getTracker(c)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory("RedditWear")
                            .setAction(action)
                            .setLabel(label)
                            .build());
        }
    }

    private static synchronized Tracker getTracker(Context c) {
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(c).newTracker(R.xml.google_analytics);
            if (BuildConfig.DEBUG) {
                GoogleAnalytics.getInstance(c).getLogger().setLogLevel(com.google.android.gms.analytics.Logger.LogLevel.VERBOSE);
            }
        }
        return mTracker;
    }
}
