package com.emmaguy.todayilearned;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.emmaguy.todayilearned.background.AlarmReceiver;

public class Utils {
    public static void setRecurringAlarm(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String refreshString = prefs.getString(SettingsActivity.PREFS_REFRESH_FREQUENCY, "1");

        if (!TextUtils.isEmpty(refreshString)) {
            int refreshIntervalMinutes = Integer.parseInt(refreshString);
            if (refreshIntervalMinutes > 0) {
                Intent downloader = new Intent(context, AlarmReceiver.class);
                PendingIntent refreshIntent = PendingIntent.getBroadcast(context, 0, downloader, PendingIntent.FLAG_CANCEL_CURRENT);

                AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), convertMinutesToMillis(refreshIntervalMinutes), refreshIntent);
            }
        }
    }

    private static long convertMinutesToMillis(int minutes) {
        return 60000 * minutes;
    }
}
