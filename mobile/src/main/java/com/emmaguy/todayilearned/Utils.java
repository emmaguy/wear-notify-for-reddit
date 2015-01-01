package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class Utils {
    public static final boolean sIsDebug = true;//BuildConfig.DEBUG;

    public static final String FEEDBACK_EMAIL_ADDRESS = "ringthebellapp@gmail.com";

    public static Intent getFeedbackEmailIntent(Context c) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, c.getString(R.string.feedback_android_version) + Utils.getVersionName(c, c.getApplicationContext().getPackageName()));
        return intent;
    }

    public static String getVersionName(Context c, String packageName) {
        try {
            return c.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (Exception e) {
            Logger.sendThrowable(c, "Failed to get version name", e);
        }

        return "";
    }
}
