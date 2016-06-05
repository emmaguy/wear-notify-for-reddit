package com.emmaguy.todayilearned.refresh;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.common.StringUtils;
import com.emmaguy.todayilearned.sharedlib.Comment;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class CommentsConverter implements Converter {
    private final GsonConverter mOriginalConverter;
    private final UserStorage mUserStorage;
    private final Resources mResources;
    private final Gson mGson;

    public CommentsConverter(Gson gson, GsonConverter gsonConverter, Resources resources,
                             UserStorage userStorage) {
        mGson = gson;
        mOriginalConverter = gsonConverter;
        mResources = resources;
        mUserStorage = userStorage;
    }

    @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
        List<ListingResponse> responses = (List<ListingResponse>) mOriginalConverter.fromBody(body,
                new TypeToken<List<ListingResponse>>() {
                }.getType());

        // First child is always the post itself, second is all the comments
        final ListingResponse listingResponse = responses.get(1);
        return convert(listingResponse, 0);
    }

    @NonNull private List<Comment> convert(ListingResponse listingResponse, int level) {
        level++;

        final List<Comment> comments = new ArrayList<>();
        if (listingResponse == null || listingResponse.getData() == null ||
                listingResponse.getData().getChildren() == null || listingResponse.getData()
                .getChildren()
                .isEmpty()) {
            return comments;
        }

        for (PostResponse response : listingResponse.getData().getChildren()) {
            final PostResponse.Data data = response.getData();
            if (data.getName().equals("more")) {
                continue;
            }

            final boolean hasReplies = data.getReplies() != null && data.getReplies()
                    .isJsonObject();
            final List<Comment> childComments = hasReplies ? convert(mGson.fromJson(data.getReplies(),
                    ListingResponse.class), level) : null;

            final String title = StringUtils.isEmpty(data.getTitle()) ? "" : data.getTitle().trim();
            final String description = StringUtils.isEmpty(data.getBody()) ? "" : data.getBody()
                    .trim();

            if (StringUtils.isEmpty(title) && StringUtils.isEmpty(description)) {
                continue;
            }

            comments.add(new Comment.Builder().setComments(childComments)
                    .setAuthor(data.getAuthor())
                    .setPostContents(getPostContents(title, description))
                    .setIsScoreHidden(data.isScoreHidden())
                    .setScore(data.getScore())
                    .setGilded(data.getGilded())
                    .setReplyLevel(level)
                    .build());
        }

        return comments;
    }

    private String getPostContents(String title, String description) {
        if (StringUtils.isEmpty(title)) {
            return description;
        }

        if (StringUtils.isEmpty(description)) {
            return title;
        }

        return title + "\n\n" + description;
    }

    @Override public TypedOutput toBody(Object object) {
        return mOriginalConverter.toBody(object);
    }
}
