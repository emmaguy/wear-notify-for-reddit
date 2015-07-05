package com.emmaguy.todayilearned.refresh;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Converts a {@link TokenResponse} into a {@link Token}
 * Throws an exception if the token is null or missing its access token
 */
class TokenConverter implements Converter {
    private final Converter mOriginalConverter;

    TokenConverter(Converter originalConverter) {
        mOriginalConverter = originalConverter;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        if (type != Token.class) {
            return mOriginalConverter.fromBody(body, type);
        }

        // We don't check for null refresh token, it's optional - it's retrieved on initial auth but not on a token refresh
        TokenResponse response = (TokenResponse) mOriginalConverter.fromBody(body, TokenResponse.class);
        if (response == null || response.getAccessToken() == null) {
            throw new ConversionException("Empty/missing token response: " + response);
        }
        return new Token.Builder()
                .accessToken(response.getAccessToken())
                .refreshToken(response.getRefreshToken())
                .expiryTimeMillis(DateTime.now(DateTimeZone.UTC).plusSeconds(response.getExpiresIn()).getMillis())
                .build();
    }

    @Override
    public TypedOutput toBody(Object object) {
        return mOriginalConverter.toBody(object);
    }
}
