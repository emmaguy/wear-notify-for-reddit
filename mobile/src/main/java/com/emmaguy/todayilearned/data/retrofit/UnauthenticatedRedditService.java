package com.emmaguy.todayilearned.data.retrofit;

import com.emmaguy.todayilearned.sharedlib.Constants;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.Converter;

/**
 * Created by emma on 14/06/15.
 */
public class UnauthenticatedRedditService {
    public RedditService getRedditService(Converter converter, RequestInterceptor requestInterceptor) {
        RedditService redditService = new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_SSL_REDDIT)
                .setConverter(converter)
                .setRequestInterceptor(requestInterceptor)
                .build()
                .create(RedditService.class);

        return redditService;
    }
}
