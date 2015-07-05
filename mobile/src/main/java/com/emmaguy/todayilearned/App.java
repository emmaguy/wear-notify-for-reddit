package com.emmaguy.todayilearned;

import android.app.Application;
import android.content.Context;

/**
 * Main application
 */
public class App extends Application {
    private AppComponent mAppComponent;

    public static App with(Context context) {
        return (App) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
