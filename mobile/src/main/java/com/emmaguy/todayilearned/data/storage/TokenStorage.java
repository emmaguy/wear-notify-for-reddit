package com.emmaguy.todayilearned.data.storage;

import com.emmaguy.todayilearned.data.model.Token;

/**
 * Created by emma on 14/06/15.
 */
public interface TokenStorage {
    boolean isLoggedIn();
    boolean hasNoToken();
    boolean hasTokenExpired();

    void saveToken(Token token);
    void updateToken(Token token);
    void clearToken();
    void forceExpireToken();

    String getRefreshToken();
    String getAccessToken();
}
