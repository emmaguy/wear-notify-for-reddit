package com.emmaguy.todayilearned.data;

public class Post {
    private final String mId;
    private final String mTitle;
    private final String mSubreddit;
    private final long mCreatedUtc;

    public Post(String title, String id, String subreddit, long createdUtc) {
        mId = id;
        mTitle = title;
        mCreatedUtc = createdUtc;
        mSubreddit = String.format("/r/%s", subreddit);
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }
}
