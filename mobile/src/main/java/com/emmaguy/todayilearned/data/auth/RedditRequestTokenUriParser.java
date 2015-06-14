package com.emmaguy.todayilearned.data.auth;

import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.data.storage.UniqueIdentifierStorage;
import com.emmaguy.todayilearned.data.storage.TokenStorage;

/**
 * Created by emma on 14/06/15.
 */
public class RedditRequestTokenUriParser {
    private final Resources mResources;
    private final TokenStorage mTokenStorage;
    private final UniqueIdentifierStorage mUniqueIdentifierStorage;

    private boolean mHasValidCode;
    private boolean mShowError;
    private String mCode;

    public RedditRequestTokenUriParser(Resources resources, TokenStorage tokenStorage, UniqueIdentifierStorage uniqueIdentifierStorage) {
        mResources = resources;
        mTokenStorage = tokenStorage;
        mUniqueIdentifierStorage = uniqueIdentifierStorage;
    }

    public void setUri(Uri uri) {
        mHasValidCode = false;
        mShowError = false;
        mCode = "";

        if (uri != null && uri.toString().startsWith(mResources.getString(R.string.redirect_url_scheme)) && mTokenStorage.hasNoToken()) {
            String error = uri.getQueryParameter("error");
            if (!TextUtils.isEmpty(error)) {
                mShowError = true;
            } else {
                mCode = uri.getQueryParameter("code");
                String state = uri.getQueryParameter("state");

                if (state.equals(mUniqueIdentifierStorage.getStateString())) {
                    mHasValidCode = true;
                } else {
                    mShowError = true;
                }
            }
        }

    }

    public boolean showError() {
        return mShowError;
    }

    public String getCode() {
        return mCode;
    }

    public boolean hasValidCode() {
        return mHasValidCode;
    }
}
