package com.emmaguy.todayilearned;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;

import javax.inject.Inject;
import javax.inject.Named;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class App extends Application {
    private final boolean mIsDebug = BuildConfig.DEBUG;

    private AppComponent mAppComponent;

    @Inject @Named("analytics") UniqueIdentifierStorage mStorage;

    public static App with(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        mAppComponent.inject(this);

        Fabric.with(this, new Crashlytics(), new Answers());
        Crashlytics.setString("Build time", BuildConfig.BUILD_TIME);
        Crashlytics.setString("Git SHA", BuildConfig.GIT_SHA);
        Crashlytics.setUserIdentifier(mStorage.getUniqueIdentifier());

        if (mIsDebug) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashlyticsTree());
        }
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public void sendEvent(final String eventName, final String label) {
        if (mIsDebug) {
            Timber.d("Sending event: " + eventName + " " + label);
        } else {
            Answers.getInstance()
                    .logCustom(new CustomEvent(eventName)
                            .putCustomAttribute("event", label));
        }
    }

    public boolean isDebug() {
        return mIsDebug;
    }
}
