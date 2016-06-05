package com.emmaguy.todayilearned;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

public class App extends Application {
    private final boolean mIsDebug = true;//BuildConfig.DEBUG;

    private AppComponent mAppComponent;

    public static App with(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        mAppComponent.inject(this);

        if (mIsDebug) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new FirebaseTree());
        }
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public boolean isDebug() {
        return mIsDebug;
    }
}
