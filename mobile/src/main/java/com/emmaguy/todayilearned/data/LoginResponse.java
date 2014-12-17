package com.emmaguy.todayilearned.data;

import com.emmaguy.todayilearned.sharedlib.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONArray;

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
            Logger.Log("json: " + json);
            JsonObject jsonContents = json.getAsJsonObject().get("json").getAsJsonObject();

            // TODO: handle errors
//            if (jsonContents.has("errors")) {
//                JsonArray errors = jsonContents.get("errors").getAsJsonArray();
//            }

            JsonObject dataObject = jsonContents.get("data").getAsJsonObject();

            return new LoginResponse(dataObject.get("modhash").getAsString(), dataObject.get("cookie").getAsString());
        }
    }
}
