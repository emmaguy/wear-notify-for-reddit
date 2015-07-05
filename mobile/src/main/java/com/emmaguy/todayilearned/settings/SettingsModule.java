package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.refresh.BasicAuthorisationRequestInterceptor;
import com.emmaguy.todayilearned.storage.TokenStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;

@Module
public class SettingsModule {
    @Provides
    @Singleton
    public Encoder provideEncoder() {
        return new Base64Encoder();
    }

    @Provides
    @Singleton
    public RequestInterceptor provideBasicAuthorisationRequestInterceptor(Resources resources, Encoder encoder) {
        final String credentials = resources.getString(R.string.client_id) + ":";
        return new BasicAuthorisationRequestInterceptor(encoder).build(credentials);
    }

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
