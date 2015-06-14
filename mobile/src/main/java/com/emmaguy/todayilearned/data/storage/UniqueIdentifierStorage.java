package com.emmaguy.todayilearned.data.storage;

/**
 * Created by emma on 14/06/15.
 */
public interface UniqueIdentifierStorage {
    void store(String stateString);
    String getStateString();
}
