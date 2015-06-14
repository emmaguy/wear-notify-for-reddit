package com.emmaguy.todayilearned.data.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;

/**
 * Created by emma on 14/06/15.
 */
public class SharedPreferencesUniqueIdentifierStorage implements UniqueIdentifierStorage {
    private final SharedPreferences mSharedPreferences;
    private final Resources mResources;

    public SharedPreferencesUniqueIdentifierStorage(SharedPreferences sharedPreferences, Resources resources) {
        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    @Override
    public void store(String stateString) {
        mSharedPreferences.edit().putString(mResources.getString(R.string.prefs_key_state), stateString).apply();
    }

    @Override
    public String getStateString() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_state), "");
    }
}
