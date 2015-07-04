package com.emmaguy.todayilearned;

import android.app.Application;
import android.content.Context;

/**
 * Created by emma on 04/07/15.
 */
public class App extends Application {
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    public static App with(Context context) {
        return (App) context.getApplicationContext();
    }
}
