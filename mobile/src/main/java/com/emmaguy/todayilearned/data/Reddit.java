package com.emmaguy.todayilearned.data;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface Reddit {
    @GET("/r/{subreddit}/{sort}.json")
    Observable<Listing> latestTILs(@Path("subreddit") String subreddit,
                                   @Path("sort") String sort,
                                   @Query("limit") Integer limit);
}