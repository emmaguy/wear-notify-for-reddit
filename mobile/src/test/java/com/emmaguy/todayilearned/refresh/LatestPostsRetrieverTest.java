package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by emma on 19/07/15.
 */
public class LatestPostsRetrieverTest {
    private static final String DEFAULT_IMAGE_URL_NO_FILE_EXTENSION = "http://nofileextension";
    private static final String DEFAULT_THUMBNAIL_URL = "http://anythumb";
    private static final String DEFAULT_IMAGE_URL = "http://anyurl.jpg";

    private static final String DEFAULT_SUBREDDIT = "todayilearned";
    private static final String DEFAULT_SORT = "hot";
    private static final int DEFAULT_NUMBER = 5;

    private static final long DEFAULT_TIMESTAMP_NEWER = 110;
    private static final long DEFAULT_TIMESTAMP_NEW = 101;
    private static final long DEFAULT_TIMESTAMP = 100;
    private static final long DEFAULT_TIMESTAMP_OLD = 90;

    @Mock RedditService mUnauthenticatedRedditService;
    @Mock RedditService mAuthenticatedRedditService;

    @Mock ImageDownloader mImageDownloader;
    @Mock TokenStorage mTokenStorage;
    @Mock UserStorage mUserStorage;

    private Post mPost;

    private LatestPostsRetriever mRetriever;

    @Before public void before() {
        initMocks(this);

        when(mUserStorage.messagesEnabled()).thenReturn(false);
        when(mTokenStorage.isLoggedIn()).thenReturn(false);

        when(mUserStorage.getSortType()).thenReturn(DEFAULT_SORT);
        when(mUserStorage.getNumberToRequest()).thenReturn(DEFAULT_NUMBER);
        when(mUserStorage.getSubreddits()).thenReturn(DEFAULT_SUBREDDIT);
        when(mUserStorage.getTimestamp()).thenReturn(DEFAULT_TIMESTAMP);

        // If we ever set the timestamp, we need mockito to return that new value
        updateTimestampWhenSet(DEFAULT_TIMESTAMP_NEWER);
        updateTimestampWhenSet(DEFAULT_TIMESTAMP_NEW);
        updateTimestampWhenSet(DEFAULT_TIMESTAMP_OLD);
        updateTimestampWhenSet(DEFAULT_TIMESTAMP);

        when(mUserStorage.downloadFullSizedImages()).thenReturn(false);

        mPost = mockPost(DEFAULT_TIMESTAMP_NEWER);
        when(mUnauthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(Arrays.asList(mPost)));

        mRetriever = new LatestPostsRetriever(mImageDownloader, mTokenStorage, mUserStorage, mUnauthenticatedRedditService, mAuthenticatedRedditService);
    }

    private void updateTimestampWhenSet(final long timestamp) {
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                when(mUserStorage.getTimestamp()).thenReturn(timestamp);
                return null;
            }
        }).when(mUserStorage).setSeenTimestamp(timestamp);
    }

    @NonNull private Post mockPost(long timestamp) {
        final Post post = mock(Post.class);
        when(post.hasImageUrl()).thenReturn(false);
        when(post.getCreatedUtc()).thenReturn(timestamp);
        return post;
    }

    @NonNull private Post mockDirectMessage(long timestamp) {
        Post post = mockPost(timestamp);
        when(post.isDirectMessage()).thenReturn(true);
        return post;
    }

    @Test public void latestPostsWith2ThatAreNew_savesOnlyTheNewestTimestamps() {
        final List<Post> posts = Arrays.asList(mockPost(DEFAULT_TIMESTAMP_OLD), mockPost(DEFAULT_TIMESTAMP_NEW), mockPost(DEFAULT_TIMESTAMP_NEWER));
        when(mUnauthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(posts));

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        assertThat(emittedElements.size(), equalTo(2));
        verify(mUnauthenticatedRedditService).latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER);

        verifyZeroInteractions(mImageDownloader);
        verifyZeroInteractions(mAuthenticatedRedditService);
        verify(mAuthenticatedRedditService, times(0)).unreadMessages();

        verify(mUserStorage).getSubreddits();
        verify(mUserStorage).getNumberToRequest();
        verify(mUserStorage).getSortType();

        verify(mUserStorage).setSeenTimestamp(DEFAULT_TIMESTAMP_NEW);
        verify(mUserStorage).setSeenTimestamp(DEFAULT_TIMESTAMP_NEWER);
        verify(mUserStorage, times(0)).setSeenTimestamp(DEFAULT_TIMESTAMP);
        verify(mUserStorage, times(0)).setSeenTimestamp(DEFAULT_TIMESTAMP_OLD);
    }

    @Test public void latestPostsWith2ThatAreNewButNewestComeFirst_savesOnlyTheNewestTimestamps() {
        final List<Post> posts = Arrays.asList(mockPost(DEFAULT_TIMESTAMP_NEWER), mockPost(DEFAULT_TIMESTAMP_NEW), mockPost(DEFAULT_TIMESTAMP_OLD));
        when(mUnauthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(posts));

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        assertThat(emittedElements.size(), equalTo(2));
        verify(mUnauthenticatedRedditService).latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER);

        verifyZeroInteractions(mImageDownloader);
        verifyZeroInteractions(mAuthenticatedRedditService);
        verify(mAuthenticatedRedditService, times(0)).unreadMessages();

        verify(mUserStorage).getSubreddits();
        verify(mUserStorage).getNumberToRequest();
        verify(mUserStorage).getSortType();

        verify(mUserStorage).setSeenTimestamp(DEFAULT_TIMESTAMP_NEW);
        verify(mUserStorage).setSeenTimestamp(DEFAULT_TIMESTAMP_NEWER);
        verify(mUserStorage, times(0)).setSeenTimestamp(DEFAULT_TIMESTAMP);
        verify(mUserStorage, times(0)).setSeenTimestamp(DEFAULT_TIMESTAMP_OLD);
    }

    @Test public void latestPostsWithThumbnail_triesToDownloadThumbnail() {
        when(mPost.getImageUrl()).thenReturn(DEFAULT_THUMBNAIL_URL);
        when(mPost.hasImageUrl()).thenReturn(true);

        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe();

        verify(mImageDownloader).downloadImage(DEFAULT_THUMBNAIL_URL);
    }

    @Test public void latestPostsWithHighResDownloadingOn_triesToDownloadImage() {
        when(mPost.hasImageUrl()).thenReturn(true);
        when(mPost.getImageUrl()).thenReturn(DEFAULT_IMAGE_URL);
        when(mUserStorage.downloadFullSizedImages()).thenReturn(true);

        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe();

        verify(mImageDownloader).downloadImage(DEFAULT_IMAGE_URL);
    }

    @Test public void latestPostsWithHighResDownloadingOn_withoutImageFileExtension_doesNotDownloadImage() {
        when(mPost.getUrl()).thenReturn(DEFAULT_IMAGE_URL_NO_FILE_EXTENSION);
        when(mUserStorage.downloadFullSizedImages()).thenReturn(true);

        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe();

        verifyZeroInteractions(mImageDownloader);
    }

    @Test public void loggedInButMessagesNotEnabled_emitsPostAndDoesntTryToRetrieveMessages() {
        when(mAuthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(Arrays.asList(mPost)));
        when(mTokenStorage.isLoggedIn()).thenReturn(true);

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        verify(mAuthenticatedRedditService, times(0)).unreadMessages();
        assertThat(emittedElements.size(), equalTo(1));
    }

    @Test public void loggedInAndMessagesEnabled_triesToRetrieveMessagesAndMarksAsRead() {
        when(mAuthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(Arrays.asList(mPost)));

        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final Post directMessage = mockDirectMessage(DEFAULT_TIMESTAMP);
        final Observable<List<Post>> observable = Observable.just(Arrays.asList(directMessage));
        when(mAuthenticatedRedditService.unreadMessages()).thenReturn(observable);

        final MarkAllRead markAllRead = mock(MarkAllRead.class);
        when(markAllRead.hasErrors()).thenReturn(false);
        when(mAuthenticatedRedditService.markAllMessagesRead()).thenReturn(markAllRead);

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        // assert we emit both posts and messages
        assertThat(emittedElements.size(), equalTo(2));
        assertThat(emittedElements.get(0).getPost(), equalTo(mPost));
        assertThat(emittedElements.get(1).getPost(), equalTo(directMessage));

        verify(mAuthenticatedRedditService).unreadMessages();
        verify(mAuthenticatedRedditService).latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER);
    }

    @Test public void markAsReadFails_emitsOnlyPosts() throws Exception {
        when(mAuthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(Arrays.asList(mPost)));

        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final Observable<List<Post>> observable = Observable.just(Arrays.asList(mockDirectMessage(DEFAULT_TIMESTAMP)));
        when(mAuthenticatedRedditService.unreadMessages()).thenReturn(observable);

        final RetrofitError networkError = RetrofitError.networkError("Network error", mock(IOException.class));
        when(mAuthenticatedRedditService.markAllMessagesRead()).thenThrow(networkError);

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        assertThat(emittedElements.size(), equalTo(1));
        assertThat(emittedElements.get(0).getPost(), equalTo(mPost));
    }

    @Test
    public void retrievingPostsFailsButSucceedsRetrievingMessages_stillEmitsMessages() throws InterruptedException {
        final RetrofitError networkError = RetrofitError.networkError("Network error", mock(IOException.class));
        when(mAuthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenThrow(networkError);

        final Post directMessage = mockDirectMessage(DEFAULT_TIMESTAMP);
        final Observable<List<Post>> messagesObservable = Observable.just(Arrays.asList(directMessage));
        when(mAuthenticatedRedditService.unreadMessages()).thenReturn(messagesObservable);

        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final MarkAllRead markAllRead = mock(MarkAllRead.class);
        when(markAllRead.hasErrors()).thenReturn(false);
        when(mAuthenticatedRedditService.markAllMessagesRead()).thenReturn(markAllRead);

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve()
                .observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
                    @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                        emittedElements.addAll(postAndImages);
                    }
                });

        assertThat(emittedElements.size(), equalTo(1));
        assertThat(emittedElements.get(0).getPost(), equalTo(directMessage));
    }

    @Test public void retrievingMessageFailsButSucceedsRetrievingPosts_stillEmitsPosts() {
        when(mAuthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(Arrays.asList(mPost)));

        when(mTokenStorage.isLoggedIn()).thenReturn(true);
        when(mUserStorage.messagesEnabled()).thenReturn(true);

        final RetrofitError networkError = RetrofitError.networkError("Network error", mock(IOException.class));
        when(mAuthenticatedRedditService.unreadMessages()).thenThrow(networkError);

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        assertThat(emittedElements.size(), equalTo(1));
        assertThat(emittedElements.get(0).getPost(), equalTo(mPost));

        verify(mAuthenticatedRedditService).unreadMessages();
        verify(mAuthenticatedRedditService).latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER);
    }

    @Test public void imageDownloadFailsOnOnePost_stillEmitsTheOthers() throws Exception {
        final Post post0 = mockPost(DEFAULT_TIMESTAMP_NEWER);
        when(post0.hasImageUrl()).thenReturn(true);
        when(post0.getImageUrl()).thenReturn(DEFAULT_IMAGE_URL);

        final Throwable error = mock(RuntimeException.class);
        when(mImageDownloader.downloadImage(DEFAULT_IMAGE_URL)).thenThrow(error);

        final Post post1 = mockPost(DEFAULT_TIMESTAMP_NEWER);
        final Post post2 = mockPost(DEFAULT_TIMESTAMP_NEWER);

        final List<Post> posts = Arrays.asList(post0, post1, post2);
        when(mUnauthenticatedRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(posts));

        final List<LatestPostsRetriever.PostAndImage> emittedElements = new ArrayList<>();
        mRetriever.retrieve().observeOn(Schedulers.immediate()).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
            @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                emittedElements.addAll(postAndImages);
            }
        });

        assertThat(emittedElements.size(), equalTo(2));
        assertThat(emittedElements.get(0).getPost(), equalTo(post1));
        assertThat(emittedElements.get(1).getPost(), equalTo(post2));
    }
}
