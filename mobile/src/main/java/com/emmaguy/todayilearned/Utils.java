package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class Utils {
    public static final boolean sIsDebug = true;//BuildConfig.DEBUG;

    public static final String FEEDBACK_EMAIL_ADDRESS = "ringthebellapp@gmail.com";

    public static Intent getFeedbackEmailIntent(Context c) {
        final String title = c.getString(R.string.app_name) + " " + c.getString(R.string.feedback_android_version);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, title + BuildConfig.VERSION_NAME);
        return intent;
    }
}
