package com.emmaguy.todayilearned.refresh;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Headers;
import retrofit.http.POST;
import rx.Observable;

public interface RedditAuthenticationService {
    @POST("/api/v1/access_token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded Observable<Token> loginToken(@Field("grant_type") String grantType,
            @Field("redirect_uri") String redirectUri,
            @Field("code") String code);

    @POST("/api/v1/access_token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded Token refreshToken(@Field("grant_type") String grantType, @Field("refresh_token") String refreshToken);

    @POST("/api/v1/access_token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded Token appOnlyToken(@Field("grant_type") String grantType, @Field("device_id") String deviceId);
}
