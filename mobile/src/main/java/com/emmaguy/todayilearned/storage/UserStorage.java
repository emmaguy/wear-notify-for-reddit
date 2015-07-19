package com.emmaguy.todayilearned.storage;

public interface UserStorage {
    int getNumberToRequest();

    void setSeenTimestamp(long timestamp);
    boolean isTimestampNewerThanStored(long timestamp);

    String getSortType();
    String getSubreddits();

    boolean messagesEnabled();
    boolean downloadFullSizedImages();
    boolean openOnPhoneDismissesAfterAction();
}
