package com.emmaguy.todayilearned;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.emmaguy.todayilearned.background.AlarmReceiver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {
    private static final String SELECTED_SUBREDDITS = "selectedsubreddits";
    private static final String SUBREDDITS = "subreddits";

    private static Set<String> DefaultSubReddits = new HashSet<String>(Arrays.asList("todayilearned", "Android", "crazyideas", "worldnews"));
    private static Set<String> DefaultSelectedSubReddits = new HashSet<String>(Arrays.asList("Android"));

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

    private static SharedPreferences getSharedPreferences(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
    }

    public static List<String> allSubReddits(Context c) {
        Set<String> subReddits = getSharedPreferences(c).getStringSet(SUBREDDITS, DefaultSubReddits);

        ArrayList<String> sortedSubreddits = new ArrayList(subReddits);
        Collections.sort(sortedSubreddits);

        return sortedSubreddits;
    }

    public static List<String> selectedSubReddits(Context c) {
        Set<String> subReddits = getSharedPreferences(c).getStringSet(SELECTED_SUBREDDITS, DefaultSelectedSubReddits);

        return new ArrayList(subReddits);
    }

    public static void saveSubreddits(Context c, List<String> srs) {
        getSharedPreferences(c).edit().putStringSet(SUBREDDITS, new HashSet<String>(srs)).apply();
    }

    public static void saveSelectedSubreddits(Context c, List<String> selectedSubreddits) {
        getSharedPreferences(c).edit().putStringSet(SELECTED_SUBREDDITS, new HashSet<String>(selectedSubreddits)).apply();
    }

    public static void addSubreddit(Context c, String subreddit) {
        List<String> subreddits = allSubReddits(c);
        subreddits.add(subreddit);

        saveSubreddits(c, subreddits);
    }
}
