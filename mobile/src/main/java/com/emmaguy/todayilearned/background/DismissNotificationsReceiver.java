package com.emmaguy.todayilearned.background;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DismissNotificationsReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_ID_EXTRA = "NOTIFICATION_ID";
    public static final String DISMISS_ACTION = "com.emmaguy.todayilearned.background.DISMISS_ACTION";

    public DismissNotificationsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(DISMISS_ACTION)) {
            int notificationId = intent.getIntExtra(NOTIFICATION_ID_EXTRA, 0);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(notificationId);
        }
    }
}
