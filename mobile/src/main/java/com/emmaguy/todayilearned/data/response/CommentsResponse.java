package com.emmaguy.todayilearned.data.response;

import com.emmaguy.todayilearned.data.model.PostsDeserialiser;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.List;

public class CommentsResponse {
    public static class CommentsResponseJsonDeserialiser implements JsonDeserializer<List<Post>> {
        @Override
        public List<Post> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            PostsDeserialiser p = new PostsDeserialiser();

            // Reddit api appears to return 2 listings for the comment request - 1, the article, 2 the comments. We want just the comments
            JsonArray listingResponses = json.getAsJsonArray();
            JsonElement json1 = listingResponses.get(1);

            return p.deserialize(json1, typeOfT, context);
        }
    }
}
