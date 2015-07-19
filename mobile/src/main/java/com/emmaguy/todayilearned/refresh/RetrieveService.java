package com.emmaguy.todayilearned.refresh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.App;
import com.emmaguy.todayilearned.common.Logger;
import com.emmaguy.todayilearned.settings.ActionStorage;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.emmaguy.todayilearned.storage.TokenStorage;
import com.emmaguy.todayilearned.storage.UserStorage;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit.converter.GsonConverter;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RetrieveService extends WakefulIntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String INTENT_KEY_INFORM_WATCH_NO_POSTS = "inform_no_posts";

    @Inject LatestPostsFromRedditRetriever mLatestPostsRetriever;
    @Inject UnauthenticatedRedditService mUnauthenticatedRedditService;
    @Inject AuthenticatedRedditService mAuthenticatedRedditService;
    @Inject ActionStorage mWearableActionStorage;
    @Inject TokenStorage mTokenStorage;
    @Inject UserStorage mUserStorage;

    @Inject @Named("posts") GsonConverter mPostsConverter;

    private GoogleApiClient mGoogleApiClient;

    public RetrieveService() {
        super("RetrieveService");
    }

    public static Intent getFromWearableIntent(Context context) {
        final Intent intent = new Intent(context, RetrieveService.class);
        intent.putExtra(INTENT_KEY_INFORM_WATCH_NO_POSTS, true);
        return intent;
    }

    public static Intent getFromBackgroundSyncIntent(Context context) {
        return new Intent(context, RetrieveService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        App.with(this).getAppComponent().inject(this);
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        connectToWearable();

        boolean informWatchIfNoPosts = false;
        if (intent.hasExtra(INTENT_KEY_INFORM_WATCH_NO_POSTS)) {
            informWatchIfNoPosts = intent.getBooleanExtra(INTENT_KEY_INFORM_WATCH_NO_POSTS, false);
        }

        retrieveLatestPostsFromReddit(informWatchIfNoPosts);
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

    private RedditService getRedditServiceForLoggedInState(GsonConverter converter) {
        if (mTokenStorage.isLoggedIn()) {
            return mAuthenticatedRedditService.getRedditService(converter);
        }

        return mUnauthenticatedRedditService.getRedditService(converter, null);
    }

    private void retrieveLatestPostsFromReddit(final boolean sendInformationToWearableIfNoPosts) {
        mLatestPostsRetriever.getPosts(getRedditServiceForLoggedInState(mPostsConverter))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Post>>() {
                    @Override
                    public void call(List<Post> posts) {
                        if (posts.size() > 0) {
                            sendNewPostsData(posts);
                        } else if (sendInformationToWearableIfNoPosts) {
                            Logger.log(getApplicationContext(), "Sending no posts information");
                            WearListenerService.sendReplyResult(mGoogleApiClient, Constants.PATH_NO_NEW_POSTS);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.sendThrowable(getApplicationContext(), "Failed to get latest posts", throwable);
                    }
                });

        if (mTokenStorage.isLoggedIn() && mUserStorage.messagesEnabled()) {
            mAuthenticatedRedditService.getRedditService(mPostsConverter)
                    .unreadMessages()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Post>>() {
                        @Override
                        public void call(List<Post> messages) {
                            Logger.log(getApplicationContext(), "Found messages: " + messages.size());

                            if (messages.size() > 0) {
                                sendNewPostsData(messages);

                                mAuthenticatedRedditService.getRedditService(new MarkAsReadConverter())
                                        .markAllMessagesRead()
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<MarkAllRead>() {
                                            @Override
                                            public void call(MarkAllRead markAllRead) {
                                                if (markAllRead.hasErrors()) {
                                                    throw new RuntimeException("Failed to mark all as read: " + markAllRead);
                                                }
                                            }
                                        }, new Action1<Throwable>() {
                                            @Override
                                            public void call(Throwable throwable) {
                                                Logger.sendThrowable(getApplicationContext(), throwable.getMessage(), throwable);
                                            }
                                        });
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.sendThrowable(getApplicationContext(), "Failed to get latest messages", throwable);
                        }
                    });
        }
    }

    private void sendNewPostsData(List<Post> posts) {
        if (mGoogleApiClient.isConnected()) {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            final String latestPosts = gson.toJson(posts);

            // convert to json for sending to watch and to save to shared prefs
            // don't need to preserve the order like having separate String lists, can more easily add/remove fields
            PutDataMapRequest mapRequest = PutDataMapRequest.create(Constants.PATH_REDDIT_POSTS);
            DataMap dataMap = mapRequest.getDataMap();
            dataMap.putString(Constants.KEY_REDDIT_POSTS, latestPosts);

            for (Post p : posts) {
                if ((p.hasThumbnail() || p.hasHighResImage()) && p.getImage() != null) {
                    Asset asset = Asset.createFromBytes(p.getImage());
                    dataMap.putAsset(p.getId(), asset);
                }
            }
            dataMap.putIntegerArrayList(Constants.KEY_ACTION_ORDER, mWearableActionStorage.getSelectedActionIds());
            dataMap.putBoolean(Constants.KEY_DISMISS_AFTER_ACTION, mUserStorage.openOnPhoneDismissesAfterAction());
            dataMap.putLong("timestamp", System.currentTimeMillis());

            PutDataRequest request = mapRequest.asPutDataRequest();
            Logger.log(getApplicationContext(), "Sending request with " + posts.size() + " posts");
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Logger.log(getApplicationContext(), "onResult: " + dataItemResult.getStatus());

                            if (dataItemResult.getStatus().isSuccess()) {
                                if (mGoogleApiClient.isConnected()) {
                                    mGoogleApiClient.disconnect();
                                }
                            }
                        }
                    });
        }
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
