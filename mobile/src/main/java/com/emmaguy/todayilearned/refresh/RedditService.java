package com.emmaguy.todayilearned.refresh;

import com.emmaguy.todayilearned.settings.SubscriptionResponse;
import com.emmaguy.todayilearned.sharedlib.Comment;
import com.emmaguy.todayilearned.sharedlib.Post;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface RedditService {
    @POST("/api/comment?api_type=json") Observable<RedditResponse> commentOnPost(
            @Query("text") String comment, @Query("thing_id") String postId);

    @GET("/subreddits/mine/subscriber.json?limit=1000")
    Observable<SubscriptionResponse> subredditSubscriptions();

    @GET("/message/unread.json") Observable<List<Post>> unreadMessages();

    @GET("/{permalink}.json") Observable<List<Comment>> comments(
            @Path(value = "permalink", encode = false) String permalink,
            @Query("sort") String sort);

    @GET("/r/{subreddit}/{sort}.json") Observable<List<Post>> latestPosts(
            @Path("subreddit") String subreddit, @Path("sort") String sort,
            @Query("limit") Integer limit);

    @POST("/api/vote") Observable<Void> vote(@Query("id") String fullname,
                                             @Query("dir") Integer voteDirection);

    @POST("/api/read_all_messages") MarkAllRead markAllMessagesRead();

    @POST("/api/compose?api_type=json") Observable<RedditResponse> replyToDirectMessage(
            @Query("subject") String subject, @Query("text") String message,
            @Query("to") String toUser);
}
