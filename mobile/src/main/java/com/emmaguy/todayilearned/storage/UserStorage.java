package com.emmaguy.todayilearned.storage;

public interface UserStorage {
    int getNumberToRequest();

    void setSeenTimestamp(long timestamp);
    void clearTimestamp();
    boolean isTimestampNewerThanStored(long timestamp);

    String getTimestamp();
    String getSortType();
    String getSubreddits();
    String getRefreshInterval();

    boolean messagesEnabled();
    boolean downloadFullSizedImages();
    boolean openOnPhoneDismissesAfterAction();
}
