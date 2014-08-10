package com.emmaguy.todayilearned.data;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Listing {
    private List<Post> mPosts = new ArrayList<Post>();

    public void addPost(Post post) {
        mPosts.add(post);
    }

    public Iterable<? extends Post> getPosts() {
        return mPosts;
    }

    public static class ListingJsonDeserializer implements JsonDeserializer<Listing> {
        @Override
        public Listing deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Listing l = new Listing();
            JsonObject dataObject = json.getAsJsonObject().get("data").getAsJsonObject();
            for (JsonElement e : dataObject.get("children").getAsJsonArray()) {
                JsonObject data = e.getAsJsonObject().get("data").getAsJsonObject();
                boolean stickied = data.get("stickied").getAsBoolean();
                if (!stickied) {
                    l.addPost(new Post(data.get("title").getAsString(), data.get("subreddit").getAsString(), data.get("permalink").getAsString(), data.get("created_utc").getAsLong()));
                }
            }

            return l;
        }
    }
}
