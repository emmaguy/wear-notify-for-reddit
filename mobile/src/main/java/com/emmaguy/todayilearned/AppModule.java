package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.refresh.AuthenticatedRedditService;
import com.emmaguy.todayilearned.refresh.ImageDownloader;
import com.emmaguy.todayilearned.refresh.LatestPostsRetriever;
import com.emmaguy.todayilearned.refresh.UnauthenticatedRedditService;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
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
    public LatestPostsRetriever provideLatestPostsFromRedditRetriever(ImageDownloader downloader, TokenStorage tokenStorage,
            UserStorage storage, UnauthenticatedRedditService unauthenticatedRedditService, AuthenticatedRedditService authenticatedRedditService,
            @Named("posts") GsonConverter postsConverter, @Named("markread") Converter markAsReadConverter) {
        return new LatestPostsRetriever(downloader,
                tokenStorage,
                storage,
                unauthenticatedRedditService,
                authenticatedRedditService,
                postsConverter,
                markAsReadConverter);
    }

    @Provides
    @Singleton
    public UnauthenticatedRedditService provideUnauthenticatedRedditService() {
        return new UnauthenticatedRedditService();
    }

    @Provides
    @Singleton
    public AuthenticatedRedditService provideAuthenticatedRedditService(TokenStorage tokenStorage, RequestInterceptor requestInterceptor, @Named("token") Converter tokenConverter) {
        return new AuthenticatedRedditService(tokenStorage, requestInterceptor, tokenConverter);
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
