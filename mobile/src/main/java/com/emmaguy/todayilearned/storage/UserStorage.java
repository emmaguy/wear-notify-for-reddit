package com.emmaguy.todayilearned.storage;

public interface UserStorage {
    int getNumberToRequest();

    void setSeenTimestamp(long timestamp);
    void clearTimestamp();
    boolean isTimestampNewerThanStored(long timestamp);

    String getSortType();
    String getSubreddits();
    String getRefreshInterval();
    String getTimestamp();

    boolean messagesEnabled();
    boolean downloadFullSizedImages();
    boolean openOnPhoneDismissesAfterAction();
}
