package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.refresh.LatestPostsFromRedditRetriever;
import com.emmaguy.todayilearned.refresh.UnauthenticatedRedditService;
import com.emmaguy.todayilearned.storage.UserStorage;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Context mContext;

    public AppModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return mContext;
    }

    @Provides
    @Singleton
    public Resources provideResources() {
        return mContext.getResources();
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Provides
    @Singleton
    public LatestPostsFromRedditRetriever provideLatestPostsFromRedditRetriever(UserStorage storage) {
        return new LatestPostsFromRedditRetriever(mContext, storage);
    }

    @Provides
    @Singleton
    public UnauthenticatedRedditService provideUnauthenticatedRedditService() {
        return new UnauthenticatedRedditService();
    }
}
