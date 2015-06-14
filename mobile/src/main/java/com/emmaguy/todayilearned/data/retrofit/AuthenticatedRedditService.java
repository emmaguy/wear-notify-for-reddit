package com.emmaguy.todayilearned.data.retrofit;

import com.emmaguy.todayilearned.data.auth.TokenRefreshInterceptor;
import com.emmaguy.todayilearned.data.storage.TokenStorage;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;

/**
 * Created by emma on 14/06/15.
 */
public class AuthenticatedRedditService {
    private final TokenStorage mTokenStorage;

    public AuthenticatedRedditService(TokenStorage tokenStorage) {
        mTokenStorage = tokenStorage;
    }

    public RedditService getRedditService(Converter converter) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final RestAdapter.Builder builder = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Constants.ENDPOINT_URL_OAUTH_REDDIT);

        if (converter != null) {
            builder.setConverter(converter);
        }

        RedditService redditService = builder
                .setClient(new OkClient(okHttpClient))
                .build()
                .create(RedditService.class);

        okHttpClient.networkInterceptors().add(new TokenRefreshInterceptor(mTokenStorage, redditService));
        okHttpClient.setRetryOnConnectionFailure(true);
        return redditService;
    }
}
