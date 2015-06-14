package com.emmaguy.todayilearned.data.storage;

/**
 * Created by emma on 14/06/15.
 */
public interface UserStorage {
    int getNumberToRequest();

    long getCreatedUtcOfRetrievedPosts();
    void setRetrievedPostCreatedUtc(long latestCreatedUtc);

    String getSortType();
    String getSubreddit();

    boolean messagesEnabled();
    boolean downloadFullSizedImages();
    boolean openOnPhoneDismissesAfterAction();
}
