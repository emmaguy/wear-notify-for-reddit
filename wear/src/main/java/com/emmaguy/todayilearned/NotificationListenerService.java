package com.emmaguy.todayilearned;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

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
                if (path.equals("/redditwear")) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    final ArrayList<String> posts = dataMapItem.getDataMap().getStringArrayList("posts");
                    final ArrayList<String> subredditsForEachPost = dataMapItem.getDataMap().getStringArrayList("subredditsForEachPost");

                    ArrayList<Notification> notifications = new ArrayList<Notification>();
                    for (int i = 0; i < posts.size(); i++) {
                        NotificationCompat.BigTextStyle extraPageStyle = new NotificationCompat.BigTextStyle();
                        extraPageStyle.bigText(posts.get(i));
                        extraPageStyle.setBigContentTitle(subredditsForEachPost.get(i));
                        Notification extraPageNotification = new NotificationCompat.Builder(this)
                                .setStyle(extraPageStyle)
                                .build();

                        notifications.add(extraPageNotification);
                    }

                    Intent dismissIntent = new Intent(this, DismissNotificationsReceiver.class);
                    dismissIntent.putExtra(DismissNotificationsReceiver.NOTIFICATION_ID_EXTRA, NOTIFICATION_ID);
                    dismissIntent.setAction(DismissNotificationsReceiver.DISMISS_ACTION);

                    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                            .addAction(R.drawable.ic_action_done, getString(R.string.dismiss_all), dismissPendingIntent)
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
}