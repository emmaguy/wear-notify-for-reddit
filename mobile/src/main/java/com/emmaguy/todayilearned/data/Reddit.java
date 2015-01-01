package com.emmaguy.todayilearned.data;

import com.emmaguy.todayilearned.data.response.AddCommentResponse;
import com.emmaguy.todayilearned.data.response.LoginResponse;
import com.emmaguy.todayilearned.data.response.MarkAllReadResponse;
import com.emmaguy.todayilearned.data.response.SubscriptionResponse;
import com.emmaguy.todayilearned.sharedlib.Post;

import java.util.List;

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
    Observable<AddCommentResponse> commentOnPost(@Query("text") String comment,
                                              @Query("thing_id") String postId);

    @GET("/subreddits/mine/subscriber.json")
    Observable<SubscriptionResponse> subredditSubscriptions();


    @GET("/message/unread.json")
    Observable<List<Post>> unreadMessages();

    @GET("/{permalink}.json")
    Observable<List<Post>> comments(@Path(value = "permalink", encode = false) String permalink,
                                    @Query("sort") String sort,
                                    @Query("limit") Integer limit);

    @GET("/r/{subreddit}/{sort}.json")
    Observable<List<Post>> latestPosts(@Path("subreddit") String subreddit,
                                       @Path("sort") String sort,
                                       @Query("limit") Integer limit);

    @POST("/api/vote")
    Observable<Void> vote(@Query("id") String fullname, @Query("dir") Integer voteDirection);

    @POST("/api/read_all_messages")
    Observable<MarkAllReadResponse> markAllMessagesRead();

    @POST("/api/compose?api_type=json")
    Observable<AddCommentResponse> replyToDirectMessage(@Query("subject") String subject,
                                                     @Query("text") String message,
                                                     @Query("to") String toUser);
}
