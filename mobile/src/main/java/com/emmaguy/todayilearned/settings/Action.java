package com.emmaguy.todayilearned.settings;

class Action {
    public final int mId;
    public final boolean mEnabled;
    public final String mName;
    public final String mDisabledReason;

    Action(int id, String name) {
        mId = id;
        mName = name;
        mEnabled = true;
        mDisabledReason = null;
    }

    Action(int id, String name, boolean isEnabled, String disabledReason) {
        mId = id;
        mName = name;
        mEnabled = isEnabled;
        mDisabledReason = disabledReason;
    }

    public String getName() {
        return mEnabled ? mName : mName + " " + mDisabledReason;
    }
}
