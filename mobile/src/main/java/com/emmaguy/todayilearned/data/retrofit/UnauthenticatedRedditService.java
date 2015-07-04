package com.emmaguy.todayilearned.data.retrofit;

import com.emmaguy.todayilearned.sharedlib.Constants;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.Converter;

/**
 * Created by emma on 14/06/15.
 */
public class UnauthenticatedRedditService {
    public RedditService getRedditService(Converter converter, RequestInterceptor requestInterceptor) {
        final RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_SSL_REDDIT)
                .setConverter(converter);

        if (requestInterceptor != null) {
            builder.setRequestInterceptor(requestInterceptor);
        }

        return builder.build().create(RedditService.class);
    }
}
