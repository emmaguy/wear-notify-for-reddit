package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.emmaguy.todayilearned.data.LatestPostsFromRedditRetriever;
import com.emmaguy.todayilearned.data.auth.BasicAuthorisationRequestInterceptor;
import com.emmaguy.todayilearned.data.auth.RedditAccessTokenRequester;
import com.emmaguy.todayilearned.data.auth.RedditRequestTokenUriParser;
import com.emmaguy.todayilearned.data.converter.TokenConverter;
import com.emmaguy.todayilearned.data.encoder.Base64Encoder;
import com.emmaguy.todayilearned.data.encoder.Encoder;
import com.emmaguy.todayilearned.data.retrofit.AuthenticatedRedditService;
import com.emmaguy.todayilearned.data.retrofit.UnauthenticatedRedditService;
import com.emmaguy.todayilearned.data.storage.SharedPreferencesTokenStorage;
import com.emmaguy.todayilearned.data.storage.SharedPreferencesUniqueIdentifierStorage;
import com.emmaguy.todayilearned.data.storage.SharedPreferencesUserStorage;
import com.emmaguy.todayilearned.data.storage.TokenStorage;
import com.emmaguy.todayilearned.data.storage.UniqueIdentifierStorage;
import com.emmaguy.todayilearned.data.storage.UserStorage;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

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
    public Encoder provideEncoder() {
        return new Base64Encoder();
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Provides
    @Singleton
    public Converter provideTokenConverter() {
        return new TokenConverter(new GsonConverter(new GsonBuilder().create()));
    }

    @Provides
    @Singleton
    public UniqueIdentifierStorage provideUniqueIdentifierStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesUniqueIdentifierStorage(preferences, resources);
    }

    @Provides
    @Singleton
    public UserStorage provideUserStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesUserStorage(preferences, resources);
    }

    @Provides
    @Singleton
    public TokenStorage provideTokenStorage(SharedPreferences preferences, Resources resources) {
        return new SharedPreferencesTokenStorage(preferences, resources);
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

    @Provides
    @Singleton
    public AuthenticatedRedditService provideAuthenticatedRedditService(TokenStorage storage, RequestInterceptor requestInterceptor, Converter tokenConverter) {
        return new AuthenticatedRedditService(storage, requestInterceptor, tokenConverter);
    }

    @Provides
    @Singleton
    public RequestInterceptor provideBasicAuthorisationRequestInterceptor(Resources resources, Encoder encoder) {
        final String credentials = resources.getString(R.string.client_id) + ":";
        return new BasicAuthorisationRequestInterceptor(encoder).build(credentials);
    }

    @Provides
    @Singleton
    public RedditAccessTokenRequester provideRedditAccessTokenRequester(Context context, Resources resources, UniqueIdentifierStorage uniqueIdentifierStorage) {
        return new RedditAccessTokenRequester(context, resources, uniqueIdentifierStorage);
    }

    @Provides
    @Singleton
    public RedditRequestTokenUriParser provideRedditRequestTokenUriParser(Resources resources, TokenStorage tokenStorage, UniqueIdentifierStorage uniqueIdentifierStorage) {
        return new RedditRequestTokenUriParser(resources, tokenStorage, uniqueIdentifierStorage);
    }
}
