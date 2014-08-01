package com.emmaguy.todayilearned.background;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.SettingsActivity;
import com.emmaguy.todayilearned.Utils;
import com.emmaguy.todayilearned.data.Listing;
import com.emmaguy.todayilearned.data.Post;
import com.emmaguy.todayilearned.data.Reddit;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RetrieveService extends WakefulIntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://www.reddit.com/")
            .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(Listing.class, new Listing.ListingJsonDeserializer()).create()))
            .build();

    private final Reddit mRedditEndpoint = restAdapter.create(Reddit.class);
    private final ArrayList<String> mRedditPosts = new ArrayList<String>();

    private GoogleApiClient mGoogleApiClient;

    public RetrieveService() {
        super("RetrieveService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        connectToWearable();
        retrieveLatestTILsFromReddit();
    }

    private void connectToWearable() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void retrieveLatestTILsFromReddit() {
        mRedditEndpoint.latestTILs(getSubreddit(), getSortType(), getNumberToRequest(), getBeforeId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<Listing, Observable<Post>>() {
                    @Override
                    public Observable<Post> call(Listing listing) {
                        storeNewBeforeId(listing.before);

                        return Observable.from(listing.getPosts());
                    }
                })
                .subscribe(new Action1<Post>() {
                    @Override
                    public void call(Post post) {
                        mRedditPosts.add(post.getTitle());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("RedditWearApp", "failed: " + throwable.getLocalizedMessage(), throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        sendNewPostsData();
                    }
                });
    }

    private void storeNewBeforeId(String before) {
        if (!TextUtils.isEmpty(before)) {
            getSharedPreferences().edit().putString(SettingsActivity.PREFS_BEFORE_ID, before).apply();
        }
    }

    private void sendNewPostsData() {
        if (mRedditPosts.size() <= 0) {
            return;
        }

        if (mGoogleApiClient.isConnected()) {
            PutDataMapRequest mapRequest = PutDataMapRequest.create("/redditwear");
            mapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis()); // debug, ensure it sends, even if content is the same
            mapRequest.getDataMap().putStringArrayList("posts", mRedditPosts);

            Wearable.DataApi.putDataItem(mGoogleApiClient, mapRequest.asPutDataRequest())
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            mRedditPosts.clear();

                            if (mGoogleApiClient.isConnected()) {
                                mGoogleApiClient.disconnect();
                            }
                        }
                    });
        }
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    private int getNumberToRequest() {
        return Integer.parseInt(getSharedPreferences().getString(SettingsActivity.PREFS_NUMBER_TO_RETRIEVE, "5"));
    }

    private String getBeforeId() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_BEFORE_ID, "");
    }

    private String getSortType() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_SORT_ORDER, "hot");
    }

    private String getSubreddit() {
        return TextUtils.join("+", Utils.selectedSubReddits(getApplicationContext()));
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
