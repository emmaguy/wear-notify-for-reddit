package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by emma on 14/06/15.
 */
class RedditAccessTokenRequester {
    private final Context mContext;
    private final Resources mResources;
    private final UniqueIdentifierStorage mStateStorage;

    @Inject RedditAccessTokenRequester(Context context, Resources resources, @Named("state") UniqueIdentifierStorage stateStorage) {
        mContext = context;
        mResources = resources;
        mStateStorage = stateStorage;
    }

    public void request() {
        final String redirectUrl = mResources.getString(R.string.redirect_url_scheme) + mResources.getString(R.string.redirect_url_callback);

        String url = Constants.WEB_URL_REDDIT + "/api/v1/authorize.compact?" +
                "client_id=" + mResources.getString(R.string.client_id) +
                "&duration=" + mResources.getString(R.string.reddit_auth_duration) +
                "&response_type=code" +
                "&state=" + mStateStorage.getUniqueIdentifier() +
                "&redirect_uri=" + redirectUrl +
                "&scope=" + mResources.getString(R.string.reddit_auth_scope);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(url));

        final Intent i = createOpenBrowserIntent(mResources.getString(R.string.chooser_title), browserIntent);
        if (i == null) {
            Toast.makeText(mContext, R.string.no_browsers_installed, Toast.LENGTH_LONG).show();
        } else {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }
    }

    @Nullable private Intent createOpenBrowserIntent(String title, Intent target) {
        // find all the browsers installed - don't use the reddit url because that will include 3rd party reddit clients
        final Intent dummy = new Intent(Intent.ACTION_VIEW);
        dummy.setData(Uri.parse("https://www.google.com"));

        final List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(dummy, 0);

        final List<HashMap<String, String>> metaInfo = new ArrayList<>();
        for (ResolveInfo ri : resInfo) {
            if (ri.activityInfo == null) {
                continue;
            }

            HashMap<String, String> info = new HashMap<>();
            info.put("packageName", ri.activityInfo.packageName);
            info.put("className", ri.activityInfo.name);
            info.put("simpleName", String.valueOf(ri.activityInfo.loadLabel(mContext.getPackageManager())));
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
