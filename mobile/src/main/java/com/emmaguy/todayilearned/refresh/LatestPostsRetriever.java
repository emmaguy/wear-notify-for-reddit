package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.android.gms.wearable.Asset;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Retrieves the latest n posts from the user's preferred subreddit(s)
 */
public class LatestPostsRetriever {
    private final RedditService mRedditService;

    private final ImageDownloader mImageDownloader;
    private final UserStorage mUserStorage;

    public LatestPostsRetriever(ImageDownloader imageDownloader, UserStorage userStorage, RedditService redditService) {
        mImageDownloader = imageDownloader;
        mUserStorage = userStorage;
        mRedditService = redditService;
    }

    @NonNull public Observable<List<PostAndImage>> retrieve() {
        final long currentSavedTimestamp = mUserStorage.getTimestamp();
        return Observable.defer(new Func0<Observable<List<PostAndImage>>>() {
            @Override public Observable<List<PostAndImage>> call() {
                return mRedditService
                        .latestPosts(mUserStorage.getSubreddits(), mUserStorage.getSortType(), mUserStorage.getNumberToRequest())
                        .lift(LatestPostsRetriever.<Post>flattenList())
                        .filter(new Func1<Post, Boolean>() {
                            @Override
                            public Boolean call(Post post) {
                                // Check that this post is new (i.e. we haven't retrieved it before)
                                return post.getCreatedUtc() > currentSavedTimestamp;
                            }
                        })
                        .doOnNext(new Action1<Post>() {
                            @Override public void call(Post post) {
                                // Save the timestamps from these new posts, so we don't see them again
                                mUserStorage.setSeenTimestamp(post.getCreatedUtc());
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
                        .toList();
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<List<PostAndImage>>>() {
            @Override public Observable<List<PostAndImage>> call(Throwable throwable) {
                Timber.e(throwable, "Failed to get latest posts: " + mUserStorage.getSubreddits() + ", " + mUserStorage.getSortType() + ", " + mUserStorage.getNumberToRequest());
                // If we fail somewhere whilst retrieving posts, just emit an empty list
                return Observable.just(Collections.<PostAndImage>emptyList());
            }
        });
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

        PostAndImage(Post post) {
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
