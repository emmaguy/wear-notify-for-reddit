package com.emmaguy.todayilearned.refresh;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

class RedditResponse {
    private boolean mHasErrors;
    private String mErrors;

    public boolean hasErrors() {
        return mHasErrors;
    }

    @Override public String toString() {
        return "Response has errors: " + mHasErrors + " " + mErrors;
    }

    private void setErrors(String errors) {
        mHasErrors = true;
        mErrors = errors;

        // TODO: handle errors
        // if BAD_CAPTCHA user needs to verify their account on web first
    }

    static class CommentResponseJsonDeserializer implements JsonDeserializer<RedditResponse> {
        @Override public RedditResponse deserialize(JsonElement json, Type typeOfT,
                                                    JsonDeserializationContext context) throws
                JsonParseException {
            JsonObject jsonContents = json.getAsJsonObject().get("json").getAsJsonObject();
            JsonArray errors = jsonContents.get("errors").getAsJsonArray();

//            Logger.log("CommentResponse json: " + json);
            RedditResponse response = new RedditResponse();

            if (errors.size() > 0) {
                response.setErrors(errors.toString());
            }

            return response;
        }
    }
}
