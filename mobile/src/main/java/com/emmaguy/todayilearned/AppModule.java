package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.refresh.ImageDownloader;
import com.emmaguy.todayilearned.refresh.LatestPostsRetriever;
import com.emmaguy.todayilearned.refresh.UnauthenticatedRedditService;
import com.emmaguy.todayilearned.storage.UserStorage;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    public ImageDownloader provideImageDownloader() {
        return new ImageDownloader(mContext);
    }

    @Provides
    @Singleton
    public LatestPostsRetriever provideLatestPostsFromRedditRetriever(ImageDownloader downloader, UserStorage storage) {
        return new LatestPostsRetriever(downloader, storage);
    }

    @Provides
    @Singleton
    public UnauthenticatedRedditService provideUnauthenticatedRedditService() {
        return new UnauthenticatedRedditService();
    }

    @Provides
    @Singleton
    @Named("io")
    public Scheduler provideIo() {
        return Schedulers.io();
    }

    @Provides
    @Singleton
    @Named("ui")
    public Scheduler provideUi() {
        return AndroidSchedulers.mainThread();
    }
}
