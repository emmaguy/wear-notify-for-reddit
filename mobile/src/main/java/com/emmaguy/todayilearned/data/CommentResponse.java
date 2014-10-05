package com.emmaguy.todayilearned.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class CommentResponse {
    public static class CommentResponseJsonDeserializer implements JsonDeserializer<CommentResponse> {
        @Override
        public CommentResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonContents = json.getAsJsonObject().get("json").getAsJsonObject();
            JsonArray errors = jsonContents.get("errors").getAsJsonArray();

            if (errors.size() <= 0) {
                return new CommentResponse();
            }

            // TODO: actually parse and do something with the errors
            return null;
        }
    }
}
