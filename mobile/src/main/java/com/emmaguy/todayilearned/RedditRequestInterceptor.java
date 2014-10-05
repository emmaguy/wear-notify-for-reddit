package com.emmaguy.todayilearned;

import retrofit.RequestInterceptor;

public class RedditRequestInterceptor implements RequestInterceptor {
    private final String mCookie;
    private final String mModhash;

    public RedditRequestInterceptor(String cookie, String modhash) {
        mCookie = cookie;
        mModhash = modhash;
    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("X-Modhash", mModhash);
        request.addHeader("Cookie", "reddit_session=" + mCookie);
        request.addHeader("User-Agent", "reddit for wear by /u/jellyfishsuck");
    }
}
