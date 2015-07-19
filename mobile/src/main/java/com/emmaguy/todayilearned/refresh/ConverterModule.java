package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.sharedlib.Post;
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
    public Converter provideTokenConverter() {
        return new TokenConverter(new GsonConverter(new GsonBuilder().create()));
    }

    @Provides
    @Singleton
    @Named("posts")
    public GsonConverter providePostsConverter() {
        return new GsonConverter(new GsonBuilder().registerTypeAdapter(Post.getPostsListTypeToken(), new PostsDeserialiser()).create());
    }

    @Provides
    @Singleton
    @Named("redditResponse")
    public GsonConverter provideRedditResponseConverter() {
        return new GsonConverter(new GsonBuilder().registerTypeAdapter(RedditResponse.class, new RedditResponse.CommentResponseJsonDeserializer()).create());
    }

    @Provides
    @Singleton
    @Named("comments")
    public GsonConverter provideCommentsConverter() {
        return new GsonConverter(new GsonBuilder().registerTypeAdapter(Post.getPostsListTypeToken(), new CommentsResponse.CommentsResponseJsonDeserialiser()).create());
    }
}
