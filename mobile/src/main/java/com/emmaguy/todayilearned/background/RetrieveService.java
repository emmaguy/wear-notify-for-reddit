package com.emmaguy.todayilearned.background;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.BuildConfig;
import com.emmaguy.todayilearned.Logger;
import com.emmaguy.todayilearned.PocketUtil;
import com.emmaguy.todayilearned.R;
import com.emmaguy.todayilearned.RedditRequestInterceptor;
import com.emmaguy.todayilearned.SubredditPreference;
import com.emmaguy.todayilearned.data.ListingJsonDeserializer;
import com.emmaguy.todayilearned.data.Reddit;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class RetrieveService extends WakefulIntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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
                .setEndpoint(Constants.ENDPOINT_URL_REDDIT)
                .setRequestInterceptor(new RedditRequestInterceptor(getCookie(), getModhash()))
                .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(new TypeToken<List<Post>>() {}.getType(), new ListingJsonDeserializer()).create()))
                .build();

        final Reddit reddit = restAdapter.create(Reddit.class);

        latestPostsFromReddit(reddit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Post>>() {
                    @Override
                    public void call(List<Post> posts) {
                        Logger.Log("Found posts: " + posts.size());

                        if (mLatestCreatedUtc > 0) {
                            updateRetrievedPostCreatedUtc(mLatestCreatedUtc);
                        }

                        if (posts.size() > 0) {
                            sendNewPostsData(posts);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.Log(getApplicationContext(), "Failed to get latest posts", throwable);
                    }
                });

        if (isLoggedIn() && messagesEnabled()) {
            reddit.unreadMessages()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Post>>() {
                        @Override
                        public void call(List<Post> messages) {
                            Logger.Log("Found messages: " + messages.size());

                            if (messages.size() > 0) {
                                sendNewPostsData(messages);

                                getRestAdapter()
                                        .create(Reddit.class)
                                        .markAllMessagesRead()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<MarkAllReadResponse>() {
                                            @Override
                                            public void call(MarkAllReadResponse markAllReadResponse) {
                                                Logger.Log("Mark all read: " + markAllReadResponse.isSuccessResponse());
                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                Logger.Log(getApplicationContext(), "Failed to mark all as read", throwable);
                                            }
                                        });
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.Log(getApplicationContext(), "Failed to get latest messages", throwable);
                        }
                    });
        }
    }

    private Observable<List<Post>> latestPostsFromReddit(final Reddit reddit) {
        return reddit.latestPosts(getSubreddit(), getSortType(), getNumberToRequest())
                .lift(RetrieveService.<Post>flattenList())
                .filter(new Func1<Post, Boolean>() {
                    @Override
                    public Boolean call(Post post) {
                        // Check that this post is new (i.e. we haven't retrieved it before)
                        // In debug, never ignore posts - we want content to test with
                        return (post.getCreatedUtc() > getCreatedUtcOfRetrievedPosts()) || BuildConfig.DEBUG;
                    }
                })
                .doOnNext(new Action1<Post>() {
                    @Override
                    public void call(Post post) {
                        if (post.getCreatedUtc() > mLatestCreatedUtc) {
                            mLatestCreatedUtc = post.getCreatedUtc();
                            Logger.Log("Updating mLatestCreatedUtc to: " + mLatestCreatedUtc);
                        }

                        if (post.hasThumbnail()) {
                            try {
                                URL url = new URL(post.getThumbnail());
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setDoInput(true);
                                connection.connect();
                                InputStream input = connection.getInputStream();

                                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                                Bitmap bitmap = BitmapFactory.decodeStream(input);
                                bitmap.compress(Bitmap.CompressFormat.PNG, 85, byteStream);
                                post.setThumbnailImage(byteStream.toByteArray());
                            } catch (Exception e) {
                                Logger.Log(getApplicationContext(), "Failed to download image: " + post.getThumbnail(), e);
                            }
                        }
                    }
                })
                .toList();
    }

    private static <T> Observable.Operator<T, List<T>> flattenList() {
        return new Observable.Operator<T, List<T>>() {
            @Override
            public Subscriber<? super List<T>> call(final Subscriber<? super T> subscriber) {
                return new Subscriber<List<T>>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(List<T> list) {
                        for (T c : list) {
                            subscriber.onNext(c);
                        }
                    }
                };
            }
        };
    }

    private RestAdapter getRestAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(Constants.ENDPOINT_URL_REDDIT)
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
        return getSharedPreferences().getBoolean(getString(R.string.prefs_key_messages_enabled), true);
    }

    private boolean isLoggedIn() {
        return !TextUtils.isEmpty(getCookie());
    }

    private String getModhash() {
        return getSharedPreferences().getString(getString(R.string.prefs_key_modhash), "");
    }

    private String getCookie() {
        return getSharedPreferences().getString(getString(R.string.prefs_key_cookie), "");
    }

    private void sendNewPostsData(List<Post> posts) {
        if (mGoogleApiClient.isConnected()) {
            Logger.Log("sendNewPostsData: " + posts.size());

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            final String latestPosts = gson.toJson(posts);

            Logger.Log("latestPosts: " + latestPosts);

            // convert to json for sending to watch and to save to shared prefs
            // don't need to preserve the order like having separate String lists, can more easily add/remove fields
            PutDataMapRequest mapRequest = PutDataMapRequest.create(Constants.PATH_REDDIT_POSTS);
            mapRequest.getDataMap().putString(Constants.KEY_REDDIT_POSTS, latestPosts);

            for (Post p : posts) {
                if (p.hasThumbnail() && p.getThumbnailImage() != null) {
                    Asset asset = Asset.createFromBytes(p.getThumbnailImage());

                    Logger.Log("Putting asset with id: " + p.getId() + " asset " + asset + " url: " + p.getThumbnail());
                    mapRequest.getDataMap().putAsset(p.getId(), asset);
                }
            }

            mapRequest.getDataMap().putBoolean(Constants.KEY_POCKET_INSTALLED, PocketUtil.isPocketInstalled(this));
            mapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());

            PutDataRequest request = mapRequest.asPutDataRequest();
            Logger.Log("Sending request: " + request);
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Logger.Log("onResult: " + dataItemResult.getStatus());

                            if (dataItemResult.getStatus().isSuccess()) {
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
        return Integer.parseInt(getSharedPreferences().getString(getString(R.string.prefs_key_number_to_retrieve), "5"));
    }

    private void updateRetrievedPostCreatedUtc(long createdAtUtc) {
        Logger.Log("updateRetrievedPostCreatedUtc: " + createdAtUtc);

        getSharedPreferences().edit().putLong(getString(R.string.prefs_key_created_utc), createdAtUtc).apply();
    }

    private long getCreatedUtcOfRetrievedPosts() {
        return getSharedPreferences().getLong(getString(R.string.prefs_key_created_utc), 0);
    }

    private String getSortType() {
        return getSharedPreferences().getString(getString(R.string.prefs_key_sort_order), "new");
    }

    private String getSubreddit() {
        return TextUtils.join("+", SubredditPreference.getAllSelectedSubreddits(getSharedPreferences(), getString(R.string.prefs_key_selected_subreddits)));
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
