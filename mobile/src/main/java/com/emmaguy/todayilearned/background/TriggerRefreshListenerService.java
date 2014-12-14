package com.emmaguy.todayilearned.background;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.RedditRequestInterceptor;
import com.emmaguy.todayilearned.SettingsActivity;
import com.emmaguy.todayilearned.Utils;
import com.emmaguy.todayilearned.data.CommentResponse;
import com.emmaguy.todayilearned.data.Reddit;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Logger;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TriggerRefreshListenerService extends WearableListenerService {
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Constants.PATH_REFRESH)) {
            WakefulIntentService.sendWakefulWork(this, RetrieveService.class);
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                String path = event.getDataItem().getUri().getPath();
                if (Constants.PATH_REPLY.equals(path)) {
                    String fullname = dataMap.getString(Constants.PATH_KEY_POST_FULLNAME);
                    String message = dataMap.getString(Constants.PATH_KEY_MESSAGE);
                    boolean isDirectMessage = dataMap.getBoolean(Constants.PATH_KEY_IS_DIRECT_MESSAGE);

                    if (isDirectMessage) {
                        String subject = dataMap.getString(Constants.PATH_KEY_MESSAGE_SUBJECT);
                        String toUser = dataMap.getString(Constants.PATH_KEY_MESSAGE_TO_USER);

                        replyToDirectMessage(subject, message, toUser);
                    } else {
                        replyToRedditPost(fullname, message);
                    }
                } else if (Constants.PATH_OPEN_ON_PHONE.equals(path)) {
                    String permalink = dataMap.getString(Constants.KEY_POST_PERMALINK);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com" + permalink));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }
    }

    private String getModhash() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.PREFS_KEY_MODHASH, "");
    }

    private String getCookie() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.PREFS_KEY_COOKIE, "");
    }

    private void replyToDirectMessage(String subject, String message, String toUser) {
        String cookie = getCookie();
        String modhash = getModhash();

        if (TextUtils.isEmpty(cookie) || TextUtils.isEmpty(modhash)) {
            Logger.Log("Not logged in, can't reply to message");
            return;
        }

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://www.reddit.com/")
                .setRequestInterceptor(new RedditRequestInterceptor(cookie, modhash))
                .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(CommentResponse.class, new CommentResponse.CommentResponseJsonDeserializer()).create()))
                .build();

        final Reddit redditEndpoint = restAdapter.create(Reddit.class);
        redditEndpoint.replyToDirectMessage(subject, message, toUser)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommentResponse>() {
                    @Override
                    public void onNext(CommentResponse response) {
                        if (response == null) {
                            sendReplyResult(Constants.PATH_POST_REPLY_RESULT_FAILURE);
                        }
                    }

                    @Override
                    public void onCompleted() {
                        sendReplyResult(Constants.PATH_POST_REPLY_RESULT_SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.Log("Failed to post to reddit", e);
                        sendReplyResult(Constants.PATH_POST_REPLY_RESULT_FAILURE);
                    }
                });
    }

    private void replyToRedditPost(String fullname, String message) {
        String cookie = getCookie();
        String modhash = getModhash();

        if (TextUtils.isEmpty(cookie) || TextUtils.isEmpty(modhash)) {
            Logger.Log("Not logged in, can't post reply");
            return;
        }

        final RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://www.reddit.com/")
                .setRequestInterceptor(new RedditRequestInterceptor(cookie, modhash))
                .setConverter(new GsonConverter(new GsonBuilder().registerTypeAdapter(CommentResponse.class, new CommentResponse.CommentResponseJsonDeserializer()).create()))
                .build();

        final Reddit redditEndpoint = restAdapter.create(Reddit.class);
        redditEndpoint.commentOnPost(message, fullname)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<CommentResponse>() {
                    @Override
                    public void onNext(CommentResponse response) {
                        if (response == null) {
                            sendReplyResult(Constants.PATH_POST_REPLY_RESULT_FAILURE);
                        }
                    }

                    @Override
                    public void onCompleted() {
                        sendReplyResult(Constants.PATH_POST_REPLY_RESULT_SUCCESS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.Log("Failed to post to reddit", e);
                        sendReplyResult(Constants.PATH_POST_REPLY_RESULT_FAILURE);
                    }
                });
    }

    private void sendReplyResult(final String result) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, "", result, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                Logger.Log("sendReplyResult: " + result + " status " + sendMessageResult.getStatus());
            }
        });
    }
}
