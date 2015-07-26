package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.refresh.BasicAuthorisationRequestInterceptorBuilder;
import com.emmaguy.todayilearned.storage.TokenStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;

@Module
public class SettingsModule {
    @Provides
    @Singleton
    public WearableActions provideWearableActions(Context context, Resources resources, TokenStorage tokenStorage) {
        return new WearableActions(context, resources, tokenStorage);
    }

    @Provides
    @Singleton
    public ActionStorage provideActionStorage(WearableActions wearableActions, SharedPreferences sharedPreferences, Resources resources, Context context) {
        return new WearableActionStorage(wearableActions, sharedPreferences, resources, context);
    }
}
