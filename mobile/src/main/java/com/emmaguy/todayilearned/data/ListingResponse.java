package com.emmaguy.todayilearned.data;

import android.text.TextUtils;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListingResponse {
    private List<Post> mPosts = new ArrayList<Post>();

    public void addPost(Post post) {
        mPosts.add(post);
    }

    public Iterable<? extends Post> getPosts() {
        return mPosts;
    }

    public static class ListingJsonDeserializer implements JsonDeserializer<ListingResponse> {
        @Override
        public ListingResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ListingResponse l = new ListingResponse();
            JsonObject dataObject = json.getAsJsonObject().get("data").getAsJsonObject();
            for (JsonElement e : dataObject.get("children").getAsJsonArray()) {
                JsonObject data = e.getAsJsonObject().get("data").getAsJsonObject();

                boolean stickied = false;
                if (data.has("stickied")) {
                    stickied = data.get("stickied").getAsBoolean();
                }
                if (!stickied) {
                    String title = getEmptyStringOrValue(data, "title");
                    String selftext = getEmptyStringOrValue(data, "selftext");
                    l.addPost(new Post(title,
                            getEmptyStringOrValue(data, "subreddit"),
                            TextUtils.isEmpty(selftext) ? getEmptyStringOrValue(data, "subject") + "\n" + getEmptyStringOrValue(data, "body") : selftext,
                            data.get("name").getAsString(),
                            getEmptyStringOrValue(data, "permalink"),
                            data.get("author").getAsString(),
                            data.get("created_utc").getAsLong()));
                }
            }

            return l;
        }

        private String getEmptyStringOrValue(JsonObject data, String key) {
            String value = "";
            if (data.has(key)) {
                JsonElement element = data.get(key);
                if (element != null && !element.isJsonNull()) {
                    value = element.getAsString().trim();
                }
            }
            return value;
        }
    }
}
