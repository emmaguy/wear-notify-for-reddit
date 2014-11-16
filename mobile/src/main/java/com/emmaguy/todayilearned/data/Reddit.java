package com.emmaguy.todayilearned.data;

import com.emmaguy.todayilearned.background.MarkAllReadResponse;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface Reddit {
    @POST("/api/login?rem=true&api_type=json")
    Observable<LoginResponse> login(@Query("user") String username,
                                    @Query("passwd") String password);

    @POST("/api/comment?api_type=json")
    Observable<CommentResponse> commentOnPost(@Query("text") String comment,
                                              @Query("thing_id") String postId);

    @GET("/subreddits/mine/subscriber.json")
    Observable<SubscriptionResponse> subredditSubscriptions();


    @GET("/message/unread.json")
    Observable<ListingResponse> unreadMessages();

    @GET("/r/{subreddit}/{sort}.json")
    Observable<ListingResponse> latestPosts(@Path("subreddit") String subreddit,
                                            @Path("sort") String sort,
                                            @Query("limit") Integer limit);

    @POST("/api/read_all_messages")
    Observable<MarkAllReadResponse> markAllMessagesRead();

    @POST("/api/compose?api_type=json")
    Observable<CommentResponse> replyToDirectMessage(@Query("subject") String subject,
                                                     @Query("text") String message,
                                                     @Query("to") String toUser);
}
