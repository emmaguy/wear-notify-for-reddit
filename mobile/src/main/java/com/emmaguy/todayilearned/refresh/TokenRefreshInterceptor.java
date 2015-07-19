package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.RetrofitError;

/**
 * Refresh a token - transparently to rest of the code. Will block a request whilst doing the token refresh,
 * then continue with the original request once we have a valid token again.
 * <p/>
 * Created by emma on 14/06/15.
 */
class TokenRefreshInterceptor implements Interceptor {
    private static final String BEARER_FORMAT = "bearer %s";

    private final TokenStorage mTokenStorage;
    private final RedditService mRefreshRedditService;

    TokenRefreshInterceptor(TokenStorage tokenStorage, RedditService refreshRedditService) {
        mTokenStorage = tokenStorage;
        mRefreshRedditService = refreshRedditService;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response;
        if (request.url().toString().contains("access_token")) {
            // if we're trying to get the access token, carry on
            response = chain.proceed(request);
        } else if (mTokenStorage.hasNoToken()) {
            throw new RuntimeException("No token");
        } else if (mTokenStorage.hasTokenExpired()) {
            response = renewTokenAndDoRequest(chain, request);
        } else {
            response = addHeaderAndProceedWithChain(chain, request);
            if (response.code() == HttpURLConnection.HTTP_FORBIDDEN || response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                mTokenStorage.forceExpireToken();
                // throw an IOException, so that this request will be retried
                throw new IOException("Token error, throwing to retry");
            }
        }

        return response;
    }

    private Response addHeaderAndProceedWithChain(Chain chain, Request originalRequest) throws IOException {
        final String value = String.format(BEARER_FORMAT, mTokenStorage.getAccessToken());
        final Request authenticatedRequest = originalRequest.newBuilder().header(Constants.AUTHORIZATION, value).build();
        return chain.proceed(authenticatedRequest);
    }

    // synchronized so we only renew one request at a time
    private synchronized Response renewTokenAndDoRequest(Chain chain, Request originalRequest) throws IOException {
        if (mTokenStorage.hasTokenExpired()) {
            try {
                Token token = mRefreshRedditService.refreshToken(Constants.GRANT_TYPE_REFRESH_TOKEN, mTokenStorage.getRefreshToken());
                mTokenStorage.updateToken(token);
            } catch (RetrofitError error) {
                if (error.getResponse() == null || isServerError(error.getResponse())) {
                    throw new RuntimeException(error.getCause());
                } else {
                    mTokenStorage.clearToken();
                    throw new RuntimeException(error.getCause());
                }
            }
        }
        return addHeaderAndProceedWithChain(chain, originalRequest);
    }

    private boolean isServerError(retrofit.client.Response response) {
        return response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND || response.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}
