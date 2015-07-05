package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;

import javax.inject.Inject;

/**
 * Stores a unique identifier in {@link SharedPreferences}
 */
class SharedPreferencesUniqueIdentifierStorage implements UniqueIdentifierStorage {
    private final SharedPreferences mSharedPreferences;
    private final Resources mResources;

    @Inject SharedPreferencesUniqueIdentifierStorage(SharedPreferences sharedPreferences, Resources resources) {
        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    @Override
    public void storeUniqueIdentifier(String uniqueIdentifier) {
        mSharedPreferences.edit().putString(mResources.getString(R.string.prefs_key_state), uniqueIdentifier).apply();
    }

    @Override
    public String getUniqueIdentifier() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_state), "");
    }
}
