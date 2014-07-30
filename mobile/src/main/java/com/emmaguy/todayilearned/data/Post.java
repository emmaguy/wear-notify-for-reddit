package com.emmaguy.todayilearned.data;

public class Post {
    private final String mId;
    private final String mTitle;

    public Post(String title, String id) {
        mId = id;
        mTitle = title;
    }

    public String getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }
}