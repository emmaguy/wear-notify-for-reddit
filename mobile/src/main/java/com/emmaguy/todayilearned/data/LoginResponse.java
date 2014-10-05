package com.emmaguy.todayilearned.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class LoginResponse {
    private String mModhash;
    private String mCookie;

    public LoginResponse(String modhash, String cookie) {
        mModhash = modhash;
        mCookie = cookie;
    }

    public String getModhash() {
        return mModhash;
    }

    public String getCookie() {
        return mCookie;
    }

    public static class LoginResponseJsonDeserializer implements JsonDeserializer<LoginResponse> {
        @Override
        public LoginResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonContents = json.getAsJsonObject().get("json").getAsJsonObject();
            JsonObject dataObject = jsonContents.get("data").getAsJsonObject();

            return new LoginResponse(dataObject.get("modhash").getAsString(), dataObject.get("cookie").getAsString());
        }
    }
}
