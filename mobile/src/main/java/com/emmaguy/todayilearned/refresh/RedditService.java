package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.settings.SubscriptionResponse;
import com.emmaguy.todayilearned.sharedlib.Post;

import java.util.List;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

// TODO: separate by endpoint
public interface RedditService {
    @POST("/api/v1/access_token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded Observable<Token> loginToken(@Field("grant_type") String grantType,
            @Field("redirect_uri") String redirectUri,
            @Field("code") String code);

    @POST("/api/v1/access_token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded Token refreshToken(@Field("grant_type") String grantType,
            @Field("refresh_token") String refreshToken);

    @POST("/api/comment?api_type=json") Observable<RedditResponse> commentOnPost(@Query("text") String comment,
            @Query("thing_id") String postId);

    @GET("/subreddits/mine/subscriber.json") Observable<SubscriptionResponse> subredditSubscriptions();

    @GET("/message/unread.json") Observable<List<Post>> unreadMessages();

    @GET("/{permalink}.json") Observable<List<Post>> comments(
            @Path(value = "permalink", encode = false) String permalink,
            @Query("sort") String sort);

    @GET("/r/{subreddit}/{sort}.json") Observable<List<Post>> latestPosts(@Path("subreddit") String subreddit,
            @Path("sort") String sort,
            @Query("limit") Integer limit);

    @POST("/api/vote") Observable<Void> vote(@Query("id") String fullname, @Query("dir") Integer voteDirection);

    @POST("/api/read_all_messages") MarkAllRead markAllMessagesRead();

    @POST("/api/compose?api_type=json") Observable<RedditResponse> replyToDirectMessage(
            @Query("subject") String subject,
            @Query("text") String message,
            @Query("to") String toUser);
}
