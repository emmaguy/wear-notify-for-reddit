package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.settings.SubscriptionResponse;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by emma on 26/07/15.
 */
public class SubscriptionConverter implements Converter {
    @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
        return new GsonConverter(new GsonBuilder().registerTypeAdapter(SubscriptionResponse.class, new SubscriptionResponse.SubscriptionResponseJsonDeserializer()).create()).fromBody(body, type);
    }

    @Override public TypedOutput toBody(Object object) {
        throw new UnsupportedOperationException();
    }
}
