package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.android.gms.wearable.Asset;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Retrieves the latest n posts from the user's preferred subreddit(s) and all unread direct messages
 */
public class LatestPostsRetriever {
    private final RedditService mUnauthenticatedRedditService;
    private final RedditService mAuthenticatedRedditService;

    private final ImageDownloader mImageDownloader;
    private final TokenStorage mTokenStorage;
    private final UserStorage mUserStorage;

    public LatestPostsRetriever(ImageDownloader imageDownloader, TokenStorage tokenStorage, UserStorage userStorage,
            RedditService unauthenticatedRedditService, RedditService authenticatedRedditService) {
        mImageDownloader = imageDownloader;
        mTokenStorage = tokenStorage;
        mUserStorage = userStorage;
        mUnauthenticatedRedditService = unauthenticatedRedditService;
        mAuthenticatedRedditService = authenticatedRedditService;
    }

    private RedditService getRedditServiceForLoggedInState() {
        if (mTokenStorage.isLoggedIn()) {
            return mAuthenticatedRedditService;
        }

        return mUnauthenticatedRedditService;
    }

    @NonNull public Observable<List<PostAndImage>> retrieve() {
        final Observable<List<PostAndImage>> postsObservable = Observable.defer(new Func0<Observable<List<PostAndImage>>>() {
            @Override public Observable<List<PostAndImage>> call() {
                return getRedditServiceForLoggedInState()
                        .latestPosts(mUserStorage.getSubreddits(), mUserStorage.getSortType(), mUserStorage.getNumberToRequest())
                        .lift(LatestPostsRetriever.<Post>flattenList())
                        .filter(new Func1<Post, Boolean>() {
                            @Override
                            public Boolean call(Post post) {
                                // Check that this post is new (i.e. we haven't retrieved it before)
                                return post.getCreatedUtc() > mUserStorage.getTimestamp();
                            }
                        })
                        .flatMap(new Func1<Post, Observable<PostAndImage>>() {
                            @Override public Observable<PostAndImage> call(final Post post) {
                                return Observable.defer(new Func0<Observable<PostAndImage>>() {
                                    @Override public Observable<PostAndImage> call() {
                                        final PostAndImage postAndImage = new PostAndImage(post);
                                        if (post.hasImageUrl()) {
                                            final byte[] bytes = mImageDownloader.downloadImage(post.getImageUrl());
                                            if (bytes != null) {
                                                postAndImage.setImage(Asset.createFromBytes(bytes));
                                            }
                                        }
                                        return Observable.just(postAndImage);
                                    }
                                }).onErrorResumeNext(new Func1<Throwable, Observable<PostAndImage>>() {
                                    @Override public Observable<PostAndImage> call(Throwable throwable) {
                                        // If we fail to download the image, just skip this Post
                                        return Observable.empty();
                                    }
                                });
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
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<List<PostAndImage>>>() {
            @Override public Observable<List<PostAndImage>> call(Throwable throwable) {
                // If we fail somewhere whilst retrieving posts, just emit an empty list
                return Observable.just(Collections.<PostAndImage>emptyList());
            }
        });

        final Observable<List<PostAndImage>> messagesObservable = Observable.defer(new Func0<Observable<List<PostAndImage>>>() {
            @Override public Observable<List<PostAndImage>> call() {
                if (mTokenStorage.isLoggedIn() && mUserStorage.messagesEnabled()) {
                    return mAuthenticatedRedditService.unreadMessages()
                            .lift(LatestPostsRetriever.<Post>flattenList())
                            .map(new Func1<Post, PostAndImage>() {
                                @Override public PostAndImage call(Post post) {
                                    return new PostAndImage(post);
                                }
                            })
                            .toList()
                            .flatMap(new Func1<List<PostAndImage>, Observable<List<PostAndImage>>>() {
                                @Override public Observable<List<PostAndImage>> call(List<PostAndImage> posts) {
                                    if (!posts.isEmpty()) {
                                        MarkAllRead markAllRead = mAuthenticatedRedditService.markAllMessagesRead();
                                        if (markAllRead.hasErrors()) {
                                            throw new RuntimeException("Failed to mark all messages as read: " + markAllRead);
                                        }
                                    }

                                    return Observable.just(posts);
                                }
                            });
                } else {
                    return Observable.just(Collections.<PostAndImage>emptyList());
                }
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<List<PostAndImage>>>() {
            @Override public Observable<List<PostAndImage>> call(Throwable throwable) {
                // If we fail somewhere whilst retrieving messages, just emit an empty list
                return Observable.just(Collections.<PostAndImage>emptyList());
            }
        });

        return Observable.merge(postsObservable, messagesObservable);
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
