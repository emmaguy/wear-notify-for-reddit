package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RedditAccessTokenRequesterTest {
    private RedditAccessTokenRequester mTokenRequester;

    @Mock private UniqueIdentifierStorage mUniqueIdentifierStorage;
    @Mock private PackageManager mPackageManager;
    @Mock private Resources mResources;
    @Mock private Context mContext;
    @Mock private BrowserIntentBuilder mBrowserIntentBuilder;

    @Mock private Intent mIntent;

    @Before public void before() {
        initMocks(this);

        when(mResources.getString(R.string.chooser_title)).thenReturn("title");
        when(mBrowserIntentBuilder.build(eq("title"), any(Intent.class))).thenReturn(mIntent);

        mTokenRequester = new RedditAccessTokenRequester(mContext,
                mResources,
                mUniqueIdentifierStorage,
                mBrowserIntentBuilder);
    }

    @Test public void request_generatesNewUniqueIdentifierAndStartsActivity() {
        mTokenRequester.request();

        verify(mUniqueIdentifierStorage).getUniqueIdentifier();
        verify(mContext).startActivity(any(Intent.class));
    }
}
