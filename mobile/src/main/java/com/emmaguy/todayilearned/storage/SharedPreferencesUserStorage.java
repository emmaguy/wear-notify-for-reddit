package com.emmaguy.todayilearned.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Constants;

import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

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
        if (hasTimestampBeenSeen(createdAtUtc)) {
            mSharedPreferences
                    .edit()
                    .putLong(mResources.getString(R.string.prefs_key_created_utc), createdAtUtc)
                    .apply();
        }
    }

    private long getCreatedUtcOfRetrievedPosts() {
        return mSharedPreferences.getLong(mResources.getString(R.string.prefs_key_created_utc), 0);
    }

    @Override
    public String getSortType() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_sort_order), "new");
    }

    @Override
    public String getSubreddits() {
        final String key = mResources.getString(R.string.prefs_key_selected_subreddits);
        Set<String> subreddits = mSharedPreferences.getStringSet(key, Constants.sDefaultSelectedSubreddits);

        return TextUtils.join("+", new ArrayList(subreddits));
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

    @Override public boolean hasTimestampBeenSeen(long postCreatedUtc) {
        return postCreatedUtc > getCreatedUtcOfRetrievedPosts();
    }
}
