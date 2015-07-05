package com.emmaguy.todayilearned.storage;

import com.emmaguy.todayilearned.refresh.Token;

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
