package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.refresh.BackgroundAlarmListener;
import com.emmaguy.todayilearned.refresh.BasicAuthorisationRequestInterceptorBuilder;
import com.emmaguy.todayilearned.refresh.CommentsConverter;
import com.emmaguy.todayilearned.refresh.DelegatingConverter;
import com.emmaguy.todayilearned.refresh.HtmlDecoder;
import com.emmaguy.todayilearned.refresh.ImageDownloader;
import com.emmaguy.todayilearned.refresh.LatestPostsRetriever;
import com.emmaguy.todayilearned.refresh.MarkAsReadConverter;
import com.emmaguy.todayilearned.refresh.PostConverter;
import com.emmaguy.todayilearned.refresh.RedditAuthenticationService;
import com.emmaguy.todayilearned.refresh.RedditService;
import com.emmaguy.todayilearned.refresh.SubscriptionConverter;
import com.emmaguy.todayilearned.refresh.TokenConverter;
import com.emmaguy.todayilearned.refresh.TokenRefreshInterceptor;
import com.emmaguy.todayilearned.refresh.UnreadDirectMessageRetriever;
import com.emmaguy.todayilearned.settings.Base64Encoder;
import com.emmaguy.todayilearned.settings.BrowserIntentBuilder;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.SharedPreferencesUniqueIdentifierStorage;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;
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
        return new ImageDownloader();
    }

    @Provides
    @Singleton
    public LatestPostsRetriever provideLatestPostsFromRedditRetriever(ImageDownloader downloader, UserStorage storage, RedditService redditService) {
        return new LatestPostsRetriever(downloader, storage, redditService);
    }

    @Provides
    @Singleton
    public UnreadDirectMessageRetriever provideUnreadDirectMessageRetriever(TokenStorage tokenStorage, UserStorage storage, RedditService redditService) {
        return new UnreadDirectMessageRetriever(tokenStorage, storage, redditService);
    }

    @Provides
    @Singleton
    public BrowserIntentBuilder provideBrowserIntentBuilder(Context context) {
        return new BrowserIntentBuilder(context.getPackageManager());
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public RedditAuthenticationService provideRedditAuthenticationService(Gson gson, Resources resources) {
        final GsonConverter gsonConverter = new GsonConverter(gson);
        final String credentials = resources.getString(R.string.client_id) + ":";
        return new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_SSL_REDDIT)
                .setConverter(new TokenConverter(gsonConverter))
                .setRequestInterceptor(new BasicAuthorisationRequestInterceptorBuilder(new Base64Encoder()).build(credentials))
                .build()
                .create(RedditAuthenticationService.class);
    }

    @Provides
    @Singleton
    public RedditService provideRedditService(Gson gson, Resources resources, UserStorage userStorage, TokenStorage tokenStorage, RedditAuthenticationService authService) {
        final GsonConverter gsonConverter = new GsonConverter(gson);

        final OkHttpClient okHttpClient = new OkHttpClient();

        RedditService authenticatedRedditService = new RestAdapter.Builder().setEndpoint(Constants.ENDPOINT_URL_OAUTH_REDDIT)
                .setClient(new OkClient(okHttpClient))
                .setConverter(new DelegatingConverter(gsonConverter,
                        new TokenConverter(gsonConverter),
                        new PostConverter(gsonConverter, resources, userStorage, new HtmlDecoder()),
                        new MarkAsReadConverter(),
                        new SubscriptionConverter(),
                        new CommentsConverter(gson, gsonConverter, resources, userStorage)))
                .build()
                .create(RedditService.class);

        okHttpClient.networkInterceptors().add(new TokenRefreshInterceptor(tokenStorage, authService));
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
    @Named("analytics")
    public UniqueIdentifierStorage provideAnalyticsUniqueIdentifierStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesUniqueIdentifierStorage(preferences, resources.getString(R.string.prefs_key_analytics_id));
    }

    @Provides
    @Singleton
    public BackgroundAlarmListener provideAlarmListener() {
        return new BackgroundAlarmListener();
    }
}
