package com.emmaguy.todayilearned.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.emmaguy.todayilearned.BuildConfig;
import com.emmaguy.todayilearned.R;

public class Utils {
    public static final boolean sIsDebug = BuildConfig.DEBUG;

    public static final String FEEDBACK_EMAIL_ADDRESS = "ringthebellapp@gmail.com";

    public static Intent getFeedbackEmailIntent(Context c) {
        final String title = c.getString(R.string.app_name) + " " + c.getString(R.string.feedback_android_version);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, title + BuildConfig.VERSION_NAME);
        return intent;
    }
}
