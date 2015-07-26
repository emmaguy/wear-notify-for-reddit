package com.emmaguy.todayilearned.refresh;

import android.content.res.Resources;

import com.emmaguy.todayilearned.TestUtils;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CommentsConverterTest {
    private final Gson mGson = new Gson();
    private final GsonConverter mGsonConverter = new GsonConverter(mGson);

    @Mock UserStorage mUserStorage;
    @Mock Resources mResources;

    @Before public void before() throws Exception {
        initMocks(this);
    }

    @Test public void parsesToPostsSuccessfully() throws Exception {
        List<Post> posts = convertComments("comment-default.json");

        assertThat(posts.size(), equalTo(2));

        assertThat(posts.get(0).getPostContents(), equalTo("first reply lol"));
        assertThat(posts.get(0).getReplyLevel(), equalTo(1));
        assertThat(posts.get(0).getScore(), equalTo(1));
        assertThat(posts.get(0).getReplies().size(), equalTo(1));
        assertThat(posts.get(0).getReplies().get(0).getPostContents(), equalTo("reply to first reply"));
        assertThat(posts.get(0).getReplies().get(0).getReplyLevel(), equalTo(2));

        assertThat(posts.get(1).getPostContents(), equalTo("second reply"));
        assertThat(posts.get(1).getReplyLevel(), equalTo(1));
    }

    private List<Post> convertComments(String filename) throws IOException, ConversionException {
        final TypedInput body = mock(TypedInput.class);
        when(body.in()).thenReturn(TestUtils.loadFileFromStream(filename));

        final CommentsConverter commentsConverter = new CommentsConverter(mGson, mGsonConverter, mResources, mUserStorage);
        final List<Post> posts = (List<Post>) commentsConverter.fromBody(body, new ParameterizedType() {
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

        return posts;
    }
}
