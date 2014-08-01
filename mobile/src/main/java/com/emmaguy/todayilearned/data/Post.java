package com.emmaguy.todayilearned.data;

public class Post {
    private final String mId;
    private final String mTitle;
    private final String mSubreddit;

    public Post(String title, String id, String subreddit) {
        mId = id;
        mTitle = title;
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
}
