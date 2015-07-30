package com.emmaguy.todayilearned;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.UUID;

import retrofit.RetrofitError;
import timber.log.Timber;

import static com.google.android.gms.analytics.Logger.LogLevel;

public class App extends Application {
    private final boolean mIsDebug = BuildConfig.DEBUG;

    private AppComponent mAppComponent;

    private static GoogleAnalytics sGoogleAnalytics;
    private static Tracker sTracker;

    public static App with(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();

        sGoogleAnalytics = GoogleAnalytics.getInstance(this);
        sGoogleAnalytics.setLocalDispatchPeriod(1800);

        sTracker = sGoogleAnalytics.newTracker(getString(R.string.google_analytics_id));
        sTracker.enableExceptionReporting(true);
        sTracker.enableAdvertisingIdCollection(false);
        sTracker.enableAutoActivityTracking(true);

        if (mIsDebug) {
            Timber.plant(new Timber.DebugTree());
        } else {
            GoogleAnalytics.getInstance(this).getLogger().setLogLevel(LogLevel.VERBOSE);
            Timber.plant(new GoogleAnalyticsTree(sTracker));
        }
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public void sendEvent(String action, String label) {
        if (mIsDebug) {
            Timber.d("Sending event: " + action + " " + label);
        } else {
            sTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("RedditWear")
                    .setAction(action)
                    .setLabel(label)
                    .build());
        }
    }

    public boolean isDebug() {
        return mIsDebug;
    }

    private static class GoogleAnalyticsTree extends Timber.Tree {
        private Tracker mTracker;

        private GoogleAnalyticsTree(Tracker tracker) {
            mTracker = tracker;
        }

        @Override protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.ERROR) {
                final String errorKind = (t instanceof RetrofitError) ? "retrofit, " + ((RetrofitError) t).getKind() : "non-retrofit";
                final String info = message + ", msg: " + t.getMessage() + ", stack trace: " + Log.getStackTraceString(t);

                final String description = "error kind: " + errorKind + " info: " + info;

                mTracker.send(new HitBuilders.ExceptionBuilder().setDescription(description).build());
            } else {
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("DEBUG" + BuildConfig.VERSION_NAME)
                        .setAction(message)
                        .build());
            }
        }
    }
}
