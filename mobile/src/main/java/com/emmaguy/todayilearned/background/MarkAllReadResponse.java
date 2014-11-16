package com.emmaguy.todayilearned.background;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class MarkAllReadResponse {
    private final boolean mIsSuccessResponse;

    public MarkAllReadResponse(boolean isSuccessResponse) {
        mIsSuccessResponse = isSuccessResponse;
    }

    public boolean isSuccessResponse() {
        return mIsSuccessResponse;
    }

    public static class MarkAllReadDeserializer implements JsonDeserializer<MarkAllReadResponse> {
        @Override
        public MarkAllReadResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new MarkAllReadResponse(json.toString().equals("202 Accepted"));
        }
    }
}
