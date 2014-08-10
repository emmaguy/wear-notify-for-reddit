package com.emmaguy.todayilearned;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;

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

            showConfirmation(context);
        }
    }

    private void showConfirmation(Context context) {
        Intent confirmationActivity = new Intent(context, ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, context.getString(R.string.dismiss_all));
        context.startActivity(confirmationActivity);
    }
}
