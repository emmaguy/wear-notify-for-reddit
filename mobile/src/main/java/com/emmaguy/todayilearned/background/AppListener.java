package com.emmaguy.todayilearned.background;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.emmaguy.todayilearned.SettingsActivity;
import com.emmaguy.todayilearned.Utils;

import java.util.concurrent.TimeUnit;

public class AppListener implements WakefulIntentService.AlarmListener {
    public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String refreshString = prefs.getString(SettingsActivity.PREFS_REFRESH_FREQUENCY, "15");

        if (!TextUtils.isEmpty(refreshString)) {
            int refreshIntervalMinutes = Integer.parseInt(refreshString);
            if (refreshIntervalMinutes > 0) {
                Utils.Log("Setting alarm, interval: " + refreshIntervalMinutes);
                mgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000, TimeUnit.MINUTES.toMillis(refreshIntervalMinutes), pi);
            } else if (refreshIntervalMinutes == -1) {
                WakefulIntentService.cancelAlarms(context);
            }
        }
    }

    public void sendWakefulWork(Context context) {
        WakefulIntentService.sendWakefulWork(context, RetrieveService.class);
    }

    public long getMaxAge() {
        return (AlarmManager.INTERVAL_FIFTEEN_MINUTES * 2);
    }
}