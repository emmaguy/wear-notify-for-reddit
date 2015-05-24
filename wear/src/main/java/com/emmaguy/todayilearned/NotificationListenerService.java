package com.emmaguy.todayilearned;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.ConfirmationActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.emmaguy.todayilearned.comments.ActionReceiver;
import com.emmaguy.todayilearned.comments.CommentsActivity;
import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationListenerService extends WearableListenerService {
    private static final String GROUP_KEY_SUBREDDIT_POSTS = "group_key_subreddit_posts";
    private static final String EXTRA_VOICE_REPLY = "extra_voice_reply";
    private static final String ACTION_RESPONSE = "com.emmaguy.todayilearned.Reply";

    private static final long TIMEOUT_MS = 30 * 1000;

    private static final int REQUEST_CODE_VOTE_UP = 0;
    private static final int REQUEST_CODE_VOTE_DOWN = 1;
    private static final int REQUEST_CODE_OPEN_ON_PHONE = 2;
    private static final int REQUEST_CODE_SAVE_TO_POCKET = 3;
    private static final int REQUEST_CODE_REPLY = 4;
    private static final int REQUEST_VIEW_COMMENTS = 5;
    private static final int REQUEST_VIEW_FULLSCREEN_IMAGE = 6;

    private static final int NOTIFICATION_ID_INCREMENT = 10;
    private static int sNotificationId = 0;
    private final Gson mGson = new Gson();
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
        final String path = messageEvent.getPath();
        String message = "";
        boolean finishActivity = false;

        if (path.equals(Constants.PATH_POST_REPLY_RESULT_SUCCESS)) {
            message = getString(R.string.reply_successful);
        } else if (path.equals(Constants.PATH_POST_REPLY_RESULT_FAILURE)) {
            message = getString(R.string.reply_failed_sad_face);
        } else if (path.equals(Constants.PATH_KEY_SAVE_TO_POCKET_RESULT_SUCCESS)) {
            message = getString(R.string.saving_to_pocket_succeeded);
        } else if (path.equals(Constants.PATH_KEY_SAVE_TO_POCKET_RESULT_FAILED)) {
            message = getString(R.string.saving_to_pocket_failed_sad_face);
        } else if (path.equals(Constants.PATH_KEY_VOTE_RESULT_FAILED)) {
            message = getString(R.string.voting_failed);
        } else if (path.equals(Constants.PATH_KEY_VOTE_RESULT_SUCCESS)) {
            message = getString(R.string.voting_succeded);
        } else if (path.equals(Constants.PATH_KEY_GETTING_COMMENTS_RESULT_FAILED)) {
            message = getString(R.string.retrieving_comments_failed);
        } else if (path.equals(Constants.PATH_NO_NEW_POSTS)) {
            message = getString(R.string.no_posts_to_retrieve);
            finishActivity = true;
        }

        updateUser(message, finishActivity);
    }

    private void updateUser(String message, final boolean finishActivity) {
        if (!TextUtils.isEmpty(message)) {
            final String msg = message;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (finishActivity) {
                        sendBroadcast(new Intent(getString(R.string.force_finish_main_activity)));
                    }
                    Toast.makeText(NotificationListenerService.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        ConnectionResult result = mGoogleApiClient.blockingConnect(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await().getInputStream();
        mGoogleApiClient.disconnect();

        if (assetInputStream == null) {
            Logger.Log("Requested an unknown Asset");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        if (!mGoogleApiClient.isConnected()) {
            ConnectionResult connectionResult = mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Logger.Log("NotifListenerService, service failed to connect: " + connectionResult);
                return;
            }
        }

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                Logger.Log("NotificationListenerService path: " + path);

                if (path.equals(Constants.PATH_REDDIT_POSTS)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();

                    final String latestPosts = dataMap.getString(Constants.KEY_REDDIT_POSTS);
                    final boolean openOnPhoneDismisses = dataMap.getBoolean(Constants.KEY_DISMISS_AFTER_ACTION);
                    final ArrayList<Integer> actionOrder = dataMap.getIntegerArrayList(Constants.KEY_ACTION_ORDER);

                    ArrayList<Post> posts = mGson.fromJson(latestPosts, Post.getPostsListTypeToken());

                    Bitmap themeBlueBitmap = Bitmap.createBitmap(new int[]{getResources().getColor(R.color.primary)}, 1, 1, Bitmap.Config.ARGB_8888);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    for (int i = 0; i < posts.size(); i++) {
                        Post post = posts.get(i);

                        Bitmap backgroundBitmap = null;
                        if (post.hasThumbnail() || post.hasHighResImage()) {
                            Asset a = dataMap.getAsset(post.getId());
                            if (a != null) {
                                backgroundBitmap = loadBitmapFromAsset(a);
                            }
                        }

                        int notificationId = sNotificationId;

                        String title = post.isDirectMessage() ? getString(R.string.message_from_x, post.getAuthor()) : post.getSubreddit();

                        Notification.Builder builder = new Notification.Builder(this)
                                .setContentTitle(title)
                                .setContentText((post.isDirectMessage() ? post.getDescription() : post.getPostContents()))
                                .setSmallIcon(R.drawable.ic_launcher);

                        boolean hasCachedImage;
                        if (backgroundBitmap != null) {
                            if (post.hasThumbnail()) {
                                // If the post has a thumbnail, use it - this will filter out nfsw etc thumbnails
                                // but will still allow the user to see the full image if they like
                                builder.setLargeIcon(backgroundBitmap);
                            } else {
                                setBlueBackground(themeBlueBitmap, builder);
                                enableNotificationGrouping(builder);
                            }
                            hasCachedImage = cacheBackgroundToDisk(notificationId, backgroundBitmap);
                        } else {
                            hasCachedImage = false;
                            setBlueBackground(themeBlueBitmap, builder);
                            enableNotificationGrouping(builder);
                        }

                        addActions(actionOrder, openOnPhoneDismisses, hasCachedImage, post, notificationId, builder);

                        if (hasCachedImage) {
                            // When the notification is dismissed, we will remove this image from the file cache
                            builder.setDeleteIntent(getDeletePendingIntent(notificationId));
                        }

                        notificationManager.notify(notificationId, builder.build());

                        if (backgroundBitmap != null) {
                            backgroundBitmap.recycle();
                        }

                        sNotificationId += NOTIFICATION_ID_INCREMENT;
                        sendBroadcast(new Intent(getString(R.string.force_finish_main_activity)));
                    }
                } else if (path.equals(Constants.PATH_COMMENTS)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();

                    final String comments = dataMap.getString(Constants.KEY_REDDIT_POSTS);

                    if (!TextUtils.isEmpty(comments)) {
                        if (comments.equals("[]")) {
                            Toast.makeText(NotificationListenerService.this, R.string.thread_has_no_comments_yet, Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(this, CommentsActivity.class);
                            intent.putExtra(Constants.KEY_REDDIT_POSTS, comments);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    }
                }
            }
        }
    }

    private void enableNotificationGrouping(Notification.Builder builder) {
        // if it's not got an image we can group it with the other text based ones
        builder.setGroup(GROUP_KEY_SUBREDDIT_POSTS);
    }

    private void setBlueBackground(Bitmap themeBlueBitmap, Notification.Builder builder) {
        Notification.WearableExtender extender = new Notification.WearableExtender();
        extender.setBackground(themeBlueBitmap);
        builder.extend(extender);
    }

    private boolean cacheBackgroundToDisk(int notificationId, Bitmap backgroundBitmap) {
        boolean isCached = false;

        File localCache = new File(getCacheDir(), getCachedImageName(notificationId));
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(localCache);
            backgroundBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            Logger.Log("Error writing local cache", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                isCached = true;
            } catch (IOException e) {
                Logger.Log("Error closing local cache file", e);
            }
        }

        return isCached;
    }

    // Add the actions to the builder, based on the given actionOrder list
    private void addActions(ArrayList<Integer> actionOrder, boolean openOnPhoneDismisses, boolean hasCachedImage, Post post, int notificationId, Notification.Builder builder) {
        for (int i = 0; i < actionOrder.size(); i++) {
            int order = actionOrder.get(i);

            switch (order) {
                case Constants.ACTION_ORDER_VIEW_COMMENTS:
                    builder.addAction(new Notification.Action.Builder(R.drawable.ic_message_white_48dp,
                            getString(R.string.view_comments),
                            getViewCommentsPendingIntent(post, notificationId)).build());
                    break;
                case Constants.ACTION_ORDER_REPLY:
                    builder.addAction(new Notification.Action.Builder(R.drawable.ic_reply_white_48dp, getString(R.string.reply_to_x, post.getShortTitle()), getReplyPendingIntent(post, notificationId))
                            .addRemoteInput(new RemoteInput.Builder(EXTRA_VOICE_REPLY).build())
                            .build());
                    break;
                case Constants.ACTION_ORDER_UPVOTE:
                    builder.addAction(new Notification.Action.Builder(R.drawable.ic_upvote_white_48dp, getString(R.string.upvote_x, post.getShortTitle()), getVotePendingIntent(post, 1, REQUEST_CODE_VOTE_UP + notificationId)).build());
                    break;
                case Constants.ACTION_ORDER_DOWNVOTE:
                    builder.addAction(new Notification.Action.Builder(R.drawable.ic_downvote_white_48dp, getString(R.string.downvote_x, post.getShortTitle()), getVotePendingIntent(post, -1, REQUEST_CODE_VOTE_DOWN + notificationId)).build());
                    break;
                case Constants.ACTION_ORDER_SAVE_TO_POCKET:
                    builder.addAction(new Notification.Action.Builder(R.drawable.ic_pocket,
                            getString(R.string.save_to_pocket),
                            getSaveToPocketPendingIntent(post.getPermalink(), notificationId)).build());
                    break;
                case Constants.ACTION_ORDER_OPEN_ON_PHONE:
                    builder.addAction(new Notification.Action.Builder(R.drawable.ic_full_openonphone,
                            getString(R.string.open_on_phone),
                            getOpenOnPhonePendingIntent(post.getPermalink(), openOnPhoneDismisses, notificationId)).build());
                    break;
                case Constants.ACTION_ORDER_VIEW_IMAGE:
                    if (hasCachedImage) {
                        builder.addAction(new Notification.Action.Builder(R.drawable.ic_image_white_48dp,
                                getString(R.string.view_image),
                                getViewImagePendingIntent(notificationId)).build());
                    }
                    break;
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
                        Logger.Log("sendReplyToPhone, putDataItem status: " + dataItemResult.getStatus().toString());
                    }
                });
    }

    private PendingIntent getVotePendingIntent(Post post, int voteDirection, int requestCode) {
        Intent vote = new Intent(this, ActionReceiver.class);
        vote.putExtra(Constants.KEY_PATH, Constants.PATH_VOTE);
        vote.putExtra(Constants.KEY_CONFIRMATION_MESSAGE, getString(R.string.vote));
        vote.putExtra(Constants.KEY_CONFIRMATION_ANIMATION, ConfirmationActivity.SUCCESS_ANIMATION);
        vote.putExtra(Constants.PATH_KEY_POST_FULLNAME, post.getFullname());
        vote.putExtra(Constants.KEY_POST_VOTE_DIRECTION, voteDirection);
        return PendingIntent.getBroadcast(this, requestCode, vote, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getOpenOnPhonePendingIntent(String permalink, boolean openOnPhoneDismisses, int notificationId) {
        Intent openOnPhone = new Intent(this, ActionReceiver.class);
        openOnPhone.putExtra(Constants.KEY_PATH, Constants.PATH_OPEN_ON_PHONE);
        openOnPhone.putExtra(Constants.KEY_CONFIRMATION_MESSAGE, getString(R.string.open_on_phone));
        openOnPhone.putExtra(Constants.KEY_CONFIRMATION_ANIMATION, ConfirmationActivity.OPEN_ON_PHONE_ANIMATION);
        openOnPhone.putExtra(Constants.KEY_POST_PERMALINK, permalink);
        openOnPhone.putExtra(Constants.KEY_DISMISS_AFTER_ACTION, openOnPhoneDismisses);
        openOnPhone.putExtra(Constants.KEY_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(this, REQUEST_CODE_OPEN_ON_PHONE + notificationId, openOnPhone, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getSaveToPocketPendingIntent(String permalink, int notificationId) {
        Intent saveToPocket = new Intent(this, ActionReceiver.class);
        saveToPocket.putExtra(Constants.KEY_PATH, Constants.PATH_SAVE_TO_POCKET);
        saveToPocket.putExtra(Constants.KEY_CONFIRMATION_MESSAGE, getString(R.string.save_to_pocket));
        saveToPocket.putExtra(Constants.KEY_CONFIRMATION_ANIMATION, ConfirmationActivity.SUCCESS_ANIMATION);
        saveToPocket.putExtra(Constants.KEY_POST_PERMALINK, permalink);
        return PendingIntent.getBroadcast(this, REQUEST_CODE_SAVE_TO_POCKET + notificationId, saveToPocket, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getReplyPendingIntent(Post post, int notificationId) {
        Intent intent = new Intent(ACTION_RESPONSE);
        intent.putExtra(Constants.PATH_KEY_IS_DIRECT_MESSAGE, post.isDirectMessage());
        intent.putExtra(Constants.PATH_KEY_MESSAGE_TO_USER, post.getAuthor());
        intent.putExtra(Constants.PATH_KEY_MESSAGE_SUBJECT, post.getDescription());
        intent.putExtra(Constants.PATH_KEY_POST_FULLNAME, post.getFullname());
        return PendingIntent.getService(this, REQUEST_CODE_REPLY + notificationId, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getViewCommentsPendingIntent(Post post, int notificationId) {
        Intent intent = new Intent(this, ActionReceiver.class);
        intent.putExtra(Constants.KEY_PATH, Constants.PATH_COMMENTS);
        intent.putExtra(Constants.KEY_CONFIRMATION_MESSAGE, getString(R.string.getting_comments));
        intent.putExtra(Constants.KEY_CONFIRMATION_ANIMATION, ConfirmationActivity.SUCCESS_ANIMATION);
        intent.putExtra(Constants.KEY_POST_PERMALINK, post.getPermalink());
        return PendingIntent.getBroadcast(this, REQUEST_VIEW_COMMENTS + notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getViewImagePendingIntent(int notificationId) {
        Intent intent = new Intent(this, ViewImageActivity.class);
        intent.putExtra(Constants.KEY_HIGHRES_IMAGE_NAME, getCachedImageName(notificationId));
        return PendingIntent.getActivity(this, REQUEST_VIEW_FULLSCREEN_IMAGE + notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent getDeletePendingIntent(int notificationId) {
        Intent intent = new Intent(this, DeleteCachedImageReceiver.class);
        intent.putExtra(Constants.KEY_HIGHRES_IMAGE_NAME, getCachedImageName(notificationId));
        return PendingIntent.getBroadcast(this, REQUEST_VIEW_COMMENTS + notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getCachedImageName(int notificationId) {
        return notificationId + ".png";
    }
}
