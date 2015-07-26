package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.settings.SubscriptionResponse;
import com.emmaguy.todayilearned.sharedlib.Post;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * A converter that figures out what type we've received and uses other converters to convert to the found type
 */
public class DelegatingConverter implements Converter {
    private final SubscriptionConverter mSubscriptionConverter;
    private final MarkAsReadConverter mMarkAsReadConverter;
    private final TokenConverter mTokenConverter;
    private final PostConverter mPostConverter;
    private final Converter mOriginalConverter;

    public DelegatingConverter(Converter originalConverter, TokenConverter tokenConverter, PostConverter postConverter, MarkAsReadConverter markAsReadConverter, SubscriptionConverter subscriptionConverter) {
        mMarkAsReadConverter = markAsReadConverter;
        mOriginalConverter = originalConverter;
        mTokenConverter = tokenConverter;
        mPostConverter = postConverter;
        mSubscriptionConverter = subscriptionConverter;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        if (type == Token.class) {
            return mTokenConverter.fromBody(body, type);
        } else if (type == MarkAllRead.class) {
            return mMarkAsReadConverter.fromBody(body, type);
        } else if (type == SubscriptionResponse.class) {
            return mSubscriptionConverter.fromBody(body, type);
        } else if (isListOfPosts(type)) {
            return mPostConverter.fromBody(body, type);
        }

        return mOriginalConverter.fromBody(body, type);
    }

    private boolean isListOfPosts(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getActualTypeArguments()[0] == Post.class;
        }
        return false;
    }

    @Override
    public TypedOutput toBody(Object object) {
        return mOriginalConverter.toBody(object);
    }
}
