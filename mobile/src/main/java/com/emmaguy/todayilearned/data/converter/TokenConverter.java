package com.emmaguy.todayilearned.data.converter;

/**
 * Created by emma on 14/06/15.
 */

import com.emmaguy.todayilearned.data.model.Token;
import com.emmaguy.todayilearned.data.response.TokenResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class TokenConverter implements Converter {
    private final Converter mOriginalConverter;

    public TokenConverter(Converter originalConverter) {
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
