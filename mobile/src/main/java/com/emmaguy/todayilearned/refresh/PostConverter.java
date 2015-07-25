package com.emmaguy.todayilearned.refresh;

import android.content.res.Resources;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.StringUtils;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

class PostConverter implements Converter {
    private final GsonConverter mOriginalConverter;
    private final UserStorage mUserStorage;
    private final Resources mResources;

    PostConverter(GsonConverter gsonConverter, Resources resources, UserStorage userStorage) {
        mOriginalConverter = gsonConverter;
        mResources = resources;
        mUserStorage = userStorage;
    }

    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        if (!isPostList(type)) {
            return mOriginalConverter.fromBody(body, type);
        }

        ListingResponse listingResponse = (ListingResponse) mOriginalConverter.fromBody(body, ListingResponse.class);
        List<Post> result = new ArrayList<>();
        if (listingResponse == null || listingResponse.getData() == null || listingResponse.getData().getChildren() == null ||
                listingResponse.getData().getChildren().isEmpty()) {
            return result;
        }

        for (PostResponse response : listingResponse.getData().getChildren()) {
            final PostResponse.Data data = response.getData();
            if (data.isStickied()) {
                continue;
            }

            final boolean isDirectMessage = data.getName().startsWith("t4");

            final String title = StringUtils.isEmpty(data.getTitle()) ? "" : data.getTitle().trim();
            final String description = getDescription(data);
            final String subreddit = data.getSubreddit();
            final String imageUrl = getImageUrl(data);
            final String author = data.getAuthor();

            result.add(new Post.Builder()
                    .setTitle(isDirectMessage ? mResources.getString(R.string.message_from_x, author) : subreddit)
                    .setShortTitle(isDirectMessage ? getShortDescription(description) : getShortString(title))
                    .setSubreddit(subreddit)
                    .setPermalink(isDirectMessage ? "/message/messages/" + data.getId() : data.getPermalink())
                    .setPostContents(getPostContents(isDirectMessage, title, description))
                    .setCreatedUtc(getCreatedUtc(data.getCreatedUtc()))
                    .setImageUrl(imageUrl)
                    .setGilded(data.getGilded())
                    .setFullname(data.getName())
                    .setUrl(data.getUrl())
                    .setId(data.getId())
                    .setAuthor(data.getAuthor())
                    .setScore(data.getScore())
                    .hasImageUrl(!StringUtils.isEmpty(imageUrl))
                    .setIsDirectMessage(isDirectMessage)
                    .setIsScoreHidden(data.isScoreHidden())
                    .build());
        }

        return result;
    }

    private String getDescription(PostResponse.Data data) {
        final String subjectAndBody = data.getSubject() + "\n" + data.getBody();
        final String description = StringUtils.isEmpty(data.getSelfText()) ? subjectAndBody : data.getSelfText();
        return StringUtils.isEmpty(description) ? "" : description.trim();
    }

    // Sometimes the api returns invalid longs, e.g. 1420079792.0
    private long getCreatedUtc(String createdUtc) {
        if (StringUtils.isEmpty(createdUtc)) {
            return 0;
        }

        if (createdUtc.endsWith(".0")) {
            createdUtc = createdUtc.replace(".0", "");
        }

        return Long.valueOf(createdUtc);
    }

    private String getImageUrl(PostResponse.Data data) {
        // TODO: use 'Preview' from json
        String imageUrl = StringUtils.isEmpty(data.getThumbnail()) ? "" : data.getThumbnail().trim(); // TODO: what happens without default?
        if (StringUtils.isEmpty(imageUrl) || imageUrl.equals("default") || imageUrl.equals("nsfw") || imageUrl.equals("self")) {
            return "";
        }

        if (data.getMedia() != null && data.getMedia().getOembed() != null) {
            imageUrl = data.getMedia().getOembed().getThumbnailUrl();
        }
        // If user has chosen to get full images, only do so if we actually have a image url, else fallback to thumbnail
        if (mUserStorage.downloadFullSizedImages()) {
            final String url = data.getUrl();
            if (isImage(url)) {
                imageUrl = url;
            } else if (url.contains("imgur.com") && !url.endsWith(".jpg")) {
                // Some imgur urls submitted don't end with .jpg but are jpgs, e.g. http://imgur.com/iBYHBA3
                imageUrl = url + ".jpg";
            }
        }

        return imageUrl;
    }

    private boolean isImage(String url) {
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg");
    }

    private boolean isPostList(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return parameterizedType.getActualTypeArguments()[0] == Post.class;
        }
        return false;
    }

    private String getShortString(String string) {
        if (string.length() < 15) {
            return string;
        }
        return string.substring(0, 12) + "...";
    }

    public String getShortDescription(String description) {
        if (description.contains("\n")) {
            String title = description.substring(0, description.indexOf("\n"));
            return getShortString(title);
        }
        return getShortString(description);
    }

    public String getPostContents(boolean isDirectMessage, String title, String description) {
        if (isDirectMessage) {
            return description;
        }

        if (StringUtils.isEmpty(title)) {
            return description;
        }

        if (StringUtils.isEmpty(description)) {
            return title;
        }

        return title + "\n\n" + description;
    }

    @Override
    public TypedOutput toBody(Object object) {
        return mOriginalConverter.toBody(object);
    }
}
