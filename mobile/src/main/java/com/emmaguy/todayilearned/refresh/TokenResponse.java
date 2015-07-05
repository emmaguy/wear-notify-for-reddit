package com.emmaguy.todayilearned.refresh;

import com.google.gson.annotations.SerializedName;

/**
 * Token from the server
 */
class TokenResponse {
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

    @Override public String toString() {
        return "TokenResponse{" +
                "mAccessToken='" + mAccessToken + '\'' +
                ", mTokenType='" + mTokenType + '\'' +
                ", mExpiresIn=" + mExpiresIn +
                ", mRefreshToken='" + mRefreshToken + '\'' +
                ", mScope='" + mScope + '\'' +
                '}';
    }
}
