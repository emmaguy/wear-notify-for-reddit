package com.emmaguy.todayilearned;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Analytics {
    private final FirebaseAnalytics mFirebaseAnalytics;

    public Analytics(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;

        mFirebaseAnalytics.setUserProperty("Build time", BuildConfig.BUILD_TIME);
        mFirebaseAnalytics.setUserProperty("Git SHA", BuildConfig.GIT_SHA);
    }

    public void logLogin(boolean loginSuccessful) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", loginSuccessful);

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    public void sendEvent(String category, String result) {

    }
}
