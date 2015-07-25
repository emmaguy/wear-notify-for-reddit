package com.emmaguy.todayilearned.refresh;

import android.content.res.Resources;

import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

@Module
public class ConverterModule {
    @Provides
    @Singleton
    public Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public GsonConverter provideGsonConverter(Gson gson) {
        return new GsonConverter(gson);
    }

    @Provides
    @Singleton
    @Named("token")
    public Converter provideTokenConverter(GsonConverter gsonConverter) {
        return new TokenConverter(gsonConverter);
    }

    @Provides
    @Singleton
    @Named("markread")
    public Converter provideMarkAsReadConverter() {
        return new MarkAsReadConverter();
    }

    @Provides
    @Singleton
    @Named("posts")
    public Converter providePostConverter(Gson gson, GsonConverter gsonConverter, Resources resources, UserStorage userStorage) {
        return new PostConverter(gson, gsonConverter, resources, userStorage);
    }

    @Provides
    @Singleton
    @Named("redditResponse")
    public Converter provideRedditResponseConverter() {
        return new GsonConverter(new GsonBuilder().registerTypeAdapter(RedditResponse.class, new RedditResponse.CommentResponseJsonDeserializer()).create());
    }

    @Provides
    @Singleton
    @Named("comments")
    public Converter provideCommentsConverter(Gson gson, GsonConverter gsonConverter, Resources resources, UserStorage userStorage) {
        return new CommentsConverter(gson, gsonConverter, resources, userStorage);
    }
}
