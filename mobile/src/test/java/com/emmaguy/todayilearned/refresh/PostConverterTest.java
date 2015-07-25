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

public class PostConverterTest {
    private final GsonConverter mGsonConverter = new GsonConverter(new Gson());

    @Mock UserStorage mUserStorage;
    @Mock Resources mResources;

    @Before public void before() throws Exception {
        initMocks(this);
    }

    @Test public void parsesToPostSuccessfully_whenIsPost() throws Exception {
        Post post = convertPostResponse("post-default.json");

        assertThat(post.isDirectMessage(), equalTo(false));
        assertThat(post.getPermalink(), equalTo("/r/todayilearned/comments/3eiq9r/til_that_the_smokey_bear_effect_is_a_bad_thing/"));
        assertThat(post.getFullname(), equalTo("t3_3eiq9r"));
        assertThat(post.getCreatedUtc(), equalTo(1437791472l));
        assertThat(post.getAuthor(), equalTo("FriendlyXenomorph"));
        assertThat(post.getTitle(), equalTo("todayilearned"));
        assertThat(post.getShortTitle(), equalTo("TIL that The..."));
        assertThat(post.getGilded(), equalTo(0));
        assertThat(post.getScore(), equalTo(2858));
        assertThat(post.hasImageUrl(), equalTo(true));
        assertThat(post.getImageUrl(), equalTo("http://b.thumbs.redditmedia.com/FjpoyGMzecJTly92C7goT72YSSRG5voENhCu-McchIo.jpg"));
        assertThat(post.getPostContents(), equalTo("TIL that The Smokey Bear Effect is a bad thing, decades of preventing small fires leads to the accumulation of undergrowth, that fuels massive superfires today"));
    }

    @Test public void whenUserDoesntEnableHighResImages_postImageUrl_isHighRes() throws Exception {
        Post post = convertPostResponse("post-url-is-image.json");

        assertThat(post.hasImageUrl(), equalTo(true));
        assertThat(post.getImageUrl(), equalTo("http://b.thumbs.redditmedia.com/09H4Gb80XZmwWw5fRqXCwxSAaNxoBycel5kdyX2czsQ.jpg"));
    }

    @Test public void whenUserEnablesHighResImages_postUrlIsImgurWithoutJpgExtension_appendJpgAndUse() throws Exception {
        when(mUserStorage.downloadFullSizedImages()).thenReturn(true);

        Post post = convertPostResponse("post-url-is-imgur-blank.json");

        assertThat(post.hasImageUrl(), equalTo(true));
        assertThat(post.getUrl(), equalTo("http://imgur.com/OWSVzoc"));
        assertThat(post.getImageUrl(), equalTo("http://imgur.com/OWSVzoc.jpg"));
    }

    @Test public void whenUserEnablesHighResImages_postImageUrl_isHighRes() throws Exception {
        when(mUserStorage.downloadFullSizedImages()).thenReturn(true);

        Post post = convertPostResponse("post-url-is-image.json");

        assertThat(post.hasImageUrl(), equalTo(true));
        assertThat(post.getImageUrl(), equalTo(post.getUrl()));
        assertThat(post.getImageUrl(), equalTo("http://i.imgur.com/7qVju5e.jpg"));
    }

    @Test public void parsesToPostSuccessfully_whenIsDirectMessage() throws Exception {
        Post post = convertPostResponse("post-direct-message.json");

        assertThat(post.isDirectMessage(), equalTo(true));
        assertThat(post.getPermalink(), equalTo("/message/messages/3i88bo"));
        assertThat(post.getFullname(), equalTo("t4_3i88bo"));
        assertThat(post.getCreatedUtc(), equalTo(1432572755l));
        assertThat(post.getAuthor(), equalTo("an-author"));
        assertThat(post.getPostContents(), equalTo("subject-of-a-direct-message\na-direct-message"));
    }

    private Post convertPostResponse(String filename) throws IOException, ConversionException {
        final TypedInput body = mock(TypedInput.class);
        when(body.in()).thenReturn(TestUtils.loadFileFromStream(filename));

        final PostConverter postConverter = new PostConverter(mGsonConverter, mResources, mUserStorage);
        final List<Post> posts = (List<Post>) postConverter.fromBody(body, new ParameterizedType() {
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

        assertThat(posts.size(), equalTo(1));
        return posts.get(0);
    }
}
