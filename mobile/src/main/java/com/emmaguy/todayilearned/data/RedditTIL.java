package com.emmaguy.todayilearned.data;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface RedditTIL {
    @GET("/r/todayilearned/hot.json")
    Observable<Listing> latestTILs(@Query("limit") Integer limit,
                                   @Query("before") String before);
}