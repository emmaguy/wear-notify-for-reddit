package com.emmaguy.todayilearned.data.model;

/**
 * Created by emma on 14/06/15.
 */
public class Token {
    private long mExpiryTimeMillis;
    private String mAccessToken;
    private String mRefreshToken;

    private Token(Builder builder) {
        mExpiryTimeMillis = builder.mExpiryTimeMillis;
        mAccessToken = builder.mAccessTokenBytes;
        mRefreshToken = builder.mRefreshTokenBytes;
    }

    public long getExpiryTimeMillis() {
        return mExpiryTimeMillis;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }


    @Override public String toString() {
        return "Token{" +
                "mExpiryTimeMillis=" + mExpiryTimeMillis +
                ", mAccessToken='" + mAccessToken + '\'' +
                ", mRefreshToken='" + mRefreshToken + '\'' +
                '}';
    }

    public static final class Builder {
        private long mExpiryTimeMillis;
        private String mAccessTokenBytes;
        private String mRefreshTokenBytes;

        public Builder expiryTimeMillis(long expiryTimeMillis) {
            mExpiryTimeMillis = expiryTimeMillis;
            return this;
        }

        public Builder accessToken(String accessToken) {
            mAccessTokenBytes = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            mRefreshTokenBytes = refreshToken;
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }
}
