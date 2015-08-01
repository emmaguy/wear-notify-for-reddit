package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.common.StringUtils;

import java.util.UUID;

/**
 * Stores a unique identifier in {@link SharedPreferences}
 */
public class SharedPreferencesUniqueIdentifierStorage implements UniqueIdentifierStorage {
    private final SharedPreferences mSharedPreferences;
    private final String mKey;

    public SharedPreferencesUniqueIdentifierStorage(SharedPreferences sharedPreferences, String key) {
        mSharedPreferences = sharedPreferences;
        mKey = key;
    }

    private String generateNewUniqueIdentifier() {
        final String uniqueIdentifier = UUID.randomUUID().toString();
        mSharedPreferences.edit().putString(mKey, uniqueIdentifier).apply();
        return uniqueIdentifier;
    }

    @Override @NonNull public String getUniqueIdentifier() {
        String uniqueIdentifier = getStoredIdentifier();

        if (!StringUtils.isEmpty(uniqueIdentifier)) {
            return uniqueIdentifier;
        }

        return generateNewUniqueIdentifier();
    }

    @NonNull private String getStoredIdentifier() {
        return mSharedPreferences.getString(mKey, "");
    }
}
