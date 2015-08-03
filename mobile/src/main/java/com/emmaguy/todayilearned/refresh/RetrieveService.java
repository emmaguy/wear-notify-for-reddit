package com.emmaguy.todayilearned.refresh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SimpleArrayMap;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.App;
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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Scheduler;
import rx.functions.Action1;
import timber.log.Timber;

public class RetrieveService extends WakefulIntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String INTENT_KEY_INFORM_WATCH_NO_POSTS = "inform_no_posts";

    @Inject UnreadDirectMessageRetriever mUnreadDirectMessageRetriever;
    @Inject LatestPostsRetriever mLatestPostsRetriever;

    @Inject ActionStorage mWearableActionStorage;
    @Inject TokenStorage mTokenStorage;
    @Inject UserStorage mUserStorage;
    @Inject Gson mGson;

    @Inject @Named("io") Scheduler mIoScheduler;

    private boolean mSendInformationToWearableIfNoPosts = false;
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

        mSendInformationToWearableIfNoPosts = false;
        if (intent.hasExtra(INTENT_KEY_INFORM_WATCH_NO_POSTS)) {
            mSendInformationToWearableIfNoPosts = intent.getBooleanExtra(INTENT_KEY_INFORM_WATCH_NO_POSTS, false);
        }

        final String message = "refresh: " + mUserStorage.getRefreshInterval() + ", subreddits: " +
                mUserStorage.getSubreddits() + ", sort: " + mUserStorage.getSortType() + ", number: " + mUserStorage.getNumberToRequest() + ", timestamp: " + mUserStorage.getTimestamp();

        mLatestPostsRetriever.retrieve()
                .subscribeOn(mIoScheduler)
                .observeOn(mIoScheduler)
                .subscribe(new Action1<List<LatestPostsRetriever.PostAndImage>>() {
                    @Override public void call(List<LatestPostsRetriever.PostAndImage> postAndImages) {
                        if (postAndImages.size() > 0) {
                            String msg = message + ", posts " + postAndImages.size();

                            final List<Post> posts = new ArrayList<>(postAndImages.size());
                            final SimpleArrayMap<String, Asset> assets = new SimpleArrayMap<>();
                            for (LatestPostsRetriever.PostAndImage p : postAndImages) {
                                if (p.getImage() != null) {
                                    assets.put(p.getPost().getId(), p.getImage());
                                }
                                posts.add(p.getPost());
                            }

                            sendNewPostsData(posts, msg, assets);
                        } else if (mSendInformationToWearableIfNoPosts) {
                            WearListenerService.sendReplyResult(mGoogleApiClient, Constants.PATH_NO_NEW_POSTS);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        Timber.e(throwable, "Failed to get latest posts: " + message);
                    }
                });

        mUnreadDirectMessageRetriever.retrieve()
                .subscribeOn(mIoScheduler)
                .observeOn(mIoScheduler)
                .subscribe(new Action1<List<Post>>() {
                    @Override public void call(List<Post> posts) {
                        if (posts.size() > 0) {
                            String msg = "Refresh messages, found " + posts.size();
                            sendNewPostsData(posts, msg, null);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable throwable) {
                        Timber.e(throwable, "Failed to get latest messages");
                    }
                });
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

    private void sendNewPostsData(@NonNull List<Post> posts, @NonNull final String msg, @Nullable SimpleArrayMap<String, Asset> assets) {
        if (mGoogleApiClient.isConnected()) {
            // convert to json for sending to watch and to save to shared prefs
            // don't need to preserve the order like having separate String lists, can more easily add/remove fields
            PutDataMapRequest mapRequest = PutDataMapRequest.create(Constants.PATH_REDDIT_POSTS);
            DataMap dataMap = mapRequest.getDataMap();

            if (assets != null && !assets.isEmpty()) {
                for (int i = 0; i < assets.size(); i++) {
                    dataMap.putAsset(assets.keyAt(i), assets.valueAt(i));
                }
            }

            dataMap.putLong("timestamp", System.currentTimeMillis());
            dataMap.putString(Constants.KEY_REDDIT_POSTS, mGson.toJson(posts));
            dataMap.putBoolean(Constants.KEY_DISMISS_AFTER_ACTION, mUserStorage.openOnPhoneDismissesAfterAction());
            dataMap.putIntegerArrayList(Constants.KEY_ACTION_ORDER, mWearableActionStorage.getSelectedActionIds());

            PutDataRequest request = mapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            Timber.d(msg + ", final timestamp: " + mUserStorage.getTimestamp() + " result: " + dataItemResult.getStatus());

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
