package com.emmaguy.todayilearned.data;

import android.text.TextUtils;

import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PostsDeserialiser implements JsonDeserializer<List<Post>> {
    @Override
    public List<Post> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Post> l = new ArrayList<Post>();
        if (json.isJsonObject()) {
            JsonObject dataObject = json.getAsJsonObject().get("data").getAsJsonObject();
            for (JsonElement e : dataObject.get("children").getAsJsonArray()) {
                String kind = e.getAsJsonObject().get("kind").getAsString();
                if(!kind.equals("t1") && !kind.equals("t3")) {
                    // only allow comments or links
                    continue;
                }

                JsonObject data = e.getAsJsonObject().get("data").getAsJsonObject();

                boolean stickied = false;
                if (data.has("stickied")) {
                    stickied = data.get("stickied").getAsBoolean();
                }
                if (!stickied) {
                    String title = getEmptyStringOrValue(data, "title");
                    String selftext = getEmptyStringOrValue(data, "selftext");
                    Post post = new Post(title,
                            getEmptyStringOrValue(data, "subreddit"),
                            TextUtils.isEmpty(selftext) ? getEmptyStringOrValue(data, "subject") + "\n" + getEmptyStringOrValue(data, "body") : selftext,
                            getEmptyStringOrValue(data, "name"),
                            getEmptyStringOrValue(data, "permalink"),
                            getEmptyStringOrValue(data, "author"),
                            getEmptyStringOrValue(data, "id"),
                            getEmptyStringOrValue(data, "thumbnail"),
                            getAsLong(getEmptyStringOrValue(data, "created_utc")),
                            getAsLong(getEmptyStringOrValue(data, "ups")),
                            getAsLong(getEmptyStringOrValue(data, "downs")));

                    Logger.Log("json " + data);
                    if(data.has("replies") && data.get("replies").isJsonObject()) {
                        List<Post> replies = deserialize(data.get("replies").getAsJsonObject(), typeOfT, context);
                        Logger.Log("replies " + replies.size());
                        Logger.Log("reply 0: " + replies.get(0).getDescription());
                        post.setReplies(replies);
                    }

                    l.add(post);
                }
            }
        }

        return l;
    }

    // Sometimes the api returns invalid longs, e.g. 1420079792.0
    private long getAsLong(String longValue) {
        if (TextUtils.isEmpty(longValue)) {
            return 0;
        }

        if (longValue.endsWith(".0")) {
            longValue = longValue.replace(".0", "");
        }

        return Long.valueOf(longValue);
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