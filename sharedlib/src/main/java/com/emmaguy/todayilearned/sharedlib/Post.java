package com.emmaguy.todayilearned.sharedlib;

import java.util.List;

public class Post {
    private final boolean mIsDirectMessage;
    private final boolean mHasImageUrl;
    private final boolean mScoreHidden;

    private final String mShortTitle;
    private final String mPostContents;
    private final String mSubreddit;
    private final String mPermalink;
    private final String mFullname;
    private final String mImageUrl;
    private final String mAuthor;
    private final String mTitle;
    private final String mUrl;
    private final String mId;

    private final long mCreatedUtc;
    private final int mReplyLevel;
    private final int mScore;
    private final int mGilded;

    private final List<Post> mReplies;

    public Post(Builder builder) {
        mId = builder.mId;
        mUrl = builder.mUrl;
        mScore = builder.mScore;
        mTitle = builder.mTitle;
        mGilded = builder.mGilded;
        mAuthor = builder.mAuthor;
        mReplies = builder.mReplies;
        mImageUrl = builder.mImageUrl;
        mFullname = builder.mFullname;
        mSubreddit = builder.mSubreddit;
        mPermalink = builder.mPermalink;
        mReplyLevel = builder.mReplyLevel;
        mShortTitle = builder.mShortTitle;
        mCreatedUtc = builder.mCreatedUtc;
        mHasImageUrl = builder.mHasImageUrl;
        mPostContents = builder.mPostContents;
        mScoreHidden = builder.mIsScoreHidden;
        mIsDirectMessage = builder.mIsDirectMessage;
    }

    public boolean isDirectMessage() {
        return mIsDirectMessage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getFullname() {
        return mFullname;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public String getPermalink() {
        return mPermalink;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getId() {
        return mId;
    }

    public int getScore() {
        return mScore;
    }

    public int getGilded() {
        return mGilded;
    }

    public boolean isScoreHidden() {
        return mScoreHidden;
    }

    public String getShortTitle() {
        return mShortTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean hasImageUrl() {
        return mHasImageUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getPostContents() {
        return mPostContents;
    }

    public int getReplyLevel() {
        return mReplyLevel;
    }

    public List<Post> getReplies() {
        return mReplies;
    }

    public static final class Builder {
        private boolean mIsDirectMessage;
        private boolean mHasImageUrl;
        private boolean mIsScoreHidden;

        private String mPostContents;
        private String mShortTitle;
        private String mPermalink;
        private String mSubreddit;
        private String mImageUrl;
        private String mFullname;
        private String mAuthor;
        private String mTitle;
        private String mUrl;
        private String mId;
        private List<Post> mReplies;

        private long mCreatedUtc;
        private int mGilded;
        private int mScore;
        private int mReplyLevel;

        public Post build() {
            return new Post(this);
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setSubreddit(String subreddit) {
            mSubreddit = subreddit;
            return this;
        }

        public Builder setIsDirectMessage(boolean isDirectMessage) {
            mIsDirectMessage = isDirectMessage;
            return this;
        }

        public Builder setPermalink(String permalink) {
            mPermalink = permalink;
            return this;
        }

        public Builder setPostContents(String postContents) {
            mPostContents = postContents;
            return this;
        }

        public Builder setShortTitle(String shortTitle) {
            mShortTitle = shortTitle;
            return this;
        }

        public Builder hasImageUrl(boolean hasImageUrl) {
            mHasImageUrl = hasImageUrl;
            return this;
        }

        public Builder setCreatedUtc(long createdUtc) {
            mCreatedUtc = createdUtc;
            return this;
        }

        public Builder setIsScoreHidden(boolean isScoreHidden) {
            mIsScoreHidden = isScoreHidden;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
            return this;
        }

        public Builder setId(String id) {
            mId = id;
            return this;
        }

        public Builder setGilded(int gilded) {
            mGilded = gilded;
            return this;
        }

        public Builder setFullname(String fullname) {
            mFullname = fullname;
            return this;
        }

        public Builder setUrl(String url) {
            mUrl = url;
            return this;
        }

        public Builder setAuthor(String author) {
            mAuthor = author;
            return this;
        }

        public Builder setScore(int score) {
            mScore = score;
            return this;
        }

        public Builder setReplies(List<Post> replies) {
            mReplies = replies;
            return this;
        }

        public Builder setReplyLevel(int replyLevel) {
            mReplyLevel = replyLevel;
            return this;
        }
    }
}
