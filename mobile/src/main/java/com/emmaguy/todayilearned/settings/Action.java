package com.emmaguy.todayilearned.settings;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class Action {
    private final int mId;
    private final int mResId;
    private final boolean mIsEnabled;
    private final @NonNull String mName;
    private final @Nullable String mDisabledReason;

    Action(int id, String name, @DrawableRes int resId) {
        this(id, name, resId, true, null);
    }

    Action(int id, String name, @DrawableRes int resId, boolean isEnabled, String disabledReason) {
        mId = id;
        mName = name;
        mResId = resId;
        mIsEnabled = isEnabled;
        mDisabledReason = disabledReason;
    }

    public String getName() {
        return mIsEnabled ? mName : mName + " " + mDisabledReason;
    }

    public int getId() {
        return mId;
    }

    public int getResId() {
        return mResId;
    }

    public boolean isEnabled() {
        return mIsEnabled;
    }
}
