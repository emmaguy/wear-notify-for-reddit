package com.emmaguy.todayilearned.data.response;

import com.emmaguy.todayilearned.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class AddCommentResponse {
    private boolean mHasErrors;
    private String mErrors;

    public boolean hasErrors() {
        return mHasErrors;
    }

    @Override
    public String toString() {
        return "CommentResponse has errors: " + mHasErrors + " " + mErrors;
    }

    private void setErrors(String errors) {
        mHasErrors = true;
        mErrors = errors;

        // TODO: handle errors
        // if BAD_CAPTCHA user needs to verify their account on web first
    }

    public static class CommentResponseJsonDeserializer implements JsonDeserializer<AddCommentResponse> {
        @Override
        public AddCommentResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonContents = json.getAsJsonObject().get("json").getAsJsonObject();
            JsonArray errors = jsonContents.get("errors").getAsJsonArray();

            Logger.Log("CommentResponse json: " + json);
            AddCommentResponse response = new AddCommentResponse();

            if (errors.size() > 0) {
                response.setErrors(errors.toString());
            }

            return response;
        }
    }
}
