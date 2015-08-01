package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit.RequestInterceptor;
import retrofit.RetrofitError;
import retrofit.converter.Converter;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TokenRefreshInterceptorTest {
    private static final String DEFAULT_ACCESS_TOKEN = "access_token";
    private static final String DEFAULT_REFRESH_TOKEN = "refresh_token";
    private static final String AUTHORIZATION = "Authorization";
    private static final String DEFAULT_URL = "http://ssl.reddit.com/";
    private static final String BEARER_ = "bearer ";

    @Mock private RedditAuthenticationService mRedditService;
    @Mock private RequestInterceptor mRequestInterceptor;
    @Mock private Interceptor.Chain mChain;
    @Mock private TokenStorage mTokenStorage;
    @Mock private Converter mMockConverter;
    @Mock private Token mToken;

    private Request mOriginalRequest = new Request.Builder()
            .url(DEFAULT_URL)
            .header(AUTHORIZATION, "Bearer")
            .build();
    private Response mSuccessfulResponse = new Response.Builder()
            .request(mOriginalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(HttpURLConnection.HTTP_OK)
            .build();
    private Response mUnauthorizedResponse = new Response.Builder()
            .request(mOriginalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(HttpURLConnection.HTTP_UNAUTHORIZED)
            .build();

    private TokenRefreshInterceptor mRefreshTokenInterceptor;

    @Before public void before() throws Exception {
        initMocks(this);

        when(mRedditService.refreshToken(anyString(), anyString())).thenReturn(mToken);
        when(mRedditService.appOnlyToken(anyString(), anyString())).thenReturn(mToken);

        mRefreshTokenInterceptor = new TokenRefreshInterceptor(mTokenStorage, mRedditService);
    }

    @Test public void tokenIsEmpty_requestsAppOnlyAuth() throws Exception {
        when(mTokenStorage.hasNoToken()).thenReturn(true);
        when(mChain.request()).thenReturn(mOriginalRequest);
        when(mChain.proceed(argThat(allOf(hasUrl(DEFAULT_URL), hasAuthorisationHeader(BEARER_ + DEFAULT_ACCESS_TOKEN))))).thenReturn(mSuccessfulResponse);
        when(mTokenStorage.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);

        assertThat(mRefreshTokenInterceptor.intercept(mChain), sameInstance(mSuccessfulResponse));

        verify(mRedditService).appOnlyToken(eq(Constants.GRANT_TYPE_INSTALLED_CLIENT), anyString());
        verify(mTokenStorage).updateToken(mToken);
    }

    @Test public void tokenNotExpiredAndNotEmpty_addAuthorisationHeaderAndDoRequest() throws Exception {
        when(mTokenStorage.hasNoToken()).thenReturn(false);
        when(mTokenStorage.hasTokenExpired()).thenReturn(false);
        when(mTokenStorage.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);
        when(mChain.request()).thenReturn(mOriginalRequest);
        when(mChain.proceed(argThat(allOf(hasUrl(DEFAULT_URL), hasAuthorisationHeader(BEARER_ + DEFAULT_ACCESS_TOKEN))))).thenReturn(mSuccessfulResponse);

        assertThat(mRefreshTokenInterceptor.intercept(mChain), sameInstance(mSuccessfulResponse));
    }

    @Test public void tokenNotEmptyButHasExpired_renewsTokenAndUpdatesTokenStorage() throws Exception {
        when(mTokenStorage.hasNoToken()).thenReturn(false);
        when(mTokenStorage.hasTokenExpired()).thenReturn(true);
        when(mTokenStorage.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);
        when(mTokenStorage.getRefreshToken()).thenReturn(DEFAULT_REFRESH_TOKEN);
        when(mChain.request()).thenReturn(mOriginalRequest);
        when(mChain.proceed(argThat(allOf(hasUrl(DEFAULT_URL), hasAuthorisationHeader(BEARER_ + DEFAULT_ACCESS_TOKEN))))).thenReturn(mSuccessfulResponse);

        assertThat(mRefreshTokenInterceptor.intercept(mChain), sameInstance(mSuccessfulResponse));
        verify(mTokenStorage).updateToken(mToken);
    }

    @Test(expected = RuntimeException.class)
    public void tokenExpiredButRenewFailsWithNetworkError_doesNotClearToken() throws Exception {
        when(mTokenStorage.hasNoToken()).thenReturn(false);
        when(mTokenStorage.hasTokenExpired()).thenReturn(true);
        when(mTokenStorage.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);
        when(mTokenStorage.getRefreshToken()).thenReturn(DEFAULT_REFRESH_TOKEN);
        when(mChain.request()).thenReturn(mOriginalRequest);
        when(mChain.proceed(argThat(allOf(hasUrl(DEFAULT_URL), hasAuthorisationHeader(BEARER_ + DEFAULT_ACCESS_TOKEN)))))
                .thenReturn(mSuccessfulResponse);
        when(mRedditService.refreshToken(Constants.GRANT_TYPE_REFRESH_TOKEN, DEFAULT_REFRESH_TOKEN)).thenThrow(RetrofitError.networkError(DEFAULT_URL, new IOException()));

        assertThat(mRefreshTokenInterceptor.intercept(mChain), sameInstance(mSuccessfulResponse));
        verify(mTokenStorage, times(0)).clearToken();
    }

    @Test(expected = RuntimeException.class)
    public void tokenExpiredButRenewFailsWithUnexpectedError_clearsToken() throws Exception {
        when(mTokenStorage.hasNoToken()).thenReturn(false);
        when(mTokenStorage.hasTokenExpired()).thenReturn(true);
        when(mTokenStorage.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);
        when(mTokenStorage.getRefreshToken()).thenReturn(DEFAULT_REFRESH_TOKEN);
        when(mChain.request()).thenReturn(mOriginalRequest);
        when(mChain.proceed(argThat(allOf(hasUrl(DEFAULT_URL), hasAuthorisationHeader(BEARER_ + DEFAULT_ACCESS_TOKEN)))))
                .thenReturn(mSuccessfulResponse);
        when(mRedditService.refreshToken(Constants.GRANT_TYPE_REFRESH_TOKEN, DEFAULT_REFRESH_TOKEN)).thenThrow(RetrofitError.unexpectedError(DEFAULT_URL, new Throwable()));

        assertThat(mRefreshTokenInterceptor.intercept(mChain), sameInstance(mSuccessfulResponse));
        verify(mTokenStorage).clearToken();
    }

    @Test(expected = IOException.class)
    public void tokenNotExpiredNotEmptyAndRequestFails_forceExpireTokenAndThrowIOException() throws Exception {
        when(mTokenStorage.hasNoToken()).thenReturn(false);
        when(mTokenStorage.hasTokenExpired()).thenReturn(false);
        when(mTokenStorage.getAccessToken()).thenReturn(DEFAULT_ACCESS_TOKEN);
        when(mChain.request()).thenReturn(mOriginalRequest);
        when(mChain.proceed(argThat(allOf(hasUrl(DEFAULT_URL), hasAuthorisationHeader(BEARER_ + DEFAULT_ACCESS_TOKEN)))))
                .thenReturn(mUnauthorizedResponse);

        mRefreshTokenInterceptor.intercept(mChain);

        verify(mTokenStorage).forceExpireToken();
    }

    private Matcher<Request> hasAuthorisationHeader(String authHeader) {
        return new FeatureMatcher<Request, String>(equalTo(authHeader), "Authorisation header", "Unexpected auth header") {
            @Override
            protected String featureValueOf(Request actual) {
                return actual.header(AUTHORIZATION);
            }
        };
    }

    private Matcher<Request> hasUrl(String url) {
        return new FeatureMatcher<Request, String>(equalTo(url), "Url", "Unexpected url") {
            @Override
            protected String featureValueOf(Request actual) {
                return actual.urlString();
            }
        };
    }
}
