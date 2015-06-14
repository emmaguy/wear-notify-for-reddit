package com.emmaguy.todayilearned.data.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by emma on 14/06/15.
 */
public class TokenResponse {
    @SerializedName("access_token")
    private String mAccessToken;

    @SerializedName("token_type")
    private String mTokenType;

    @SerializedName("expires_in")
    private int mExpiresIn;

    @SerializedName("refresh_token")
    private String mRefreshToken;

    @SerializedName("scope")
    private String mScope;

    public String getAccessToken() {
        return mAccessToken;
    }

    public String getTokenType() {
        return mTokenType;
    }

    public int getExpiresIn() {
        return mExpiresIn;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public String getScope() {
        return mScope;
    }
}
