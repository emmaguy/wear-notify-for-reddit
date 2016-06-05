package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.settings.Encoder;
import com.emmaguy.todayilearned.sharedlib.Constants;

import retrofit.RequestInterceptor;

/**
 * Created by emma on 14/06/15.
 */
public class BasicAuthorisationRequestInterceptorBuilder {
    private Encoder mEncoder;

    public BasicAuthorisationRequestInterceptorBuilder(Encoder encoder) {
        mEncoder = encoder;
    }

    public RequestInterceptor build(final String credentials) {
        return new RequestInterceptor() {
            @Override public void intercept(RequestInterceptor.RequestFacade request) {
                request.addHeader(Constants.AUTHORIZATION,
                        String.format("Basic %s", mEncoder.encode(credentials.getBytes())));
            }
        };
    }
}
