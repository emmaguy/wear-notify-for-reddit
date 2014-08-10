package com.emmaguy.todayilearned;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.emmaguy.todayilearned.sharedlib.Constants;
import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationListenerService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 1;

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
                    ArrayList<Post> posts = gson.fromJson(latestPosts, new TypeToken<ArrayList<Post>>() {}.getType());

                    ArrayList<Notification> notifications = new ArrayList<Notification>();
                    for (int i = 0; i < posts.size(); i++) {
                        NotificationCompat.BigTextStyle extraPageStyle = new NotificationCompat.BigTextStyle();
                        extraPageStyle.bigText(posts.get(i).getTitle());
                        extraPageStyle.setBigContentTitle(posts.get(i).getSubreddit());

                        Notification extraPageNotification = new NotificationCompat.Builder(this)
                                .setStyle(extraPageStyle)
                                .build();

                        notifications.add(extraPageNotification);
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .addAction(R.drawable.ic_action_done, getString(R.string.dismiss_all), getDismissPendingIntent())
                            .addAction(R.drawable.go_to_phone_00156, getString(R.string.open_on_phone), getOpenOnPhonePendingIntent())
                            .setContentTitle(getResources().getQuantityString(R.plurals.x_new_posts, notifications.size(), notifications.size()))
                            .setSmallIcon(R.drawable.ic_launcher);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
                    notificationManager.notify(NOTIFICATION_ID,
                            new NotificationCompat.WearableExtender()
                                    .addPages(notifications)
                                    .extend(builder)
                                    .build());
                }
            }
        }
    }

    private PendingIntent getOpenOnPhonePendingIntent() {
        Intent openOnPhone = new Intent(this, OpenOnPhoneReceiver.class);
        return PendingIntent.getBroadcast(this, 0, openOnPhone, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getDismissPendingIntent() {
        Intent dismissIntent = new Intent(this, DismissNotificationsReceiver.class);
        dismissIntent.putExtra(DismissNotificationsReceiver.NOTIFICATION_ID_EXTRA, NOTIFICATION_ID);
        dismissIntent.setAction(DismissNotificationsReceiver.DISMISS_ACTION);

        return PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}