package com.emmaguy.todayilearned.storage;

import java.util.Set;

public interface UserStorage {
    int getNumberToRequest();

    void setSeenTimestamp(long timestamp);
    void clearTimestamp();

    long getTimestamp();

    String getSortType();
    String getSubreddits();
    Set<String> getSubredditCollection();
    String getRefreshInterval();

    boolean messagesEnabled();
    boolean downloadFullSizedImages();
    boolean openOnPhoneDismissesAfterAction();
}
