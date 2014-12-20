package com.emmaguy.todayilearned;

/*
 * Copyright (C) 2014 Read It Later Inc. (Pocket)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * Helper methods for saving to Pocket with an Intent.
 */
public class PocketUtil {
    private static final int UTIL_VERSION = 1;

    private static final String[] POCKET_PACKAGE_NAMES = new String[]{
            "com.ideashower.readitlater.pro",
            "com.pocket.cn",
            "com.pocket.ru",
            "com.pocket.corgi"
    };

    private static final String EXTRA_SOURCE_PACKAGE = "source";
    private static final String EXTRA_TWEET_STATUS_ID = "tweetStatusId";
    private static final String EXTRA_VERSION = "utilVersion";

    /**
     * Creates a new Intent that will save a url to the user's Pocket account.
     *
     * @param url           A url starting with http:// or https://
     * @param tweetStatusId Optional. If the url is saved from a tweet, pass its status id to attribute the save to that tweet. Otherwise pass null.
     * @param context
     * @return The Activity Intent that can be started to save the url, or null if Pocket is not installed.
     * @see #isPocketInstalled(Context)
     * @see #installPocket(Activity)
     */
    public static Intent newAddToPocketIntent(String url, String tweetStatusId, Context context) {
        String pocketPackageName = getPocketPackageName(context);
        if (pocketPackageName == null) {
            return null; // Pocket not installed
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(pocketPackageName);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, url);
        if (tweetStatusId != null && tweetStatusId.length() > 0) {
            intent.putExtra(EXTRA_TWEET_STATUS_ID, tweetStatusId);
        }
        intent.putExtra(EXTRA_SOURCE_PACKAGE, context.getPackageName());
        intent.putExtra(EXTRA_VERSION, UTIL_VERSION);

        return intent;
    }

    /**
     * Returns true if Pocket is installed on this device.
     *
     * @see #installPocket(Activity)
     */
    public static boolean isPocketInstalled(Context context) {
        return getPocketPackageName(context) != null;
    }

    /**
     * This looks for all possible Pocket versions and returns the package name of one if it is installed.
     * Otherwise returns null if Pocket is not installed.
     */
    private static String getPocketPackageName(Context context) {
        for (String pname : POCKET_PACKAGE_NAMES) {
            if (isAppInstalled(context, pname)) {
                return pname;
            }
        }
        return null;
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info;
        try {
            info = pm.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            info = null;
        }

        return info != null;
    }

}