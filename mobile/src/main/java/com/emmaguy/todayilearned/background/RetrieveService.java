package com.emmaguy.todayilearned.background;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.RedditRequestInterceptor;
import com.emmaguy.todayilearned.SettingsActivity;
import com.emmaguy.todayilearned.Utils;
import com.emmaguy.todayilearned.data.ListingResponse;
import com.emmaguy.todayilearned.data.Reddit;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
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
    private final ArrayList<Post> mRedditPosts = new ArrayList<Post>();

    private GoogleApiClient mGoogleApiClient;

    private long mLatestCreatedUtc = 0;

    public RetrieveService() {
        super("RetrieveService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        connectToWearable();
        retrieveLatestPostsFromReddit();
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

    private void retrieveLatestPostsFromReddit() {
        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://www.reddit.com/")
                .setRequestInterceptor(new RedditRequestInterceptor(getCookie(), getModhash()))
                .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(ListingResponse.class, new ListingResponse.ListingJsonDeserializer()).create()))
                .build();

        final Reddit reddit = restAdapter.create(Reddit.class);

        reddit.latestPosts(getSubreddit(), getSortType(), getNumberToRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<ListingResponse, Observable<Post>>() {
                    @Override
                    public Observable<Post> call(ListingResponse listingResponse) {
                        return Observable.from(listingResponse.getPosts());
                    }
                })
                .subscribe(new Action1<Post>() {
                    @Override
                    public void call(Post post) {
                        if (postIsNewerThanPreviouslyRetrievedPosts(post)) {
                            Utils.Log("Adding post: " + post.getTitle());

                            mRedditPosts.add(post);

                            if (post.getCreatedUtc() > mLatestCreatedUtc) {
                                mLatestCreatedUtc = post.getCreatedUtc();
                                Utils.Log("updating mLatestCreatedUtc to: " + mLatestCreatedUtc);
                            }
                        } else {
                            Utils.Log("Ignoring post: " + post.getTitle());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("RedditWearApp", "Failed to retrieve latest posts: " + throwable.getLocalizedMessage(), throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        Utils.Log("Posts found: " + mRedditPosts.size());
                        if (mRedditPosts.size() > 0) {
                            if (mLatestCreatedUtc > 0) {
                                storeNewCreatedUtc(mLatestCreatedUtc);
                            }

                            sendNewPostsData();
                        }
                    }
                });
    }

    private String getModhash() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_KEY_MODHASH, "");
    }

    private String getCookie() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_KEY_COOKIE, "");
    }

    private boolean postIsNewerThanPreviouslyRetrievedPosts(Post post) {
        return post.getCreatedUtc() > getCreatedUtcOfPosts();
    }

    private void storeNewCreatedUtc(long createdAtUtc) {
        Utils.Log("storeNewCreatedUtc: " + createdAtUtc);

        getSharedPreferences().edit().putLong(SettingsActivity.PREFS_CREATED_UTC, createdAtUtc).apply();
    }

    private void sendNewPostsData() {
        if (mGoogleApiClient.isConnected()) {
            Utils.Log("sendNewPostsData: " + mRedditPosts.size());

            Gson gson = new Gson();
            final String latestPosts = gson.toJson(mRedditPosts);

            // convert to json for sending to watch and to save to shared prefs
            // don't need to preserve the order like having separate String lists, can more easily add/remove fields
            PutDataMapRequest mapRequest = PutDataMapRequest.create(Constants.PATH_REDDIT_POSTS);
            mapRequest.getDataMap().putString(Constants.KEY_REDDIT_POSTS, latestPosts);
            mapRequest.getDataMap().putBoolean(Constants.KEY_SHOW_DESCRIPTIONS, getSharedPreferences().getBoolean(SettingsActivity.PREFS_SHOW_DESCRIPTIONS, true));

            Wearable.DataApi.putDataItem(mGoogleApiClient, mapRequest.asPutDataRequest())
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Utils.Log("onResult: " + dataItemResult.getStatus());

                            if (dataItemResult.getStatus().isSuccess()) {
                                getSharedPreferences().edit().putString(SettingsActivity.PREFS_REDDIT_POSTS, latestPosts).apply();

                                mRedditPosts.clear();

                                if (mGoogleApiClient.isConnected()) {
                                    mGoogleApiClient.disconnect();
                                }
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

    private long getCreatedUtcOfPosts() {
        return getSharedPreferences().getLong(SettingsActivity.PREFS_CREATED_UTC, 0);
    }

    private String getSortType() {
        return getSharedPreferences().getString(SettingsActivity.PREFS_SORT_ORDER, "new");
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
