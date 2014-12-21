package com.emmaguy.todayilearned.data;

import com.emmaguy.todayilearned.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class LoginResponse {
    private boolean mHasErrors;
    private String mModhash;
    private String mCookie;
    private String mErrors;

    public boolean hasErrors() {
        return mHasErrors;
    }

    @Override
    public String toString() {
        return "LoginResponse has errors: " + mHasErrors + " " + mErrors;
    }

    private void setErrors(String errors) {
        mHasErrors = true;
        mErrors = errors;
    }

    public String getModhash() {
        return mModhash;
    }

    public String getCookie() {
        return mCookie;
    }

    public void setModhash(String modhash) {
        mModhash = modhash;
    }

    public void setCookie(String cookie) {
        mCookie = cookie;
    }

    public static class LoginResponseJsonDeserializer implements JsonDeserializer<LoginResponse> {
        @Override
        public LoginResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Logger.Log("json: " + json);
            JsonObject jsonContents = json.getAsJsonObject().get("json").getAsJsonObject();

            LoginResponse loginResponse = new LoginResponse();

            if (jsonContents.has("data")) {
                JsonObject dataObject = jsonContents.get("data").getAsJsonObject();
                String modhash = dataObject.get("modhash").getAsString();
                String cookie = dataObject.get("cookie").getAsString();

                loginResponse.setModhash(modhash);
                loginResponse.setCookie(cookie);
            }

            if (jsonContents.has("errors")) {
                JsonArray errors = jsonContents.get("errors").getAsJsonArray();
                if (errors.size() > 0) {
                    loginResponse.setErrors(errors.toString());
                }
            }

            return loginResponse;
        }
    }
}
