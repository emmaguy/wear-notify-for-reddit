package com.emmaguy.todayilearned.data.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;

import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.data.model.Token;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by emma on 14/06/15.
 */
public class SharedPreferencesTokenStorage implements TokenStorage {
    private final SharedPreferences mSharedPreferences;
    private final Resources mResources;

    public SharedPreferencesTokenStorage(SharedPreferences sharedPreferences, Resources resources) {
        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    @Override
    public boolean isLoggedIn() {
        return !hasNoToken();
    }

    @Override
    public boolean hasNoToken() {
        return TextUtils.isEmpty(getRefreshToken()) || TextUtils.isEmpty(getAccessToken());
    }

    @Override
    public boolean hasTokenExpired() {
        long expiryTime = mSharedPreferences.getLong(mResources.getString(R.string.prefs_key_token_expiry_millis), -1);
        final boolean hasExpired = expiryTime < DateTime.now(DateTimeZone.UTC).getMillis();
        if(hasExpired) {
            Logger.log("token has expired");
        } else {
            Logger.log("token not expired");
        }

        return hasExpired;
    }

    @Override
    public void saveToken(Token token) {
        Logger.log("saveToken");
        mSharedPreferences
                .edit()
                .putLong(mResources.getString(R.string.prefs_key_token_expiry_millis), token.getExpiryTimeMillis())
                .putString(mResources.getString(R.string.prefs_key_token_access_token), token.getAccessToken())
                .putString(mResources.getString(R.string.prefs_key_token_refresh_token), token.getRefreshToken())
                .apply();
    }

    @Override
    public void updateToken(Token token) {
        // Update the expiry and access token, but the refresh token remains the same
        mSharedPreferences
                .edit()
                .putLong(mResources.getString(R.string.prefs_key_token_expiry_millis), token.getExpiryTimeMillis())
                .putString(mResources.getString(R.string.prefs_key_token_access_token), token.getAccessToken())
                .apply();
    }

    @Override
    public void clearToken() {
        Logger.log("clearToken");
        mSharedPreferences
                .edit()
                .remove(mResources.getString(R.string.prefs_key_token_expiry_millis))
                .remove(mResources.getString(R.string.prefs_key_token_access_token))
                .remove(mResources.getString(R.string.prefs_key_token_refresh_token))
                .apply();
    }

    @Override
    public void forceExpireToken() {
        Logger.log("forceExpireToken");
        final long timeInThePast = DateTime.now(DateTimeZone.UTC).getMillis() - 1;
        mSharedPreferences
                .edit()
                .putLong(mResources.getString(R.string.prefs_key_token_expiry_millis), timeInThePast)
                .apply();
    }

    @Override
    public String getRefreshToken() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_token_refresh_token), "");
    }

    @Override
    public String getAccessToken() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_token_access_token), "");
    }
}
