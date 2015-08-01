package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by emma on 19/07/15.
 */
public class UnreadDirectMessageRetrieverTest {
    @Mock RedditService mAuthenticatedRedditService;
    @Mock TokenStorage mTokenStorage;
    @Mock UserStorage mUserStorage;

    private UnreadDirectMessageRetriever mRetriever;

    @Before public void before() {
        initMocks(this);

        when(mUserStorage.messagesEnabled()).thenReturn(false);
        when(mTokenStorage.isLoggedIn()).thenReturn(false);

        mRetriever = new UnreadDirectMessageRetriever(mTokenStorage, mUserStorage, mAuthenticatedRedditService);
    }

    @NonNull private Post mockDirectMessage() {
        final Post post = mock(Post.class);
        when(post.isDirectMessage()).thenReturn(true);
        return post;
    }

    @Test public void notLoggedInWithMessagesEnabled_doesNotTryToRetrieveMessages() {
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final List<Post> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<Post>>() {
            @Override public void call(List<Post> posts) {
                emittedElements.addAll(posts);
            }
        });

        verify(mAuthenticatedRedditService, never()).unreadMessages();
        assertThat(emittedElements.size(), equalTo(0));
    }

    @Test public void loggedInButMessagesNotEnabled_doesNotTryToRetrieveMessages() {
        when(mTokenStorage.isLoggedIn()).thenReturn(true);

        final List<Post> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<Post>>() {
            @Override public void call(List<Post> posts) {
                emittedElements.addAll(posts);
            }
        });

        verify(mAuthenticatedRedditService, never()).unreadMessages();
        assertThat(emittedElements.size(), equalTo(0));
    }

    @Test public void loggedInAndMessagesEnabled_retrievesMessagesAndMarksAsRead() {
        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final Post directMessage = mockDirectMessage();
        final Observable<List<Post>> observable = Observable.just(Arrays.asList(directMessage));
        when(mAuthenticatedRedditService.unreadMessages()).thenReturn(observable);

        final MarkAllRead markAllRead = mock(MarkAllRead.class);
        when(markAllRead.hasErrors()).thenReturn(false);
        when(mAuthenticatedRedditService.markAllMessagesRead()).thenReturn(markAllRead);

        final List<Post> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<Post>>() {
            @Override public void call(List<Post> posts) {
                emittedElements.addAll(posts);
            }
        });

        assertThat(emittedElements.size(), equalTo(1));
        assertThat(emittedElements.get(0), equalTo(directMessage));

        verify(mAuthenticatedRedditService).unreadMessages();
        verify(mAuthenticatedRedditService).markAllMessagesRead();
    }

    @Test public void markAsReadFailsDueToNetworkError_emitsNothing() throws Exception {
        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final Observable<List<Post>> observable = Observable.just(Arrays.asList(mockDirectMessage()));
        when(mAuthenticatedRedditService.unreadMessages()).thenReturn(observable);

        final RetrofitError networkError = RetrofitError.networkError("Network error", mock(IOException.class));
        when(mAuthenticatedRedditService.markAllMessagesRead()).thenThrow(networkError);

        final List<Post> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<Post>>() {
            @Override public void call(List<Post> posts) {
                emittedElements.addAll(posts);
            }
        });

        verify(mAuthenticatedRedditService).unreadMessages();

        assertThat(emittedElements.size(), equalTo(0));
    }

    @Test public void retrievingMessageFailsDueToNetworkError_returnsEmptyList() {
        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final RetrofitError networkError = RetrofitError.networkError("Network error", mock(IOException.class));
        when(mAuthenticatedRedditService.unreadMessages()).thenThrow(networkError);

        final List<Post> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<Post>>() {
            @Override public void call(List<Post> posts) {
                emittedElements.addAll(posts);
            }
        });

        verify(mAuthenticatedRedditService).unreadMessages();
        verifyNoMoreInteractions(mAuthenticatedRedditService);

        verify(mAuthenticatedRedditService, never()).markAllMessagesRead();

        assertThat(emittedElements.size(), equalTo(0));
    }
}
