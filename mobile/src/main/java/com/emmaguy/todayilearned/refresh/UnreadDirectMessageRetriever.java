package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import timber.log.Timber;

/**
 * Retrieves all unread direct messages for the logged in user, marks as read on successful retrieval
 */
public class UnreadDirectMessageRetriever {
    private final RedditService mRedditService;

    private final TokenStorage mTokenStorage;
    private final UserStorage mUserStorage;

    public UnreadDirectMessageRetriever(@NonNull final TokenStorage tokenStorage,
                                        @NonNull final UserStorage userStorage,
                                        @NonNull final RedditService redditService) {
        mTokenStorage = tokenStorage;
        mUserStorage = userStorage;
        mRedditService = redditService;
    }

    @NonNull public Observable<List<Post>> retrieve() {
        return Observable.defer(() -> {
            if (mTokenStorage.isLoggedIn() && mUserStorage.messagesEnabled()) {
                return mRedditService.unreadMessages().flatMap(posts -> {
                    if (!posts.isEmpty()) {
                        MarkAllRead markAllRead = mRedditService.markAllMessagesRead();
                        if (markAllRead.hasErrors()) {
                            throw new RuntimeException("Failed to mark all messages as read: " + markAllRead);
                        }
                    }

                    return Observable.just(posts);
                });
            } else {
                return Observable.just(Collections.<Post>emptyList());
            }
        }).onErrorResumeNext(throwable -> {
            Timber.e(throwable, "UnreadDirectMessageRetriever: failed to get unread messages");
            // If we fail somewhere whilst retrieving messages, just emit an empty list
            return Observable.just(Collections.<Post>emptyList());
        });
    }
}
