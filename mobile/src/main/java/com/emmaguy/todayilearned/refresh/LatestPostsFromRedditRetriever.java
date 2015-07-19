package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by emma on 14/06/15.
 */
public class LatestPostsFromRedditRetriever {
    private final ImageDownloader mImageDownloader;
    private final UserStorage mUserStorage;

    @Inject public LatestPostsFromRedditRetriever(ImageDownloader imageDownloader, UserStorage userStorage) {
        mImageDownloader = imageDownloader;
        mUserStorage = userStorage;
    }

    public Observable<List<Post>> getPosts(final RedditService reddit) {
        return reddit.latestPosts(mUserStorage.getSubreddits(), mUserStorage.getSortType(), mUserStorage.getNumberToRequest())
                .lift(LatestPostsFromRedditRetriever.<Post>flattenList())
                .filter(new Func1<Post, Boolean>() {
                    @Override
                    public Boolean call(Post post) {
                        // Check that this post is new (i.e. we haven't retrieved it before)
                        return mUserStorage.isTimestampNewerThanStored(post.getCreatedUtc());
                    }
                })
                .doOnNext(new Action1<Post>() {
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
                })
                .toList()
                .doOnNext(new Action1<List<Post>>() {
                    // once all the posts have been processed, find the newest timestamp so we don't see these ones again
                    @Override public void call(List<Post> posts) {
                        for (Post p : posts) {
                            mUserStorage.setSeenTimestamp(p.getCreatedUtc());
                        }
                    }
                });
    }

    private static <T> Observable.Operator<T, List<T>> flattenList() {
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

    private boolean isImage(String pictureFileName) {
        return pictureFileName.endsWith(".png")
                || pictureFileName.endsWith(".jpg")
                || pictureFileName.endsWith(".jpeg");
    }
}
