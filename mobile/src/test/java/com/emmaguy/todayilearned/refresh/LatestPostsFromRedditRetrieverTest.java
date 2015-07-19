package com.emmaguy.todayilearned.refresh;

import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by emma on 19/07/15.
 */
public class LatestPostsFromRedditRetrieverTest {
    private static final String DEFAULT_IMAGE_URL_NO_FILE_EXTENSION = "http://nofileextension";
    private static final String DEFAULT_THUMBNAIL_URL = "http://anythumb";
    private static final String DEFAULT_IMAGE_URL = "http://anyurl.jpg";

    private static final String DEFAULT_SUBREDDIT = "todayilearned";
    private static final String DEFAULT_SORT = "hot";
    private static final int DEFAULT_NUMBER = 5;

    private static final long DEFAULT_TIMESTAMP_LARGER = 110;
    private static final long DEFAULT_TIMESTAMP_OLD = 90;
    private static final long DEFAULT_TIMESTAMP = 100;

    @Mock ImageDownloader mImageDownloader;
    @Mock RedditService mRedditService;
    @Mock UserStorage mUserStorage;

    private Post mPost;
    private List<Post> resultingPosts;

    @Before public void before() {
        initMocks(this);

        when(mUserStorage.isTimestampNewerThanStored(DEFAULT_TIMESTAMP)).thenReturn(true);
        when(mUserStorage.isTimestampNewerThanStored(DEFAULT_TIMESTAMP_OLD)).thenReturn(false);
        when(mUserStorage.isTimestampNewerThanStored(DEFAULT_TIMESTAMP_LARGER)).thenReturn(true);

        when(mUserStorage.getSortType()).thenReturn(DEFAULT_SORT);
        when(mUserStorage.getNumberToRequest()).thenReturn(DEFAULT_NUMBER);
        when(mUserStorage.getSubreddits()).thenReturn(DEFAULT_SUBREDDIT);

        when(mUserStorage.downloadFullSizedImages()).thenReturn(false);

        mPost = mockPost(DEFAULT_TIMESTAMP);
        when(mRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(Arrays.asList(mPost)));
    }

    @NonNull private Post mockPost(long timestamp) {
        final Post post = mock(Post.class);
        when(post.getUrl()).thenReturn(DEFAULT_IMAGE_URL);
        when(post.getCreatedUtc()).thenReturn(timestamp);
        when(post.hasThumbnail()).thenReturn(false);
        return post;
    }

    @Test public void latestPostsWith2ThatAreNew_savesBothTimestamps() {
        final List<Post> posts = Arrays.asList(mockPost(DEFAULT_TIMESTAMP), mockPost(DEFAULT_TIMESTAMP_OLD), mockPost(DEFAULT_TIMESTAMP_LARGER));
        when(mRedditService.latestPosts(DEFAULT_SUBREDDIT, DEFAULT_SORT, DEFAULT_NUMBER)).thenReturn(Observable.just(posts));

        LatestPostsFromRedditRetriever retriever = new LatestPostsFromRedditRetriever(mImageDownloader, mUserStorage);
        retriever.getPosts(mRedditService).subscribeOn(Schedulers.immediate()).subscribe(new Action1<List<Post>>() {
            @Override public void call(List<Post> posts) {
                resultingPosts = posts;
            }
        });

        assertThat(resultingPosts.size(), equalTo(2));

        verifyZeroInteractions(mImageDownloader);

        verify(mUserStorage).getSubreddits();
        verify(mUserStorage).getNumberToRequest();
        verify(mUserStorage).getSortType();
        verify(mUserStorage).isTimestampNewerThanStored(DEFAULT_TIMESTAMP);
        verify(mUserStorage).isTimestampNewerThanStored(DEFAULT_TIMESTAMP_LARGER);

        verify(mUserStorage).setSeenTimestamp(DEFAULT_TIMESTAMP);
        verify(mUserStorage).setSeenTimestamp(DEFAULT_TIMESTAMP_LARGER);
    }

    @Test public void latestPostsWithThumbnail_triesToDownloadThumbnail() {
        when(mPost.getThumbnail()).thenReturn(DEFAULT_THUMBNAIL_URL);
        when(mPost.hasThumbnail()).thenReturn(true);

        LatestPostsFromRedditRetriever retriever = new LatestPostsFromRedditRetriever(mImageDownloader, mUserStorage);
        retriever.getPosts(mRedditService).subscribeOn(Schedulers.immediate()).subscribe();

        verify(mImageDownloader).downloadImage(mPost, DEFAULT_THUMBNAIL_URL);
    }

    @Test public void latestPostsWithHighResDownloadingOn_triesToDownloadImage() {
        when(mUserStorage.downloadFullSizedImages()).thenReturn(true);

        LatestPostsFromRedditRetriever retriever = new LatestPostsFromRedditRetriever(mImageDownloader, mUserStorage);
        retriever.getPosts(mRedditService).subscribeOn(Schedulers.immediate()).subscribe();

        verify(mImageDownloader).downloadImage(mPost, DEFAULT_IMAGE_URL);
    }

    @Test public void latestPostsWithHighResDownloadingOn_withoutImageFileExtension_doesNotDownloadImage() {
        when(mPost.getUrl()).thenReturn(DEFAULT_IMAGE_URL_NO_FILE_EXTENSION);
        when(mUserStorage.downloadFullSizedImages()).thenReturn(true);

        LatestPostsFromRedditRetriever retriever = new LatestPostsFromRedditRetriever(mImageDownloader, mUserStorage);
        retriever.getPosts(mRedditService).subscribeOn(Schedulers.immediate()).subscribe();

        verifyZeroInteractions(mImageDownloader);
    }
}
