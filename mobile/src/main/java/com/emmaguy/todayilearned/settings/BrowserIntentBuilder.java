package com.emmaguy.todayilearned.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by emma on 11/08/15.
 */
public class BrowserIntentBuilder {
    private static final String DEFAULT_URL = "https://www.google.com";

    private static final String PACKAGE_NAME = "packageName";
    private static final String SIMPLE_NAME = "simpleName";
    private static final String CLASS_NAME = "className";

    private final PackageManager mPackageManager;

    @Inject public BrowserIntentBuilder(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Nullable public Intent build(String title, Intent target) {
        // find all the browsers installed - don't use the reddit url because that will include 3rd party reddit clients
        final Intent dummy = new Intent(Intent.ACTION_VIEW);
        dummy.setData(Uri.parse(DEFAULT_URL));

        final List<ResolveInfo> resInfo = mPackageManager.queryIntentActivities(dummy, 0);

        final List<HashMap<String, String>> metaInfo = new ArrayList<>();
        for (ResolveInfo ri : resInfo) {
            if (ri.activityInfo == null) {
                continue;
            }

            final HashMap<String, String> info = new HashMap<>();
            info.put(PACKAGE_NAME, ri.activityInfo.packageName);
            info.put(CLASS_NAME, ri.activityInfo.name);
            info.put(SIMPLE_NAME, String.valueOf(ri.activityInfo.loadLabel(mPackageManager)));
            metaInfo.add(info);
        }

        if (metaInfo.isEmpty()) {
            return null;
        }

        // sort items by display name
        Collections.sort(metaInfo, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
                return map.get(SIMPLE_NAME).compareTo(map2.get(SIMPLE_NAME));
            }
        });

        // create the custom intent list
        final List<Intent> targetedIntents = new ArrayList<>();
        for (HashMap<String, String> mi : metaInfo) {
            Intent targetedShareIntent = (Intent) target.clone();
            targetedShareIntent.setPackage(mi.get(PACKAGE_NAME));
            targetedShareIntent.setClassName(mi.get(PACKAGE_NAME), mi.get(CLASS_NAME));
            targetedIntents.add(targetedShareIntent);
        }

        final Intent chooserIntent = Intent.createChooser(targetedIntents.get(0), title);
        targetedIntents.remove(0);

        final Parcelable[] targetedIntentsParcelable = targetedIntents.toArray(new Parcelable[targetedIntents.size()]);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntentsParcelable);

        return chooserIntent;
    }
}
