package com.emmaguy.todayilearned.storage;

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
