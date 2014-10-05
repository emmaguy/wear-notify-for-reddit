package com.emmaguy.todayilearned.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionResponse {
    private List<String> mSubreddits = new ArrayList<String>();

    public List<String> getSubreddits() {
        return mSubreddits;
    }

    private void addSubreddit(String subreddit) {
        mSubreddits.add(subreddit);
    }

    public static class SubscriptionResponseJsonDeserializer implements JsonDeserializer<SubscriptionResponse> {
        @Override
        public SubscriptionResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            SubscriptionResponse l = new SubscriptionResponse();

            JsonObject dataObject = json.getAsJsonObject().get("data").getAsJsonObject();
            for (JsonElement e : dataObject.get("children").getAsJsonArray()) {
                JsonObject data = e.getAsJsonObject().get("data").getAsJsonObject();

                String subreddit = data.get("display_name").getAsString();
                l.addSubreddit(subreddit);
            }

            return l;
        }
    }
}
