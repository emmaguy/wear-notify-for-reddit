package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.common.Logger;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.android.gms.wearable.Asset;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import retrofit.converter.Converter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Retrieves the latest n posts from the user's preferred subreddit(s)
 * <p/>
 * Created by emma on 14/06/15.
 */
public class LatestPostsRetriever {
    private final UnauthenticatedRedditService mUnauthenticatedRedditService;
    private final AuthenticatedRedditService mAuthenticatedRedditService;
    private final ImageDownloader mImageDownloader;
    private final TokenStorage mTokenStorage;
    private final UserStorage mUserStorage;
    private final Converter mMarkAsReadConverter;
    private final Converter mPostsConverter;

    public LatestPostsRetriever(ImageDownloader imageDownloader, TokenStorage tokenStorage, UserStorage userStorage,
            UnauthenticatedRedditService unauthenticatedRedditService, AuthenticatedRedditService authenticatedRedditService,
            @Named("posts") Converter postsConverter, @Named("markread") Converter markAsReadConverter) {
        mImageDownloader = imageDownloader;
        mTokenStorage = tokenStorage;
        mUserStorage = userStorage;
        mUnauthenticatedRedditService = unauthenticatedRedditService;
        mAuthenticatedRedditService = authenticatedRedditService;
        mPostsConverter = postsConverter;
        mMarkAsReadConverter = markAsReadConverter;
    }

    // TODO: inject different RedditServices so converters don't need to be passed around
    private RedditService getRedditServiceForLoggedInState(Converter converter) {
        if (mTokenStorage.isLoggedIn()) {
            return mAuthenticatedRedditService.getRedditService(converter);
        }

        return mUnauthenticatedRedditService.getRedditService(converter, null);
    }

    @NonNull public Observable<List<PostAndImage>> getPosts() {
        final Observable<List<PostAndImage>> newPostsObservable = getRedditServiceForLoggedInState(mPostsConverter)
                .latestPosts(mUserStorage.getSubreddits(), mUserStorage.getSortType(), mUserStorage.getNumberToRequest())
                .lift(LatestPostsRetriever.<Post>flattenList())
                .filter(new Func1<Post, Boolean>() {
                    @Override
                    public Boolean call(Post post) {
                        // Check that this post is new (i.e. we haven't retrieved it before)
                        return post.getCreatedUtc() > mUserStorage.getTimestamp();
                    }
                })
                .map(new Func1<Post, PostAndImage>() {
                    @Override public PostAndImage call(Post post) {
                        final PostAndImage postAndImage = new PostAndImage(post);
                        if (post.hasImageUrl()) {
                            final byte[] bytes = mImageDownloader.downloadImage(post.getImageUrl());
                            if (bytes != null) {
                                postAndImage.setImage(Asset.createFromBytes(bytes));
                            }
                        }
                        return postAndImage;
                    }
                })
                .toList()
                .doOnNext(new Action1<List<PostAndImage>>() {
                    @Override public void call(List<PostAndImage> postAndImages) {
                        // Once all the posts have been compared against last saved timestamp
                        // save the timestamps from these posts, so we don't see these ones again
                        for (PostAndImage postAndImage : postAndImages) {
                            mUserStorage.setSeenTimestamp(postAndImage.getPost().getCreatedUtc());
                        }
                    }
                });

        final Observable<List<PostAndImage>> newMessagesOrEmptyObservable;
        if (mTokenStorage.isLoggedIn() && mUserStorage.messagesEnabled()) {
            newMessagesOrEmptyObservable = getRedditServiceForLoggedInState(mPostsConverter)
                    .unreadMessages()
                    .flatMap(new Func1<List<Post>, Observable<List<Post>>>() {
                        @Override public Observable<List<Post>> call(List<Post> posts) {
                            if (!posts.isEmpty()) {
                                MarkAllRead markAllRead = getRedditServiceForLoggedInState(mMarkAsReadConverter).markAllMessagesRead();
                                if (markAllRead.hasErrors()) {
                                    throw new RuntimeException("Failed to mark all messages as read: " + markAllRead);
                                }
                            }

                            return Observable.just(posts);
                        }
                    }).map(new Func1<List<Post>, List<PostAndImage>>() {
                        @Override public List<PostAndImage> call(List<Post> posts) {
                            List<PostAndImage> postAndImages = new ArrayList<>();
                            for (Post p : posts) {
                                postAndImages.add(new PostAndImage(p));
                            }
                            return postAndImages;
                        }
                    });
        } else {
            newMessagesOrEmptyObservable = Observable.empty();
        }

        return Observable.merge(newPostsObservable, newMessagesOrEmptyObservable);
    }

    @NonNull private static <T> Observable.Operator<T, List<T>> flattenList() {
        return new Observable.Operator<T, List<T>>() {
            @Override
            public Subscriber<? super List<T>> call(final Subscriber<? super T> subscriber) {
                return new Subscriber<List<T>>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(List<T> list) {
                        for (T c : list) {
                            subscriber.onNext(c);
                        }
                    }
                };
            }
        };
    }

    static class PostAndImage {
        private final Post mPost;
        private Asset mImage;

        public PostAndImage(Post post) {
            mPost = post;
        }

        public void setImage(Asset image) {
            mImage = image;
        }

        public Asset getImage() {
            return mImage;
        }

        public Post getPost() {
            return mPost;
        }
    }
}
