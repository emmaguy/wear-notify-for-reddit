package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Retrieves all unread direct messages for the logged in user, marks as read on successful retrieval
 */
public class UnreadDirectMessageRetriever {
    private final RedditService mAuthenticatedRedditService;

    private final TokenStorage mTokenStorage;
    private final UserStorage mUserStorage;

    public UnreadDirectMessageRetriever(TokenStorage tokenStorage, UserStorage userStorage, RedditService authenticatedRedditService) {
        mTokenStorage = tokenStorage;
        mUserStorage = userStorage;
        mAuthenticatedRedditService = authenticatedRedditService;
    }

    @NonNull public Observable<List<Post>> retrieve() {
        return Observable.defer(new Func0<Observable<List<Post>>>() {
            @Override public Observable<List<Post>> call() {
                if (mTokenStorage.isLoggedIn() && mUserStorage.messagesEnabled()) {
                    return mAuthenticatedRedditService.unreadMessages()
                            .flatMap(new Func1<List<Post>, Observable<List<Post>>>() {
                                @Override public Observable<List<Post>> call(List<Post> posts) {
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
                    return Observable.just(Collections.<Post>emptyList());
                }
            }
        }).onErrorResumeNext(new Func1<Throwable, Observable<List<Post>>>() {
            @Override public Observable<List<Post>> call(Throwable throwable) {
                // If we fail somewhere whilst retrieving messages, just emit an empty list
                return Observable.just(Collections.<Post>emptyList());
            }
        });
    }
}
