package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.common.StringUtils;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.android.gms.wearable.Asset;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import timber.log.Timber;

/**
 * Retrieves the latest n posts from the user's preferred subreddit(s)
 */
public class LatestPostsRetriever {
    private final RedditService mRedditService;

    private final ImageDownloader mImageDownloader;
    private final UserStorage mUserStorage;

    public LatestPostsRetriever(@NonNull final ImageDownloader imageDownloader,
                                @NonNull final UserStorage userStorage,
                                @NonNull final RedditService redditService) {
        mImageDownloader = imageDownloader;
        mUserStorage = userStorage;
        mRedditService = redditService;
    }

    @NonNull public Observable<List<PostAndImage>> retrieve() {
        final long currentSavedTimestamp = mUserStorage.getTimestamp();
        return Observable.defer(() -> {
            String subreddits = mUserStorage.getSubreddits();
            if (StringUtils.isEmpty(subreddits)) {
                subreddits = StringUtils.join("+", Constants.sDefaultSelectedSubreddits);
            }
            return mRedditService.latestPosts(subreddits,
                    mUserStorage.getSortType(),
                    mUserStorage.getNumberToRequest())
                    .doOnNext(posts -> {
                        long maxTimestamp = 0;
                        for (final Post p : posts) {
                            if (p.getCreatedUtc() > maxTimestamp) {
                                maxTimestamp = p.getCreatedUtc();
                            }
                        }
                        mUserStorage.setSeenTimestamp(maxTimestamp);
                    })
                    .flatMap(Observable::from)
                    .filter(post -> post.getCreatedUtc() > currentSavedTimestamp)
                    .flatMap(post -> Observable.defer(() -> {
                        final PostAndImage postAndImage = new PostAndImage(post);
                        if (post.hasImageUrl()) {
                            final byte[] bytes = mImageDownloader.downloadImage(post.getImageUrl());
                            if (bytes != null) {
                                postAndImage.setImage(Asset.createFromBytes(bytes));
                            }
                        }
                        return Observable.just(postAndImage);
                    }).onErrorResumeNext(throwable -> Observable.empty()))
                    .toList();
        }).onErrorResumeNext(throwable -> {
            Timber.e(throwable, "Failed to get latest posts");
            // If we fail somewhere whilst retrieving posts, just emit an empty list
            return Observable.just(Collections.<PostAndImage>emptyList());
        });
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
