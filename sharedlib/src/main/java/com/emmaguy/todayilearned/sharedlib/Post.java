package com.emmaguy.todayilearned.sharedlib;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Post {
    @Expose
    private final String mSubreddit;
    @Expose
    private final String mTitle;
    @Expose
    private final String mDescription;
    @Expose
    private final String mFullname;
    @Expose
    private final String mPermalink;
    @Expose
    private final long mUps;
    @Expose
    private final long mDowns;
    @Expose
    private final String mAuthor;
    @Expose
    private final String mId;
    @Expose
    private final String mThumbnail;
    @Expose
    private final long mCreatedUtc;

    // When we serialise the Post to send to the wearable, don't include the thumbnail - it will be added as an asset
    private byte[] mThumbnailImage;

    @Expose
    private List<Post> mComments;

    @Expose
    private List<Post> mReplies;

    public Post(String title, String subreddit, String selftext, String fullname, String permalink, String author, String id, String thumbnail, long createdUtc, long ups, long downs) {
        mTitle = title;
        mDescription = selftext;
        mFullname = fullname;
        mPermalink = permalink;
        mUps = ups;
        mDowns = downs;
        mAuthor = String.format("/u/%s", author);
        mCreatedUtc = createdUtc;
        mSubreddit = String.format("/r/%s", subreddit);
        mThumbnail = thumbnail;
        mId = id;
    }

    public String getTitle() {
        if (TextUtils.isEmpty(mTitle)) {
            return "";
        }
        return mTitle.trim();
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public String getDescription() {
        if (TextUtils.isEmpty(mDescription)) {
            return "";
        }
        return mDescription.trim();
    }

    public String getFullname() {
        return mFullname;
    }

    public long getCreatedUtc() {
        return mCreatedUtc;
    }

    public String getPermalink() {
        if (isDirectMessage()) {
            return "/message/messages/" + mId;
        }

        return mPermalink;
    }

    public String getShortTitle() {
        if (isDirectMessage()) {
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

    public boolean hasThumbnail() {
        String thumbnail = mThumbnail == null ? "" : mThumbnail.trim();
        return !TextUtils.isEmpty(thumbnail) && !thumbnail.equals("default") && !thumbnail.equals("nsfw") && !thumbnail.equals("self");
    }

    public String getThumbnail() {
        return mThumbnail;
    }

    public void setThumbnailImage(byte[] thumbnailImage) {
        mThumbnailImage = thumbnailImage;
    }

    public byte[] getThumbnailImage() {
        return mThumbnailImage;
    }

    public String getId() {
        return mId;
    }

    public String getPostContents() {
        String title = getTitle();
        String description = getDescription();

        if (TextUtils.isEmpty(title)) {
            return description;
        }

        if (TextUtils.isEmpty(description)) {
            return title;
        }

        return title + "\n\n" + description;
    }

    public static Type getPostsListTypeToken() {
        return new TypeToken<List<Post>>() {}.getType();
    }

    public long getUps() {
        return mUps;
    }

    public long getDowns() {
        return mDowns;
    }

    public void setReplies(List<Post> replies) {
        mReplies = replies;
    }

    public List<Post> getReplies() {
        return mReplies;
    }
}
