package com.emmaguy.todayilearned.refresh;

import android.content.res.Resources;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

class CommentsConverter implements Converter {
    private final GsonConverter mOriginalConverter;
    private final UserStorage mUserStorage;
    private final Resources mResources;
    private final Gson mGson;

    CommentsConverter(Gson gson, GsonConverter gsonConverter, Resources resources, UserStorage userStorage) {
        mGson = gson;
        mOriginalConverter = gsonConverter;
        mResources = resources;
        mUserStorage = userStorage;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        List<ListingResponse> listingResponse = (List<ListingResponse>) mOriginalConverter.fromBody(body, new TypeToken<List<ListingResponse>>() {}.getType());

        // First child is always the post itself, second is all the comments
        return new ListingResponseConverter(mGson, mUserStorage, mResources).convert(listingResponse.get(1), 0);
    }

    @Override
    public TypedOutput toBody(Object object) {
        return mOriginalConverter.toBody(object);
    }
}
