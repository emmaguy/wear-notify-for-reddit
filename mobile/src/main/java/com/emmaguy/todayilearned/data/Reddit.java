package com.emmaguy.todayilearned.data;

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

    @GET("/r/{subreddit}/{sort}.json")
    Observable<ListingResponse> latestPosts(@Path("subreddit") String subreddit,
                                            @Path("sort") String sort,
                                            @Query("limit") Integer limit);
}
