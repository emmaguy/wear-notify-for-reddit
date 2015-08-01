package com.emmaguy.todayilearned.settings;

import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UniqueIdentifierStorage;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Parses a request token from a given {@link Uri}
 */
class RedditRequestTokenUriParser {
    private final Resources mResources;
    private final TokenStorage mTokenStorage;
    private final UniqueIdentifierStorage mStateStorage;

    private boolean mHasValidCode;
    private boolean mShowError;
    private String mCode;

    @Inject RedditRequestTokenUriParser(Resources resources, TokenStorage tokenStorage, @Named("state") UniqueIdentifierStorage stateStorage) {
        mResources = resources;
        mTokenStorage = tokenStorage;
        mStateStorage = stateStorage;
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

                if (state.equals(mStateStorage.getUniqueIdentifier())) {
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
