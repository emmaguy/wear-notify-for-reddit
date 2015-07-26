package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.refresh.AuthenticatedRedditService;
import com.emmaguy.todayilearned.refresh.BackgroundAlarmListener;
import com.emmaguy.todayilearned.refresh.BasicAuthorisationRequestInterceptorBuilder;
import com.emmaguy.todayilearned.refresh.DelegatingConverter;
import com.emmaguy.todayilearned.refresh.ImageDownloader;
import com.emmaguy.todayilearned.refresh.LatestPostsRetriever;
import com.emmaguy.todayilearned.refresh.MarkAsReadConverter;
import com.emmaguy.todayilearned.refresh.PostConverter;
import com.emmaguy.todayilearned.refresh.RedditService;
import com.emmaguy.todayilearned.refresh.TokenConverter;
import com.emmaguy.todayilearned.settings.Base64Encoder;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.gson.Gson;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
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
            UserStorage storage, @Named("unauthenticated")
    RedditService unauthenticatedRedditService, AuthenticatedRedditService authenticatedRedditService,
            @Named("posts") Converter postsConverter, @Named("markread") Converter markAsReadConverter) {
        return new LatestPostsRetriever(
                downloader,
                tokenStorage,
                storage,
                unauthenticatedRedditService,
                authenticatedRedditService,
                postsConverter,
                markAsReadConverter);
    }

    @Provides
    @Singleton
    @Named("unauthenticated")
    public RedditService provideUnauthenticatedRedditService(Resources resources, UserStorage userStorage) {
        final String credentials = resources.getString(R.string.client_id) + ":";
        final Gson gson = new Gson();
        final GsonConverter gsonConverter = new GsonConverter(gson);

        return new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_SSL_REDDIT)
                .setConverter(new DelegatingConverter(gsonConverter,
                        new TokenConverter(gsonConverter),
                        new PostConverter(gson, gsonConverter, resources, userStorage),
                        new MarkAsReadConverter()))
                .setRequestInterceptor(new BasicAuthorisationRequestInterceptorBuilder(new Base64Encoder()).build(credentials))
                .build()
                .create(RedditService.class);
    }

    @Provides
    @Singleton
    public AuthenticatedRedditService provideAuthenticatedRedditService(TokenStorage tokenStorage, RequestInterceptor requestInterceptor,
            @Named("token") Converter tokenConverter) {
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

    @Provides
    @Singleton
    public BackgroundAlarmListener provideAlarmListener() {
        return new BackgroundAlarmListener();
    }
}
