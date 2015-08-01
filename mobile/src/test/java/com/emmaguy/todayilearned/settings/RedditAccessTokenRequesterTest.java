package com.emmaguy.todayilearned.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class RedditAccessTokenRequesterTest {
    private RedditAccessTokenRequester mTokenRequester;

    @Mock private UniqueIdentifierStorage mUniqueIdentifierStorage;
    @Mock private Resources mResources;
    @Mock private Context mContext;

    @Before public void before() {
        initMocks(this);

        mTokenRequester = new RedditAccessTokenRequester(mContext, mResources, mUniqueIdentifierStorage);
    }

    @Test public void request_generatesNewUniqueIdentifierAndStartsActivity() {
        mTokenRequester.request();

        verify(mUniqueIdentifierStorage).getUniqueIdentifier();
        verify(mContext).startActivity(any(Intent.class));
    }
}
