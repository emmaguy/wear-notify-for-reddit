package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.TestUtils;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DelegatingConverterTest {
    private final Converter mGsonConverter = new GsonConverter(new Gson());

    @Mock MarkAsReadConverter mMarkAsReadConverter;
    @Mock TokenConverter mTokenConverter;
    @Mock PostConverter mPostConverter;

    @Before public void before() throws Exception {
        initMocks(this);
    }

    @Test public void usesPostConverter_whenParsingPosts() throws Exception {
        convertResponse("post-default.json");

        verifyZeroInteractions(mMarkAsReadConverter);

        verify(mPostConverter).fromBody(any(TypedInput.class), any(Type.class));
    }

    @Test public void usesPostConverter_whenParsingComments() throws Exception {
        convertResponse("comments-default.json");

        verifyZeroInteractions(mMarkAsReadConverter);

        verify(mPostConverter).fromBody(any(TypedInput.class), any(Type.class));
    }

    @Test public void usesPostConverter_whenParsingDirectMessages() throws Exception {
        convertResponse("post-direct-message.json");

        verifyZeroInteractions(mMarkAsReadConverter);

        verify(mPostConverter).fromBody(any(TypedInput.class), any(Type.class));
    }

    private void convertResponse(String filename) throws IOException, ConversionException {
        final TypedInput body = mock(TypedInput.class);
        when(body.in()).thenReturn(TestUtils.loadFileFromStream(filename));

        final DelegatingConverter delegatingConverter = new DelegatingConverter(mGsonConverter, mTokenConverter, mPostConverter, mMarkAsReadConverter);
        delegatingConverter.fromBody(body, new ParameterizedType() {
            @Override public Type[] getActualTypeArguments() {
                return new Type[]{Post.class};
            }

            @Override public Type getOwnerType() {
                return null;
            }

            @Override public Type getRawType() {
                return List.class;
            }
        });
    }
}
