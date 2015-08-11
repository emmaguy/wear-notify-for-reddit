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
    private final PackageManager mPackageManager;

    @Inject public BrowserIntentBuilder(PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Nullable public Intent build(String title, Intent target) {
        // find all the browsers installed - don't use the reddit url because that will include 3rd party reddit clients
        final Intent dummy = new Intent(Intent.ACTION_VIEW);
        dummy.setData(Uri.parse("https://www.google.com"));

        final List<ResolveInfo> resInfo = mPackageManager.queryIntentActivities(dummy, 0);

        final List<HashMap<String, String>> metaInfo = new ArrayList<>();
        for (ResolveInfo ri : resInfo) {
            if (ri.activityInfo == null) {
                continue;
            }

            HashMap<String, String> info = new HashMap<>();
            info.put("packageName", ri.activityInfo.packageName);
            info.put("className", ri.activityInfo.name);
            info.put("simpleName", String.valueOf(ri.activityInfo.loadLabel(mPackageManager)));
            metaInfo.add(info);
        }

        if (metaInfo.isEmpty()) {
            return null;
        }

        // sort items by display name
        Collections.sort(metaInfo, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> map, HashMap<String, String> map2) {
                return map.get("simpleName").compareTo(map2.get("simpleName"));
            }
        });

        // create the custom intent list
        final List<Intent> targetedIntents = new ArrayList<>();
        for (HashMap<String, String> mi : metaInfo) {
            Intent targetedShareIntent = (Intent) target.clone();
            targetedShareIntent.setPackage(mi.get("packageName"));
            targetedShareIntent.setClassName(mi.get("packageName"), mi.get("className"));
            targetedIntents.add(targetedShareIntent);
        }

        final Intent chooserIntent = Intent.createChooser(targetedIntents.get(0), title);
        targetedIntents.remove(0);

        final Parcelable[] targetedIntentsParcelable = targetedIntents.toArray(new Parcelable[targetedIntents.size()]);
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedIntentsParcelable);

        return chooserIntent;
    }
}
