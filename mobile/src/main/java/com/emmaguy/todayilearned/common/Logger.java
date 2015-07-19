package com.emmaguy.todayilearned.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.emmaguy.todayilearned.BuildConfig;
import com.emmaguy.todayilearned.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import retrofit.RetrofitError;

public class Logger {
    public static final String LOG_EVENT_SYNC_SUBREDDITS = "SyncSubreddits";
    public static final String LOG_EVENT_UPDATE_INTERVAL = "UpdateInterval";
    public static final String LOG_EVENT_SAVE_TO_POCKET = "SaveToPocket";
    public static final String LOG_EVENT_REPLY_TO_POST = "ReplyToPost";
    public static final String LOG_EVENT_SEND_DM = "SendDM";
    public static final String LOG_EVENT_LOGIN = "Login";
    public static final String LOG_EVENT_VOTE_UP = "Upvote";
    public static final String LOG_EVENT_VOTE_DOWN = "Downvote";
    public static final String LOG_EVENT_GET_COMMENTS = "GetComments";
    public static final String LOG_EVENT_CUSTOMISE_ACTIONS = "CustomiseActions";
    public static final String LOG_EVENT_SORT_ORDER = "SortOrder";
    public static final String LOG_EVENT_OPEN_ON_PHONE_DISMISSES = "OpenOnPhoneDismisses";
    public static final String LOG_EVENT_HIGH_RES_IMAGE = "HighResImage";

    public static final String LOG_EVENT_SUCCESS = "Success";
    public static final String LOG_EVENT_FAILURE = "Failure";

    private static Tracker mTracker;

    public static void log(Context context, String message) {
        if (Utils.sIsDebug) {
            Log.d("RedditWear", message);
        } else {
            Log.d("RedditWear", message);
            getTracker(context)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory("DEBUG" + BuildConfig.VERSION_NAME)
                            .setAction(message)
                            .build());
        }
    }

    private static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void sendThrowable(Context c, String message, Throwable t) {
        final String category = (t instanceof RetrofitError) ? "retrofit, " + ((RetrofitError) t).getKind() : "other";
        final String action = message + ", msg: " + t.getMessage();
        final String label = "connected to network: " + isConnectedToNetwork(c) + "\nstack trace: " + Log.getStackTraceString(t);

        if (Utils.sIsDebug) {
            Log.e("RedditWear", "category: " + category + "\naction: " + action + "\nlabel: " + label, t);
        } else {
            getTracker(c)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(category)
                            .setAction(action)
                            .setLabel(label)
                            .build());
        }
    }

    public static void sendEvent(Context c, String action, String label) {
        if (Utils.sIsDebug) {
            log(c, "Sending event: " + action + " " + label);
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
            if (Utils.sIsDebug) {
                GoogleAnalytics.getInstance(c).getLogger().setLogLevel(com.google.android.gms.analytics.Logger.LogLevel.VERBOSE);
            }
        }
        return mTracker;
    }
}
