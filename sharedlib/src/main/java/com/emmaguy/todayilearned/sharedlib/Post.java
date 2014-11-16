package com.emmaguy.todayilearned.sharedlib;

public class Post {
    private final String mSubreddit;
    private final String mTitle;
    private final String mDescription;
    private final String mFullname;
    private final String mPermalink;
    private final String mAuthor;
    private final String mId;
    private final long mCreatedUtc;

    public Post(String title, String subreddit, String selftext, String fullname, String permalink, String author, String id, long createdUtc) {
        mTitle = title;
        mDescription = selftext;
        mFullname = fullname;
        mPermalink = permalink;
        mAuthor = author;
        mCreatedUtc = createdUtc;
        mSubreddit = String.format("/r/%s", subreddit);
        mId = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getFullname() {
        return mFullname;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public String getPermalink() {
        if(isDirectMessage()) {
            return "/message/messages/" + mId;
        }

        return mPermalink;
    }

    public String getShortTitle() {
        if(isDirectMessage()) {
            return getShortDescription();
        }

        return getShortString(mTitle);
    }

    private String getShortString(String string) {
        if (string.length() < 15) {
            return string;
        }
        return string.substring(0, 12) + "...";
    }

    public boolean isDirectMessage() {
        return mFullname.startsWith("t4");
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getShortDescription() {
        if (mDescription.contains("\n")) {
            String title = mDescription.substring(0, mDescription.indexOf("\n"));
            return getShortString(title);
        }
        return getShortString(mDescription);
    }
}
