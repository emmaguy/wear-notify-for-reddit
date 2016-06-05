package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

import retrofit.RetrofitError;

/**
 * Refresh a token - transparently to rest of the code. Will block a request whilst doing the token refresh,
 * then continue with the original request once we have a valid token again.
 * <p>
 * Created by emma on 14/06/15.
 */
public class TokenRefreshInterceptor implements Interceptor {
    private static final String BEARER_FORMAT = "bearer %s";

    private final TokenStorage mTokenStorage;
    private final RedditAuthenticationService mAuthenticationService;

    public TokenRefreshInterceptor(TokenStorage tokenStorage,
                                   RedditAuthenticationService authenticationService) {
        mTokenStorage = tokenStorage;
        mAuthenticationService = authenticationService;
    }

    @Override public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response;
        if (request.url().toString().contains("access_token")) {
            // if we're trying to get the access token, carry on
            response = chain.proceed(request);
        } else if (mTokenStorage.hasNoToken()) {
            // User hasn't logged in, request an app only token
            response = requestAppOnlyTokenAndProceed(chain, request);
        } else if (mTokenStorage.hasTokenExpired()) {
            // Token's expired, renew it first
            response = renewTokenAndProceed(chain, request);
        } else {
            response = makeRequest(chain, request);
        }

        return response;
    }

    // synchronized so we only renew one request at a time
    @NonNull private synchronized Response requestAppOnlyTokenAndProceed(Chain chain,
                                                                         Request originalRequest) throws
            IOException {
        try {
            Token token = mAuthenticationService.appOnlyToken(Constants.GRANT_TYPE_INSTALLED_CLIENT,
                    UUID.randomUUID().toString());
            mTokenStorage.updateToken(token);
        } catch (RetrofitError error) {
            if (error.getResponse() == null || isServerError(error.getResponse())) {
                throw new RuntimeException(
                        "Failed to retrieve app only token, empty response/server error: " + error.getCause());
            } else {
                throw new RuntimeException("Failed to retrieve app only token, unknown cause: " + error
                        .getCause());
            }
        }
        return addHeaderAndProceedWithChain(chain, originalRequest);
    }

    @NonNull
    private synchronized Response renewTokenAndProceed(Chain chain, Request originalRequest) throws
            IOException {
        if (mTokenStorage.hasTokenExpired()) {
            try {
                Token token = mAuthenticationService.refreshToken(Constants.GRANT_TYPE_REFRESH_TOKEN,
                        mTokenStorage.getRefreshToken());
                mTokenStorage.updateToken(token);
            } catch (RetrofitError error) {
                if (error.getResponse() == null || isServerError(error.getResponse())) {
                    throw new RuntimeException(
                            "Failed to renew token, empty response/server error: " + error.getCause());
                } else {
                    throw new RuntimeException("Failed to renew token, unknown cause: " + error.getCause());
                }
            }
        }
        return addHeaderAndProceedWithChain(chain, originalRequest);
    }

    @NonNull private Response makeRequest(Chain chain, Request request) throws IOException {
        Response r = addHeaderAndProceedWithChain(chain, request);
        if (r.code() == HttpURLConnection.HTTP_FORBIDDEN || r.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            mTokenStorage.forceExpireToken();
            // throw an IOException, so that this request will be retried
            throw new IOException("Token problem, throwing to retry");
        }
        return r;
    }

    @NonNull
    private Response addHeaderAndProceedWithChain(Chain chain, Request originalRequest) throws
            IOException {
        final String value = String.format(BEARER_FORMAT, mTokenStorage.getAccessToken());
        final Request authenticatedRequest = originalRequest.newBuilder()
                .header(Constants.AUTHORIZATION, value)
                .build();
        return chain.proceed(authenticatedRequest);
    }

    private boolean isServerError(retrofit.client.Response response) {
        return response.getStatus() == HttpURLConnection.HTTP_NOT_FOUND || response.getStatus() == HttpURLConnection.HTTP_INTERNAL_ERROR;
    }
}
