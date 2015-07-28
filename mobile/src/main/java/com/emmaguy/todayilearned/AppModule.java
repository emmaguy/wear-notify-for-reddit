package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.refresh.BackgroundAlarmListener;
import com.emmaguy.todayilearned.refresh.BasicAuthorisationRequestInterceptorBuilder;
import com.emmaguy.todayilearned.refresh.CommentsConverter;
import com.emmaguy.todayilearned.refresh.DelegatingConverter;
import com.emmaguy.todayilearned.refresh.ImageDownloader;
import com.emmaguy.todayilearned.refresh.LatestPostsRetriever;
import com.emmaguy.todayilearned.refresh.MarkAsReadConverter;
import com.emmaguy.todayilearned.refresh.PostConverter;
import com.emmaguy.todayilearned.refresh.RedditService;
import com.emmaguy.todayilearned.refresh.SubscriptionConverter;
import com.emmaguy.todayilearned.refresh.TokenConverter;
import com.emmaguy.todayilearned.refresh.TokenRefreshInterceptor;
import com.emmaguy.todayilearned.settings.Base64Encoder;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
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
    public LatestPostsRetriever provideLatestPostsFromRedditRetriever(ImageDownloader downloader,
            TokenStorage tokenStorage,
            UserStorage storage,
            @Named("unauthenticated") RedditService unauthenticatedRedditService,
            @Named("authenticated") RedditService authenticatedRedditService) {
        return new LatestPostsRetriever(
                downloader,
                tokenStorage,
                storage,
                unauthenticatedRedditService,
                authenticatedRedditService
        );
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    @Named("unauthenticated")
    public RedditService provideUnauthenticatedRedditService(Gson gson, Resources resources, UserStorage userStorage) {
        final String credentials = resources.getString(R.string.client_id) + ":";
        final GsonConverter gsonConverter = new GsonConverter(gson);

        return new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_SSL_REDDIT)
                .setConverter(new DelegatingConverter(gsonConverter,
                        new TokenConverter(gsonConverter),
                        new PostConverter(gson, gsonConverter, resources, userStorage),
                        new MarkAsReadConverter(),
                        new SubscriptionConverter(),
                        new CommentsConverter(gson, gsonConverter, resources, userStorage)))
                .setRequestInterceptor(new BasicAuthorisationRequestInterceptorBuilder(new Base64Encoder()).build(credentials))
                .build()
                .create(RedditService.class);
    }

    @Provides
    @Singleton
    @Named("authenticated")
    public RedditService provideAuthenticatedRedditService(Gson gson,
            @Named("unauthenticated") RedditService redditService, Resources resources,
            UserStorage userStorage, TokenStorage tokenStorage) {
        final OkHttpClient okHttpClient = new OkHttpClient();
        final GsonConverter gsonConverter = new GsonConverter(gson);

        RedditService authenticatedRedditService = new RestAdapter.Builder().setEndpoint(Constants.ENDPOINT_URL_OAUTH_REDDIT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new DelegatingConverter(gsonConverter,
                        new TokenConverter(gsonConverter),
                        new PostConverter(gson, gsonConverter, resources, userStorage),
                        new MarkAsReadConverter(),
                        new SubscriptionConverter(),
                        new CommentsConverter(gson, gsonConverter, resources, userStorage)))
                .build()
                .create(RedditService.class);

        okHttpClient.networkInterceptors().add(new TokenRefreshInterceptor(tokenStorage, redditService));
        okHttpClient.setRetryOnConnectionFailure(true);
        return authenticatedRedditService;
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
