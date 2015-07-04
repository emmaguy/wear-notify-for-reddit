package com.emmaguy.todayilearned.data.storage;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;

import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Constants;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by emma on 14/06/15.
 */
public class SharedPreferencesUserStorage implements UserStorage {
    private final SharedPreferences mSharedPreferences;
    private final Resources mResources;

    public SharedPreferencesUserStorage(SharedPreferences sharedPreferences, Resources resources) {

        mSharedPreferences = sharedPreferences;
        mResources = resources;
    }

    @Override
    public int getNumberToRequest() {
        final String key = mResources.getString(R.string.prefs_key_number_to_retrieve);
        return Integer.parseInt(mSharedPreferences.getString(key, "5"));
    }

    public void setRetrievedPostCreatedUtc(long createdAtUtc) {
        if (createdAtUtc > getCreatedUtcOfRetrievedPosts()) {
            Logger.log("Updating mLatestCreatedUtc to: " + createdAtUtc);

            mSharedPreferences
                    .edit()
                    .putLong(mResources.getString(R.string.prefs_key_created_utc), createdAtUtc)
                    .apply();
        }
    }

    @Override
    public long getCreatedUtcOfRetrievedPosts() {
        return mSharedPreferences.getLong(mResources.getString(R.string.prefs_key_created_utc), 0);
    }

    @Override
    public String getSortType() {
        return mSharedPreferences.getString(mResources.getString(R.string.prefs_key_sort_order), "new");
    }

    @Override
    public String getSubreddit() {
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
}
