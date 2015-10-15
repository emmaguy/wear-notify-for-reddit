package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.common.StringUtils;
import com.emmaguy.todayilearned.sharedlib.Constants;

import java.util.Set;
import java.util.Timer;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Stores user's specific preferences in {@link SharedPreferences}
 */
class SharedPreferencesUserStorage implements UserStorage {
    private final SharedPreferences mSharedPreferences;
    private final Resources mResources;

    @Inject SharedPreferencesUserStorage(SharedPreferences sharedPreferences, Resources resources) {
        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    @Override
    public int getNumberToRequest() {
        final String key = mResources.getString(R.string.prefs_key_number_to_retrieve);
        return Integer.parseInt(mSharedPreferences.getString(key, "5"));
    }

    public void setSeenTimestamp(long createdAtUtc) {
        Timber.d("Setting timestamp: " + createdAtUtc);
        mSharedPreferences
                .edit()
                .putLong(mResources.getString(R.string.prefs_key_created_utc), createdAtUtc)
                .apply();
    }

    @Override public void clearTimestamp() {
        mSharedPreferences.edit().remove(mResources.getString(R.string.prefs_key_created_utc)).apply();
    }

    @Override
    public String getSortType() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_sort_order), "new");
    }

    @Override
    public String getSubreddits() {
        final String key = mResources.getString(R.string.prefs_key_selected_subreddits);
        Set<String> subreddits = mSharedPreferences.getStringSet(key, Constants.sDefaultSelectedSubreddits);

        return StringUtils.join("+", subreddits);
    }

    @Override public String getRefreshInterval() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_sync_frequency), "15");
    }

    @Override public long getTimestamp() {
        return mSharedPreferences.getLong(mResources.getString(R.string.prefs_key_created_utc), 0);
    }

    @Override
    public boolean messagesEnabled() {
        return mSharedPreferences.getBoolean(mResources.getString(R.string.prefs_key_messages_enabled), true);
    }

    @Override
    public boolean downloadFullSizedImages() {
        return mSharedPreferences.getBoolean(mResources.getString(R.string.prefs_key_full_image), false);
    }

    @Override
    public boolean openOnPhoneDismissesAfterAction() {
        return mSharedPreferences.getBoolean(mResources.getString(R.string.prefs_key_open_on_phone_dismisses), false);
    }
}
