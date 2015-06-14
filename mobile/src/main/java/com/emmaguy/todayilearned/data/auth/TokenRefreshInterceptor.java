package com.emmaguy.todayilearned.data.auth;

import com.emmaguy.todayilearned.data.retrofit.RedditService;
import com.emmaguy.todayilearned.data.model.Token;
import com.emmaguy.todayilearned.data.storage.TokenStorage;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.HttpStatus;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Refresh a token - transparently to rest of the code, blocks a request whilst it does the refresh then continues
 * with the original request once we have a valid token again
 *
 * Created by emma on 14/06/15.
 */
public class TokenRefreshInterceptor implements Interceptor {
    private static final String BEARER_FORMAT = "bearer %s";

    private final TokenStorage mTokenStorage;
    private final RedditService mRedditService;

    public TokenRefreshInterceptor(TokenStorage tokenStorage, RedditService service) {
        mTokenStorage = tokenStorage;
        mRedditService = service;
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
            response = addAuthenticationHeaderAndProceed(chain, request);
            if (isUnauthenticatedTokenResponse(response)) {
                mTokenStorage.forceExpireToken();
                // throw an IOException, so that this request will be retried
                throw new IOException("Token error, throwing to retry");
            }
        }

        return response;
    }

    // this needs to be synchronized, so that multiple renew token requests don't happen at the same time
    private synchronized Response renewTokenAndDoRequest(Chain chain, Request originalRequest) throws IOException {
        if (mTokenStorage.hasTokenExpired()) {
            try {
                Token token = mRedditService.refreshToken(Constants.GRANT_TYPE_REFRESH_TOKEN, mTokenStorage.getRefreshToken());
                mTokenStorage.saveToken(token);
            } catch (RetrofitError error) {
                if (error.getResponse() == null || isServerErrorResponse(error.getResponse())) {
                    throw new RuntimeException(error.getCause());
                } else {
                    mTokenStorage.clearToken();
                    throw new RuntimeException(error.getCause());
                }
            }
        }
        return addAuthenticationHeaderAndProceed(chain, originalRequest);
    }

    private boolean isServerErrorResponse(retrofit.client.Response response) {
        return (response.getStatus() == HttpStatus.SC_NOT_FOUND || response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    private Response addAuthenticationHeaderAndProceed(Chain chain, Request request) throws IOException {
        final String value = String.format(BEARER_FORMAT, mTokenStorage.getAccessToken());
        final Request authenticatedRequest = request.newBuilder().header(Constants.AUTHORIZATION, value).build();
        return chain.proceed(authenticatedRequest);
    }

    private boolean isUnauthenticatedTokenResponse(Response response) throws IOException {
        if (response.code() == HttpStatus.SC_FORBIDDEN || response.code() == HttpStatus.SC_UNAUTHORIZED) {
            return true;
        }
        return false;
    }
}
