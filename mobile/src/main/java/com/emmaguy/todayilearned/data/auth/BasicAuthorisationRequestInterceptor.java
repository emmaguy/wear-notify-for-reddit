package com.emmaguy.todayilearned.data.auth;

import com.emmaguy.todayilearned.data.encoder.Encoder;
import com.emmaguy.todayilearned.sharedlib.Constants;

import retrofit.RequestInterceptor;

/**
 * Created by emma on 14/06/15.
 */
public class BasicAuthorisationRequestInterceptor {
    private Encoder mEncoder;

    public BasicAuthorisationRequestInterceptor(Encoder encoder) {
        mEncoder = encoder;
    }

    public RequestInterceptor build(final String credentials) {
        return new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader(Constants.AUTHORIZATION, String.format("Basic %s", mEncoder.encode(credentials.getBytes())));
            }
        };
    }
}
