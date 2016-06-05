package com.emmaguy.todayilearned;

import android.os.Bundle;

import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.firebase.analytics.FirebaseAnalytics;

import timber.log.Timber;

public class Analytics {
    private final FirebaseAnalytics mFirebaseAnalytics;

    public Analytics(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;

        mFirebaseAnalytics.setUserProperty("build_time", BuildConfig.BUILD_TIME);
        mFirebaseAnalytics.setUserProperty("git_sha", BuildConfig.GIT_SHA);
    }

    public void sendLogin(boolean loginSuccessful) {
        Timber.d("Sending event login successful: " + loginSuccessful);

        Bundle bundle = new Bundle();
        bundle.putBoolean("login_success", loginSuccessful);

        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    public void sendEvent(String category, String result) {
        Timber.d("Sending event: " + category + " , " + result);

        Bundle bundle = new Bundle();
        bundle.putString("name", result);

        mFirebaseAnalytics.logEvent(category, bundle);
    }

    public void sendRefreshEvent(UserStorage userStorage, TokenStorage tokenStorage) {
        Timber.d("Sending refresh event");

        Bundle bundle = new Bundle();
        bundle.putInt("number_of_posts", userStorage.getNumberToRequest());
        bundle.putString("sort_type", userStorage.getSortType());
        bundle.putString("refresh_interval", userStorage.getRefreshInterval());
        bundle.putStringArray("subreddits",
                userStorage.getSubredditCollection()
                        .toArray(new String[userStorage.getSubredditCollection().size()]));
        bundle.putBoolean("high_res_images", userStorage.downloadFullSizedImages());
        bundle.putBoolean("messages_enabled", userStorage.messagesEnabled());
        bundle.putBoolean("open_on_phone_dismisses", userStorage.openOnPhoneDismissesAfterAction());
        bundle.putBoolean("is_logged_in", tokenStorage.isLoggedIn());
        bundle.putBoolean("token_expired", tokenStorage.hasTokenExpired());

        mFirebaseAnalytics.logEvent("refresh", bundle);
    }
}
