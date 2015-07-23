package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.util.List;

import javax.inject.Named;

import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
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
    private final GsonConverter mPostsConverter;
    private final TokenStorage mTokenStorage;
    private final UserStorage mUserStorage;
    private final Converter mMarkAsReadConverter;

    public LatestPostsRetriever(ImageDownloader imageDownloader, TokenStorage tokenStorage, UserStorage userStorage,
            UnauthenticatedRedditService unauthenticatedRedditService, AuthenticatedRedditService authenticatedRedditService,
            @Named("posts") GsonConverter postsConverter, @Named("markread") Converter markAsReadConverter) {
        mImageDownloader = imageDownloader;
        mTokenStorage = tokenStorage;
        mUserStorage = userStorage;
        mUnauthenticatedRedditService = unauthenticatedRedditService;
        mAuthenticatedRedditService = authenticatedRedditService;
        mPostsConverter = postsConverter;
        mMarkAsReadConverter = markAsReadConverter;
    }

    private RedditService getRedditServiceForLoggedInState(Converter converter) {
        if (mTokenStorage.isLoggedIn()) {
            return mAuthenticatedRedditService.getRedditService(converter);
        }

        return mUnauthenticatedRedditService.getRedditService(converter, null);
    }

    @NonNull public Observable<List<Post>> getPosts() {
        final Observable<List<Post>> newPostsObservable = getRedditServiceForLoggedInState(mPostsConverter)
                .latestPosts(mUserStorage.getSubreddits(), mUserStorage.getSortType(), mUserStorage.getNumberToRequest())
                .lift(LatestPostsRetriever.<Post>flattenList())
                .filter(filterNewPosts())
                .doOnNext(downloadThumbnailImage())
                .toList()
                .doOnNext(updateSeenTimestamp());

        final Observable<List<Post>> newMessagesOrEmptyObservable;
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

    @NonNull private Func1<Post, Boolean> filterNewPosts() {
        return new Func1<Post, Boolean>() {
            @Override
            public Boolean call(Post post) {
                // Check that this post is new (i.e. we haven't retrieved it before)
                return mUserStorage.isTimestampNewerThanStored(post.getCreatedUtc());
            }
        };
    }

    @NonNull private Action1<Post> downloadThumbnailImage() {
        return new Action1<Post>() {
            @Override
            public void call(Post post) {
                // Default to just getting the thumbnail, if available
                String imageUrl = post.getThumbnail();
                boolean hasHighResAvailable = false;

                // If user has chosen to get full images, only do so if we actually have a image based url
                if (mUserStorage.downloadFullSizedImages()) {
                    if (isImage(post.getUrl())) {
                        imageUrl = post.getUrl();
                        hasHighResAvailable = true;
                    }
                }

                if (post.hasThumbnail() || hasHighResAvailable) {
                    post.setHasHighResImage(hasHighResAvailable);
                    mImageDownloader.downloadImage(post, imageUrl);
                }
            }
        };
    }

    private boolean isImage(String pictureFileName) {
        return pictureFileName.endsWith(".png")
                || pictureFileName.endsWith(".jpg")
                || pictureFileName.endsWith(".jpeg");
    }

    @NonNull private Action1<List<Post>> updateSeenTimestamp() {
        return new Action1<List<Post>>() {
            // once all the posts have been processed, find the newest timestamp so we don't see these ones again
            @Override public void call(List<Post> posts) {
                for (Post p : posts) {
                    mUserStorage.setSeenTimestamp(p.getCreatedUtc());
                }
            }
        };
    }
}
