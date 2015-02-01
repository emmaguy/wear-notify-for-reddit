package com.emmaguy.todayilearned;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

public class Utils {
    public static final boolean sIsDebug = BuildConfig.DEBUG;

    public static final String FEEDBACK_EMAIL_ADDRESS = "ringthebellapp@gmail.com";

    public static Intent getFeedbackEmailIntent(Context c) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, c.getString(R.string.feedback_android_version) + Utils.getVersionName(c, c.getApplicationContext().getPackageName()));
        return intent;
    }

    public static boolean isImage(String pictureFileName) {
        return pictureFileName.endsWith(".png")
                || pictureFileName.endsWith(".jpg")
                || pictureFileName.endsWith(".jpeg");
    }

    public static String getVersionName(Context c, String packageName) {
        try {
            return c.getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (Exception e) {
            Logger.sendThrowable(c, "Failed to get version name", e);
        }

        return "";
    }

    public static boolean isLoggedIn(SharedPreferences prefs, Context context) {
        return !TextUtils.isEmpty(getCookie(prefs, context));
    }

    public static String getModhash(SharedPreferences prefs, Context context) {
        return prefs.getString(context.getString(R.string.prefs_key_modhash), "");
    }

    public static String getCookie(SharedPreferences prefs, Context context) {
        return prefs.getString(context.getString(R.string.prefs_key_cookie), "");
    }
}
