package com.emmaguy.todayilearned.background;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.BuildConfig;
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
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
                .setConverter(new GsonConverter(new GsonBuilder()
                        .registerTypeAdapter(ListingResponse.class, new ListingResponse.ListingJsonDeserializer()).create()))
                .build();

        final Reddit reddit = restAdapter.create(Reddit.class);

        if (isLoggedIn() && messagesEnabled()) {
            Log.d("RedditWear", "Retrieving user's messages");

            reddit.unreadMessages()
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
                            Log.d("RedditWear", "Found a message: " + post.getDescription());
                            mRedditPosts.add(post);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d("RedditWear", "Failed to get user's messages", throwable);
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            // No messages to mark as read, so just request posts as normal
                            if (mRedditPosts.size() <= 0) {
                                Log.d("RedditWear", "No messages found");
                                getLatestSubredditPosts(reddit);
                            } else {
                                getLatestSubredditPosts(reddit);
                                // Mark all as read, then request latest posts from subreddits
                                getRestAdapter()
                                        .create(Reddit.class)
                                        .markAllMessagesRead()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<MarkAllReadResponse>() {
                                            @Override
                                            public void call(MarkAllReadResponse response) {
                                                if (response.isSuccessResponse()) {
                                                    getLatestSubredditPosts(reddit);
                                                }
                                            }
                                        });
                            }
                        }
                    });
        } else {
            getLatestSubredditPosts(reddit);
        }
    }

    private RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint("https://www.reddit.com/")
                .setRequestInterceptor(new RedditRequestInterceptor(getCookie(), getModhash()))
                .setConverter(new Converter() {
                    @Override
                    public Object fromBody(TypedInput body, Type type) throws ConversionException {
                        try {
                            java.util.Scanner s = new java.util.Scanner(body.in()).useDelimiter("\\A");
                            String bodyText = s.hasNext() ? s.next() : "";
                            return new MarkAllReadResponse(bodyText.startsWith("202 Accepted"));
                        } catch (IOException e) {
                            throw new ConversionException(e);
                        }
                    }

                    @Override
                    public TypedOutput toBody(Object object) {
                        throw new UnsupportedOperationException();
                    }
                })
                .build();
    }

    private boolean messagesEnabled() {
        return getSharedPreferences().getBoolean(SettingsActivity.PREFS_MESSAGES_ENABLED, true);
    }

    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(getCookie());
    }

    private void getLatestSubredditPosts(final Reddit reddit) {
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
                        // In debug, never ignore posts - we want content to test with
                        if (postIsNewerThanPreviouslyRetrievedPosts(post) || BuildConfig.DEBUG) {
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
