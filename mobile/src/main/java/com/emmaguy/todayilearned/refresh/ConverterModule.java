package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;

@Module
public class ConverterModule {
    @Provides
    @Singleton
    public Converter provideTokenConverter() {
        return new TokenConverter(new GsonConverter(new GsonBuilder().create()));
    }

    @Provides
    @Singleton
    public GsonConverter providePostsConverter() {
        return new GsonConverter(new GsonBuilder().registerTypeAdapter(Post.getPostsListTypeToken(), new PostsDeserialiser()).create());
    }
}
