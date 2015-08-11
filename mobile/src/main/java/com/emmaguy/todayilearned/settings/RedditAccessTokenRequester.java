package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.widget.Toast;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by emma on 14/06/15.
 */
class RedditAccessTokenRequester {
    private final Context mContext;
    private final Resources mResources;
    private final UniqueIdentifierStorage mStateStorage;
    private final BrowserIntentBuilder mBrowserIntentBuilder;

    @Inject RedditAccessTokenRequester(Context context, Resources resources,
            @Named("state") UniqueIdentifierStorage stateStorage, BrowserIntentBuilder browserIntentBuilder) {
        mContext = context;
        mResources = resources;
        mStateStorage = stateStorage;
        mBrowserIntentBuilder = browserIntentBuilder;
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

        final Intent i = mBrowserIntentBuilder.build(mResources.getString(R.string.chooser_title), browserIntent);
        if (i == null) {
            Toast.makeText(mContext, R.string.no_browsers_installed, Toast.LENGTH_LONG).show();
        } else {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }
    }
}
