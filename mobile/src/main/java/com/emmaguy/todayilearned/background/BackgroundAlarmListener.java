package com.emmaguy.todayilearned.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.App;
import com.emmaguy.todayilearned.refresh.RetrieveService;
import com.emmaguy.todayilearned.storage.UserStorage;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class BackgroundAlarmListener implements WakefulIntentService.AlarmListener {
    @Inject UserStorage mUserStorage;

    public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context context) {
        App.with(context).getAppComponent().inject(this);

        final String refreshInterval = mUserStorage.getRefreshInterval();
        if (!TextUtils.isEmpty(refreshInterval)) {
            int refreshIntervalMinutes = Integer.parseInt(refreshInterval);
            if (refreshIntervalMinutes > 0) {
                final long triggerAtMillis = SystemClock.elapsedRealtime() + 1000;
                final long interval = TimeUnit.MINUTES.toMillis(refreshIntervalMinutes);
                mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, interval, pi);
            } else if (refreshIntervalMinutes == -1) {
                WakefulIntentService.cancelAlarms(context);
            }
        }
    }

    public void sendWakefulWork(Context context) {
        WakefulIntentService.sendWakefulWork(context, RetrieveService.getFromBackgroundSyncIntent(context));
    }

    public long getMaxAge() {
        return (AlarmManager.INTERVAL_FIFTEEN_MINUTES * 2);
    }
}
