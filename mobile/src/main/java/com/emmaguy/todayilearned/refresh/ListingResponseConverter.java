package com.emmaguy.todayilearned.refresh;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.common.StringUtils;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by emma on 25/07/15.
 */
class ListingResponseConverter {
    private final UserStorage mUserStorage;
    private final HtmlDecoder mHtmlDecoder;
    private final Resources mResources;

    ListingResponseConverter(UserStorage userStorage, Resources resources,
                             HtmlDecoder htmlDecoder) {
        mUserStorage = userStorage;
        mResources = resources;
        mHtmlDecoder = htmlDecoder;
    }

    @NonNull public List<Post> convert(ListingResponse listingResponse) {
        final List<Post> result = new ArrayList<>();
        if (listingResponse == null || listingResponse.getData() == null ||
                listingResponse.getData().getChildren() == null || listingResponse.getData()
                .getChildren()
                .isEmpty()) {
            return result;
        }

        for (PostResponse response : listingResponse.getData().getChildren()) {
            final PostResponse.Data data = response.getData();
            if (data.isStickied() || data.getName().equals("more")) {
                continue;
            }

            final boolean isDirectMessage = data.getName().startsWith("t4");
            final String title = StringUtils.isEmpty(data.getTitle()) ? "" : data.getTitle().trim();
            final String description = getDescription(data);
            final String subreddit = data.getSubreddit();
            final String imageUrl = getImageUrl(data);
            final String author = data.getAuthor();

            result.add(new Post.Builder().setTitle(isDirectMessage ? mResources.getString(R.string.message_from_x,
                    author) : subreddit)
                    .setShortTitle(isDirectMessage ? getShortDescription(description) : getShortString(
                            title))
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
        String imageUrl = StringUtils.isEmpty(data.getThumbnail()) ? "" : data.getThumbnail()
                .trim();
        if (imageUrl.equals("default") || imageUrl.equals("nsfw") || imageUrl.equals("self")) {
            return "";
        }

        if (data.getMedia() != null && data.getMedia().getOembed() != null) {
            imageUrl = data.getMedia().getOembed().getThumbnailUrl();
        }

        if (data.getPreview() != null) {
            if (data.getPreview().getImages() != null && !data.getPreview().getImages().isEmpty()) {
                final PostResponse.Data.Image image = data.getPreview().getImages().get(0);
                if (image.getResolutions() != null && !image.getResolutions().isEmpty()) {
                    imageUrl = mHtmlDecoder.decode(image.getResolutions().get(0).getUrl());

                    if (mUserStorage.downloadFullSizedImages() && image.getResolutions()
                            .size() >= 2) {
                        imageUrl = mHtmlDecoder.decode(image.getResolutions()
                                .get(image.getResolutions().size() - 1)
                                .getUrl());
                    }
                }
            }
        }

        return imageUrl;
    }

    private String getShortString(String string) {
        if (string.length() < 15) {
            return string;
        }
        return string.substring(0, 12) + "...";
    }

    private String getShortDescription(String description) {
        if (description.contains("\n")) {
            String title = description.substring(0, description.indexOf("\n"));
            return getShortString(title);
        }
        return getShortString(description);
    }

    private String getPostContents(boolean isDirectMessage, String title, String description) {
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
}
