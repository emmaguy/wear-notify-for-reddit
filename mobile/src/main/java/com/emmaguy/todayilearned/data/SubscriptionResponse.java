package com.emmaguy.todayilearned.data;

import com.google.gson.JsonArray;
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
    private boolean mHasErrors;
    private String mErrors;

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

            JsonObject jsonContents = json.getAsJsonObject();
            if (jsonContents.has("data")) {
                JsonObject dataObject = jsonContents.get("data").getAsJsonObject();
                for (JsonElement e : dataObject.get("children").getAsJsonArray()) {
                    JsonObject data = e.getAsJsonObject().get("data").getAsJsonObject();

                    String subreddit = data.get("display_name").getAsString();
                    l.addSubreddit(subreddit);
                }
            }

            if (jsonContents.has("errors")) {
                JsonArray errors = jsonContents.get("errors").getAsJsonArray();
                if (errors.size() > 0) {
                    l.setErrors(errors.toString());
                }
            }

            return l;
        }
    }

    @Override
    public String toString() {
        return "SubscriptionResponse has errors: " + mHasErrors + " " + mErrors;
    }

    public boolean hasErrors() {
        return mHasErrors;
    }

    private void setErrors(String errors) {
        mHasErrors = true;
        mErrors = errors;
    }
}
