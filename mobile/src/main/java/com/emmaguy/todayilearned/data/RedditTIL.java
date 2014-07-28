package com.emmaguy.todayilearned.data;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface RedditTIL {
    @GET("/r/todayilearned/{sort}.json")
    Observable<Listing> latestTILs(@Path("sort") String sort,
                                   @Query("limit") Integer limit,
                                   @Query("before") String before);
}