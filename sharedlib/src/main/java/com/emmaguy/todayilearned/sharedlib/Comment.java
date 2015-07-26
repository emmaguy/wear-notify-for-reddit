package com.emmaguy.todayilearned.sharedlib;

import java.util.List;

public class Comment {
    private final boolean mScoreHidden;

    private final String mPostContents;
    private final String mAuthor;
    private final String mTitle;

    private final int mReplyLevel;
    private final int mGilded;
    private final int mScore;

    private final List<Comment> mReplies;

    public Comment(Builder builder) {
        mScore = builder.mScore;
        mTitle = builder.mTitle;
        mGilded = builder.mGilded;
        mAuthor = builder.mAuthor;
        mReplies = builder.mReplies;
        mReplyLevel = builder.mReplyLevel;
        mPostContents = builder.mPostContents;
        mScoreHidden = builder.mIsScoreHidden;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
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

    public String getPostContents() {
        return mPostContents;
    }

    public int getReplyLevel() {
        return mReplyLevel;
    }

    public List<Comment> getReplies() {
        return mReplies;
    }

    public static final class Builder {
        private boolean mIsScoreHidden;

        private String mPostContents;
        private String mAuthor;
        private String mTitle;
        private List<Comment> mReplies;

        private int mGilded;
        private int mScore;
        private int mReplyLevel;

        public Comment build() {
            return new Comment(this);
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setAuthor(String author) {
            mAuthor = author;
            return this;
        }

        public Builder setComments(List<Comment> replies) {
            mReplies = replies;
            return this;
        }

        public Builder setPostContents(String postContents) {
            mPostContents = postContents;
            return this;
        }

        public Builder setGilded(int gilded) {
            mGilded = gilded;
            return this;
        }

        public Builder setScore(int score) {
            mScore = score;
            return this;
        }

        public Builder setReplyLevel(int replyLevel) {
            mReplyLevel = replyLevel;
            return this;
        }

        public Builder setIsScoreHidden(boolean scoreHidden) {
            mIsScoreHidden = scoreHidden;
            return this;
        }
    }
}
