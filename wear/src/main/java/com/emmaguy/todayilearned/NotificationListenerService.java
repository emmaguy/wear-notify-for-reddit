package com.emmaguy.todayilearned;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationListenerService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 1;
    private static final String GROUP_KEY_SUBREDDIT_POSTS = "group_key_subreddit_posts";
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final String ACTION_RESPONSE = "com.emmaguy.todayilearned.Reply";

    private GoogleApiClient mGoogleApiClient;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();

        mHandler = new Handler();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String message = "";

        if (messageEvent.getPath().equals(Constants.PATH_POST_REPLY_RESULT_SUCCESS)) {
            message = getString(R.string.reply_successful);
        } else if (messageEvent.getPath().equals(Constants.PATH_POST_REPLY_RESULT_FAILURE)) {
            message = getString(R.string.reply_failed_sad_face);
        }

        if (!TextUtils.isEmpty(message)) {
            final String msg = message;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NotificationListenerService.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e("RedditWear", "Service failed to connect");
                return;
            }
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (path.equals(Constants.PATH_REDDIT_POSTS)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    final String latestPosts = dataMapItem.getDataMap().getString(Constants.KEY_REDDIT_POSTS);

                    Gson gson = new Gson();
                    ArrayList<Post> posts = gson.fromJson(latestPosts, new TypeToken<ArrayList<Post>>() {
                    }.getType());

                    Bitmap background = Bitmap.createBitmap(new int[]{getResources().getColor(R.color.theme_blue)}, 1, 1, Bitmap.Config.ARGB_8888);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);
                        final boolean showDescriptions = dataMapItem.getDataMap().getBoolean(Constants.KEY_SHOW_DESCRIPTIONS) || post.isDirectMessage();

                        Log.d("RedditWear", "building notif, name: " + post.getFullname());
                        int notificationId = (int) post.getCreatedUtc();

                        Notification.Action replyAction = new Notification.Action.Builder(
                                R.drawable.ic_full_reply, getString(R.string.reply_to_x, post.getShortTitle()), getReplyPendingIntent(notificationId, post))
                                .addRemoteInput(new RemoteInput.Builder(EXTRA_VOICE_REPLY).build())
                                .build();

                        Notification.Action openOnPhoneAction = new Notification.Action.Builder(
                                R.drawable.go_to_phone_00156, getString(R.string.open_on_phone), getOpenOnPhonePendingIntent(notificationId, post.getPermalink()))
                                .build();

                        Notification.Builder builder = new Notification.Builder(this)
                                .setContentTitle(post.isDirectMessage() ? getString(R.string.message_from_x, post.getAuthor()) : post.getSubreddit())
                                .setContentText((post.isDirectMessage() ? post.getDescription() : post.getTitle() + postDescription(showDescriptions, post.getDescription())))
                                .setGroup(GROUP_KEY_SUBREDDIT_POSTS)
                                .setSmallIcon(R.drawable.ic_launcher);

                        builder.addAction(replyAction);
                        builder.addAction(openOnPhoneAction);

                        Notification.WearableExtender extender = new Notification.WearableExtender();
                        extender.setBackground(background);
                        builder.extend(extender);

                        notificationManager.notify(notificationId, builder.build());
                    }
                }
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == intent || null == intent.getAction()) {
            return Service.START_STICKY;
        }
        String action = intent.getAction();
        if (action.equals(ACTION_RESPONSE)) {
            Bundle remoteInputResults = RemoteInput.getResultsFromIntent(intent);
            CharSequence replyMessage = "";
            if (remoteInputResults != null) {
                replyMessage = remoteInputResults.getCharSequence(EXTRA_VOICE_REPLY);
            }
            String subject = intent.getStringExtra(Constants.PATH_KEY_MESSAGE_SUBJECT);
            String toUser = intent.getStringExtra(Constants.PATH_KEY_MESSAGE_TO_USER);
            String fullname = intent.getStringExtra(Constants.PATH_KEY_POST_FULLNAME);
            boolean isDirectMessage = intent.getBooleanExtra(Constants.PATH_KEY_IS_DIRECT_MESSAGE, false);
            sendReplyToPhone(replyMessage.toString(), fullname, toUser, subject, isDirectMessage);
        }
        return Service.START_STICKY;
    }

    private void sendReplyToPhone(String text, String fullname, String toUser, String subject, boolean isDirectMessage) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.PATH_REPLY);
        putDataMapRequest.getDataMap().putLong("timestamp", System.currentTimeMillis());
        putDataMapRequest.getDataMap().putString(Constants.PATH_KEY_MESSAGE_SUBJECT, subject);
        putDataMapRequest.getDataMap().putString(Constants.PATH_KEY_MESSAGE, text);
        putDataMapRequest.getDataMap().putString(Constants.PATH_KEY_POST_FULLNAME, fullname);
        putDataMapRequest.getDataMap().putString(Constants.PATH_KEY_MESSAGE_TO_USER, toUser);
        putDataMapRequest.getDataMap().putBoolean(Constants.PATH_KEY_IS_DIRECT_MESSAGE, isDirectMessage);

        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        Log.d("RedditWear", "sendReplyToPhone, putDataItem status: " + dataItemResult.getStatus().toString());
                    }
                });
    }

    private String postDescription(boolean showDescriptions, String description) {
        if (!showDescriptions) {
            return "";
        }

        if (TextUtils.isEmpty(description)) {
            return "";
        }
        Log.e("RedditWear", "desc: '" + description + "'");
        return "\n\n" + description;
    }

    private PendingIntent getOpenOnPhonePendingIntent(int notificationId, String permalink) {
        Intent openOnPhone = new Intent(this, OpenOnPhoneReceiver.class);
        openOnPhone.putExtra(Constants.KEY_POST_PERMALINK, permalink);
        return PendingIntent.getBroadcast(this, notificationId, openOnPhone, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getReplyPendingIntent(int notificationId, Post post) {
        Intent intent = new Intent(ACTION_RESPONSE);
        intent.putExtra(Constants.PATH_KEY_IS_DIRECT_MESSAGE, post.isDirectMessage());
        intent.putExtra(Constants.PATH_KEY_MESSAGE_TO_USER, post.getAuthor());
        intent.putExtra(Constants.PATH_KEY_MESSAGE_SUBJECT, post.getDescription());
        intent.putExtra(Constants.PATH_KEY_POST_FULLNAME, post.getFullname());
        return PendingIntent.getService(this, notificationId, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
