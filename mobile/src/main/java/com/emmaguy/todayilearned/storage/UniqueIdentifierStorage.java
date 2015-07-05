package com.emmaguy.todayilearned.storage;

public interface UniqueIdentifierStorage {
    void storeUniqueIdentifier(String uniqueIdentifier);
    String getUniqueIdentifier();
}
