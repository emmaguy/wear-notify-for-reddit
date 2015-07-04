package com.emmaguy.todayilearned.data.retrofit;

import com.emmaguy.todayilearned.data.auth.TokenRefreshInterceptor;
import com.emmaguy.todayilearned.data.storage.TokenStorage;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.Converter;

/**
 * Created by emma on 14/06/15.
 */
public class AuthenticatedRedditService {
    private final RequestInterceptor mRequestInterceptor;
    private final TokenStorage mTokenStorage;
    private final Converter mTokenConverter;

    public AuthenticatedRedditService(TokenStorage tokenStorage, RequestInterceptor interceptor, Converter tokenConverter) {
        mRequestInterceptor = interceptor;
        mTokenStorage = tokenStorage;
        mTokenConverter = tokenConverter;
    }

    public RedditService getRedditService(Converter converter) {
        OkHttpClient okHttpClient = new OkHttpClient();
        final RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_OAUTH_REDDIT);

        if (converter != null) {
            builder.setConverter(converter);
        }

        RedditService redditService = builder
                .setClient(new OkClient(okHttpClient))
                .build()
                .create(RedditService.class);

        okHttpClient.networkInterceptors().add(new TokenRefreshInterceptor(mTokenStorage, getTokenRefreshRedditService()));
        okHttpClient.setRetryOnConnectionFailure(true);
        return redditService;
    }

    private RedditService getTokenRefreshRedditService() {
        OkHttpClient okHttpClient = new OkHttpClient();
        final RestAdapter.Builder builder = new RestAdapter.Builder().setEndpoint(Constants.ENDPOINT_URL_SSL_REDDIT);

        return builder
                .setClient(new OkClient(okHttpClient))
                .setRequestInterceptor(mRequestInterceptor)
                .setConverter(mTokenConverter)
                .build()
                .create(RedditService.class);

    }
}
