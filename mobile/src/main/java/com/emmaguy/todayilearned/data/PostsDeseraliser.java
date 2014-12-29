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

public class PostsDeseraliser implements JsonDeserializer<List<Post>> {
    @Override
    public List<Post> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Logger.Log(json.toString());

        List<Post> l = new ArrayList<Post>();
        if (json.isJsonObject()) {
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
                    l.add(new Post(title,
                            getEmptyStringOrValue(data, "subreddit"),
                            TextUtils.isEmpty(selftext) ? getEmptyStringOrValue(data, "subject") + "\n" + getEmptyStringOrValue(data, "body") : selftext,
                            getEmptyStringOrValue(data, "name"),
                            getEmptyStringOrValue(data, "permalink"),
                            getEmptyStringOrValue(data, "author"),
                            getEmptyStringOrValue(data, "id"),
                            getEmptyStringOrValue(data, "thumbnail"),
                            data.get("created_utc").getAsLong()));
                }
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