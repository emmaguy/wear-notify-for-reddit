package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.TestUtils;
import com.emmaguy.todayilearned.settings.SubscriptionResponse;
import com.emmaguy.todayilearned.sharedlib.Comment;
import com.emmaguy.todayilearned.sharedlib.Post;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DelegatingConverterTest {
    @Mock SubscriptionConverter mSubscriptionConverter;
    @Mock MarkAsReadConverter mMarkAsReadConverter;
    @Mock CommentsConverter mCommentsConverter;
    @Mock TokenConverter mTokenConverter;
    @Mock PostConverter mPostConverter;
    @Mock Converter mConverter;

    @Mock TypedInput mTypedInput;

    @Before public void before() throws Exception {
        initMocks(this);
    }

    private Type getListOfType(final Type desiredType) {
        return new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() {
                return new Type[]{desiredType};
            }

            @Override public Type getOwnerType() {
                return null;
            }

            @Override public Type getRawType() {
                return List.class;
            }
        };
    }

    @Test public void usesPostConverter_whenParsingPosts() throws Exception {
        final Type listOfPostsType = getListOfType(Post.class);
        convertResponse("post-default.json", listOfPostsType);

        verifyZeroInteractions(mSubscriptionConverter);
        verifyZeroInteractions(mMarkAsReadConverter);
        verifyZeroInteractions(mCommentsConverter);
        verifyZeroInteractions(mTokenConverter);
        verifyZeroInteractions(mConverter);

        verify(mPostConverter).fromBody(mTypedInput, listOfPostsType);
    }

    @Test public void usesCommentsConverter_whenParsingComments() throws Exception {
        final Type listOfPostsType = getListOfType(Comment.class);
        convertResponse("comments-default.json", listOfPostsType);

        verifyZeroInteractions(mSubscriptionConverter);
        verifyZeroInteractions(mMarkAsReadConverter);
        verifyZeroInteractions(mTokenConverter);
        verifyZeroInteractions(mPostConverter);
        verifyZeroInteractions(mConverter);

        verify(mCommentsConverter).fromBody(mTypedInput, listOfPostsType);
    }

    @Test public void usesPostConverter_whenParsingDirectMessages() throws Exception {
        final Type listOfPostsType = getListOfType(Post.class);
        convertResponse("post-direct-message.json", listOfPostsType);

        verifyZeroInteractions(mSubscriptionConverter);
        verifyZeroInteractions(mMarkAsReadConverter);
        verifyZeroInteractions(mCommentsConverter);
        verifyZeroInteractions(mTokenConverter);
        verifyZeroInteractions(mConverter);

        verify(mPostConverter).fromBody(mTypedInput, listOfPostsType);
    }

    @Test public void usesPostConverter_whenParsingMarkAsRead() throws Exception {
        final Type type = MarkAllRead.class;
        convertResponse("markasread", type);

        verifyZeroInteractions(mSubscriptionConverter);
        verifyZeroInteractions(mCommentsConverter);
        verifyZeroInteractions(mTokenConverter);
        verifyZeroInteractions(mPostConverter);
        verifyZeroInteractions(mConverter);

        verify(mMarkAsReadConverter).fromBody(mTypedInput, type);
    }

    @Test public void usesPostConverter_whenParsingTokenRetrieve() throws Exception {
        final Type type = Token.class;
        convertResponse("token-retrieve.json", type);

        verifyZeroInteractions(mSubscriptionConverter);
        verifyZeroInteractions(mMarkAsReadConverter);
        verifyZeroInteractions(mCommentsConverter);
        verifyZeroInteractions(mPostConverter);
        verifyZeroInteractions(mConverter);

        verify(mTokenConverter).fromBody(mTypedInput, type);
    }

    @Test public void usesPostConverter_whenParsingTokenRefresh() throws Exception {
        final Type type = Token.class;
        convertResponse("token-refresh.json", type);

        verifyZeroInteractions(mSubscriptionConverter);
        verifyZeroInteractions(mMarkAsReadConverter);
        verifyZeroInteractions(mCommentsConverter);
        verifyZeroInteractions(mPostConverter);
        verifyZeroInteractions(mConverter);

        verify(mTokenConverter).fromBody(mTypedInput, type);
    }

    @Test public void usesPostConverter_whenParsingSubscriptionSync() throws Exception {
        final Type type = SubscriptionResponse.class;
        convertResponse("subscriptions-sync.json", type);

        verifyZeroInteractions(mMarkAsReadConverter);
        verifyZeroInteractions(mCommentsConverter);
        verifyZeroInteractions(mTokenConverter);
        verifyZeroInteractions(mPostConverter);
        verifyZeroInteractions(mConverter);

        verify(mSubscriptionConverter).fromBody(mTypedInput, type);
    }

    private void convertResponse(String filename, Type type) throws IOException,
            ConversionException {
        when(mTypedInput.in()).thenReturn(TestUtils.loadFileFromStream(filename));

        new DelegatingConverter(mConverter,
                mTokenConverter,
                mPostConverter,
                mMarkAsReadConverter,
                mSubscriptionConverter,
                mCommentsConverter).fromBody(mTypedInput, type);
    }
}
