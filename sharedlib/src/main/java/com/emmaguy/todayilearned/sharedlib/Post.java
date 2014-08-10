package com.emmaguy.todayilearned.sharedlib;

public class Post {
    private final String mTitle;
    private final String mSubreddit;
    private final String mPermalink;
    private final long mCreatedUtc;

    public Post(String title, String subreddit, String permalink, long createdUtc) {
        mTitle = title;
        mPermalink = permalink;
        mCreatedUtc = createdUtc;
        mSubreddit = String.format("/r/%s", subreddit);
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getPermalink() {
        return mPermalink;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }
}
